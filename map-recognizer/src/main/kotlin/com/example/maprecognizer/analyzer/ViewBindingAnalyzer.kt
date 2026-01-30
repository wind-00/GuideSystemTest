package com.example.maprecognizer.analyzer

import java.io.File
import java.nio.file.Paths
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

/**
 * ViewBinding分析器，用于处理使用ViewBinding的Android应用
 * 负责解析XML布局文件和Activity文件，提取UI组件信息和导航关系
 */
class ViewBindingAnalyzer : Analyzer {
    
    override fun startAnalysis() {
        // 开始分析，初始化资源
    }
    
    override fun stopAnalysis() {
        // 停止分析，释放资源
    }
    
    /**
     * 分析项目中的所有ViewBinding相关文件
     * @param projectPath 项目路径
     * @return 屏幕信息列表
     */
    fun analyzeProject(projectPath: String): List<ScreenInfo> {
        val screenInfos = mutableListOf<ScreenInfo>()
        
        // 1. 查找所有Activity文件
        val activityFiles = findActivityFiles(projectPath)
        println("   找到 ${activityFiles.size} 个Activity文件")
        
        // 2. 查找所有XML布局文件
        val layoutFiles = findLayoutFiles(projectPath)
        println("   找到 ${layoutFiles.size} 个XML布局文件")
        
        // 3. 分析每个Activity文件，关联对应的布局文件
        for (activityFile in activityFiles) {
            val activityInfo = analyzeActivity(activityFile, layoutFiles)
            screenInfos.add(activityInfo)
        }
        
        return screenInfos
    }
    
    /**
     * 查找项目中的所有Activity文件
     */
    private fun findActivityFiles(projectPath: String): List<File> {
        val activityFiles = mutableListOf<File>()
        val srcPath = Paths.get(projectPath, "app", "src", "main", "java").toFile()
        
        srcPath.walk().forEach { file ->
            if (file.isFile && file.extension == "kt") {
                // 匹配以任意字符开头，包含Activity，后面可能跟数字，以.kt结尾的文件
                val fileName = file.name
                // 简化匹配逻辑，直接检查文件名是否包含Activity且以.kt结尾
                if (fileName.contains("Activity") && fileName.endsWith(".kt")) {
                    activityFiles.add(file)
                }
            }
        }
        
        return activityFiles
    }
    
    /**
     * 查找项目中的所有XML布局文件
     */
    private fun findLayoutFiles(projectPath: String): List<File> {
        val layoutFiles = mutableListOf<File>()
        val resPath = Paths.get(projectPath, "app", "src", "main", "res", "layout").toFile()
        
        if (resPath.exists()) {
            resPath.walk().forEach { file ->
                if (file.isFile && file.extension == "xml") {
                    layoutFiles.add(file)
                }
            }
        }
        
        return layoutFiles
    }
    
    /**
     * 分析单个Activity文件，关联对应的布局文件
     */
    private fun analyzeActivity(activityFile: File, layoutFiles: List<File>): ScreenInfo {
        val content = activityFile.readText()
        // 正确提取Activity名称，移除.kt后缀和Activity后缀
        var activityName = activityFile.name.replace(".kt", "")
        activityName = activityName.replace("Activity", "")
        
        println("   分析Activity: $activityName")
        
        // 提取路由信息（从Activity名称推断）
        val route = "/${activityName.lowercase()}"
        
        // 查找对应的布局文件
        val layoutFile = findMatchingLayoutFile(activityName, layoutFiles, activityFile.name)
        
        // 从布局文件中提取组件信息
        val components = if (layoutFile != null) {
            val extractedComponents = extractComponentsFromLayout(layoutFile, activityName)
            // 丰富组件事件信息，添加导航目标
            enrichComponentsWithEvents(content, extractedComponents)
        } else {
            emptyList()
        }
        
        // 从Activity中提取导航目标
        val navigationTargets = extractNavigationTargetsFromActivity(content)
        
        return ScreenInfo(
            screenName = activityName,
            route = route,
            components = components,
            navigationTargets = navigationTargets
        )
    }
    
    /**
     * 查找与Activity匹配的布局文件
     */
    private fun findMatchingLayoutFile(activityName: String, layoutFiles: List<File>, fullActivityName: String): File? {
        println("   查找Activity '$activityName'（完整名称：$fullActivityName）的布局文件")
        
        // 收集所有布局文件名，用于调试
        val layoutFileNames = layoutFiles.map { it.name }
        println("   可用布局文件：$layoutFileNames")
        
        // 尝试1：使用Activity名称（不含Activity后缀）查找，如 activity_second.xml
        val expectedLayoutName1 = "activity_${activityName.lowercase()}"
        println("   尝试匹配：$expectedLayoutName1.xml")
        var layoutFile = layoutFiles.find { it.name == "$expectedLayoutName1.xml" }
        
        // 尝试2：使用完整的Activity名称（包含Activity后缀）查找，如 activity_secondactivity2.xml
        if (layoutFile == null) {
            val fullLayoutName = "activity_${fullActivityName.replace(".kt", "").lowercase()}"
            println("   尝试匹配：$fullLayoutName.xml")
            layoutFile = layoutFiles.find { it.name == "$fullLayoutName.xml" }
        }
        
        // 尝试3：使用Activity名称（小写）直接匹配，如 second2.xml
        if (layoutFile == null) {
            val simpleLayoutName = "${activityName.lowercase()}"
            println("   尝试匹配：$simpleLayoutName.xml")
            layoutFile = layoutFiles.find { it.name == "$simpleLayoutName.xml" }
        }
        
        // 尝试4：使用完整的Activity名称（去掉.kt和Activity后缀）查找，如 activity_second2.xml
        if (layoutFile == null) {
            val fullActivityNameWithoutExtension = fullActivityName.replace(".kt", "")
            val fullActivityNameWithoutSuffix = fullActivityNameWithoutExtension.replace("Activity", "")
            val expectedLayoutName4 = "activity_${fullActivityNameWithoutSuffix.lowercase()}"
            println("   尝试匹配：$expectedLayoutName4.xml")
            layoutFile = layoutFiles.find { it.name == "$expectedLayoutName4.xml" }
        }
        
        println("   找到匹配的布局文件：${layoutFile?.name}")
        return layoutFile
    }
    
    /**
     * 从XML布局文件中提取组件信息
     */
    private fun extractComponentsFromLayout(layoutFile: File, screenName: String): List<UIComponentInfo> {
        val components = mutableListOf<UIComponentInfo>()
        
        try {
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val document = docBuilder.parse(layoutFile)
            val root = document.documentElement
            root.normalize()
            
            println("   解析布局文件: ${layoutFile.name}, 根元素: ${root.tagName}")
            
            // 处理<layout>根元素的情况，获取实际布局根元素
            val actualRoot = if (root.tagName == "layout") {
                root.firstChild?.let { 
                    if (it is org.w3c.dom.Element) {
                        it
                    } else {
                        it.nextSibling as? org.w3c.dom.Element
                    }
                } ?: root
            } else {
                root
            }
            
            println("   实际布局根元素: ${actualRoot.tagName}")
            
            // 递归提取所有组件
            extractComponentsRecursively(actualRoot, screenName, components)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        println("   从${layoutFile.name}提取到${components.size}个组件")
        return components
    }
    
    /**
     * 递归提取组件
     */
    private fun extractComponentsRecursively(
        element: org.w3c.dom.Element,
        screenName: String,
        components: MutableList<UIComponentInfo>
    ) {
        // 处理当前元素
        val idAttr = element.getAttribute("android:id")
        if (idAttr.isNotEmpty()) {
            // 转换ID格式，从 @+id/btn_normal 到 btnNormal
            val componentId = idAttr.substringAfterLast("/")
                .split("_")
                .joinToString("") { it.capitalize() }
                .replaceFirstChar { it.lowercase() }
            
            // 确定组件类型
            val componentType = when (element.tagName) {
                "Button", "android.widget.Button" -> "BUTTON"
                "ImageView", "android.widget.ImageView" -> "IMAGE"
                "TextView", "android.widget.TextView" -> "TEXT"
                "EditText", "android.widget.EditText" -> "TEXT_FIELD"
                "Switch", "android.widget.Switch" -> "SWITCH"
                "CheckBox", "android.widget.CheckBox" -> "CHECKBOX"
                "RadioButton", "android.widget.RadioButton" -> "RADIO_BUTTON"
                "SeekBar", "android.widget.SeekBar" -> "SEEK_BAR"
                else -> "OTHER"
            }
            
            // 提取组件属性
            val properties = mutableMapOf<String, Any>()
            
            // 提取文本属性
            val textAttr = element.getAttribute("android:text")
            if (textAttr.isNotEmpty()) {
                properties["text"] = textAttr
            }
            
            // 提取内容描述
            val contentDescAttr = element.getAttribute("android:contentDescription")
            if (contentDescAttr.isNotEmpty()) {
                properties["contentDescription"] = contentDescAttr
            }
            
            // 提取可见性
            val visibilityAttr = element.getAttribute("android:visibility")
            properties["visible"] = visibilityAttr != "gone"
            
            // 提取启用状态
            val enabledAttr = element.getAttribute("android:enabled")
            properties["enabled"] = if (enabledAttr.isNotEmpty()) enabledAttr.toBoolean() else true
            
            // 构建组件事件
            val events = mutableListOf<ComponentEvent>()
            
            // 根据组件类型添加支持的事件
            when (componentType) {
                "BUTTON", "IMAGE" -> {
                    events.add(ComponentEvent("CLICK", null, emptyMap()))
                }
                "SWITCH", "CHECKBOX", "RADIO_BUTTON" -> {
                    events.add(ComponentEvent("CHECKED_CHANGE", null, emptyMap()))
                }
                "TEXT_FIELD" -> {
                    events.add(ComponentEvent("TEXT_CHANGE", null, emptyMap()))
                }
                "SEEK_BAR" -> {
                    events.add(ComponentEvent("PROGRESS_CHANGE", null, emptyMap()))
                }
                else -> {
                    // 其他组件类型，检查是否有点击事件
                    if (element.tagName in listOf("androidx.cardview.widget.CardView", "androidx.constraintlayout.widget.ConstraintLayout", "LinearLayout", "RelativeLayout")) {
                        events.add(ComponentEvent("CLICK", null, emptyMap()))
                    }
                }
            }
            
            // 创建UI组件信息
            val component = UIComponentInfo(
                screenName = screenName,
                componentId = componentId,
                componentName = componentId,
                componentType = componentType,
                properties = properties,
                events = events
            )
            
            components.add(component)
        }
        
        // 递归处理子元素
        val children = element.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child is org.w3c.dom.Element) {
                extractComponentsRecursively(child, screenName, components)
            }
        }
    }
    
    /**
     * 从Activity文件中提取导航目标
     */
    private fun extractNavigationTargetsFromActivity(content: String): List<NavigationTarget> {
        val targets = mutableListOf<NavigationTarget>()
        
        // 查找所有Intent调用
        val intentPattern = Pattern.compile(
            """Intent\(\s*\w+\s*,\s*(\w+)\.class\s*\)""",
            Pattern.MULTILINE
        )
        
        val matcher = intentPattern.matcher(content)
        while (matcher.find()) {
            val targetActivity = matcher.group(1) ?: continue
            
            targets.add(
                NavigationTarget(
                    type = NavigationTargetType.NAVIGATE,
                    target = targetActivity
                )
            )
        }
        
        return targets
    }
    
    /**
     * 从Activity文件中提取组件事件处理
     * @param activityContent Activity文件内容
     * @param components 从布局文件中提取的组件列表
     * @return 更新后的组件列表，包含事件处理信息
     */
    private fun enrichComponentsWithEvents(activityContent: String, components: List<UIComponentInfo>): List<UIComponentInfo> {
        return components.map { component ->
            // 查找该组件的事件处理代码
            val updatedEvents = component.events.map { event ->
                when (event.eventType) {
                    "CLICK" -> {
                        // 查找点击事件处理 - 更宽松的正则表达式，支持更多格式
                        val clickPattern = Pattern.compile(
                            "binding\\.${component.componentId}\\.setOnClickListener\\s*(?:\\{|\\()(.*?)(?:\\}|\\))",
                            Pattern.DOTALL
                        )
                        val clickMatcher = clickPattern.matcher(activityContent)
                        if (clickMatcher.find()) {
                            val clickContent = clickMatcher.group(1) ?: ""
                            // 从点击事件中提取导航目标
                            val target = extractTargetFromClick(clickContent)
                            event.copy(target = target)
                        } else {
                            // 尝试另一种模式：onClickListener = View.OnClickListener { ... }
                            val altClickPattern = Pattern.compile(
                                "binding\\.${component.componentId}\\.onClickListener\\s*=\\s*(?:View\\.OnClickListener\\s*)?(?:\\{|\\()(.*?)(?:\\}|\\))",
                                Pattern.DOTALL
                            )
                            val altClickMatcher = altClickPattern.matcher(activityContent)
                            if (altClickMatcher.find()) {
                                val clickContent = altClickMatcher.group(1) ?: ""
                                val target = extractTargetFromClick(clickContent)
                                event.copy(target = target)
                            } else {
                                event
                            }
                        }
                    }
                    else -> event
                }
            }
            
            component.copy(events = updatedEvents)
        }
    }
    
    /**
     * 从点击事件中提取导航目标
     */
    private fun extractTargetFromClick(clickContent: String): String? {
        // 查找Intent调用
        val intentPattern = Pattern.compile(
            """Intent\(\s*\w+\s*,\s*(\w+)\.class\s*\)"""
        )
        
        val matcher = intentPattern.matcher(clickContent)
        if (matcher.find()) {
            return matcher.group(1)
        }
        
        // 查找finish()调用，返回"FINISH"作为标记
        if (clickContent.contains("finish()")) {
            return "FINISH"
        }
        
        return null
    }
}