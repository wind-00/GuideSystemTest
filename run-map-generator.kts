#!/usr/bin/env kotlin

import java.io.File
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilderFactory

// 设置项目根目录
val projectRoot = Paths.get(".").toAbsolutePath().toString()

// 设置输出文件路径
val outputFile = File(projectRoot, "app_automation_map_from_script.json")

// 辅助函数：将对象转换为 JSON 字符串
fun Any?.toJsonString(): String {
    return when (this) {
        null -> "null"
        is Map<*, *> -> {
            val entries = this.entries.joinToString(",\n") {
                "  \"${it.key}\": ${it.value.toJsonString()}"
            }
            "{\n$entries\n}"
        }
        is List<*> -> {
            val elements = this.joinToString(",\n") {
                it.toJsonString().prependIndent("  ")
            }
            "[\n$elements\n]"
        }
        is String -> "\"$this\""
        is Number, is Boolean -> this.toString()
        else -> "\"${this.toString()}\""
    }
}

// 辅助函数：为字符串添加缩进
fun String.prependIndent(indent: String): String {
    return this.lines().joinToString("\n") {
        if (it.isBlank()) it else "$indent$it"
    }
}

// 数据类定义
enum class NavigationTargetType {
    NAVIGATE,
    NAVIGATE_BACK,
    POP_BACK_STACK,
    UNKNOWN
}

data class NavigationTarget(
    val type: NavigationTargetType,
    val target: String
)

data class ComponentEvent(
    val eventType: String,
    val target: String?,
    val parameters: Map<String, Any>,
    val action: String = ""
)

data class UIComponentInfo(
    val screenName: String,
    val componentId: String,
    val componentName: String,
    val componentType: String,
    val properties: Map<String, Any>,
    val events: List<ComponentEvent>
)

data class ScreenInfo(
    val screenName: String,
    val route: String,
    val components: List<UIComponentInfo>,
    val navigationTargets: List<NavigationTarget>
)

// ViewBinding分析器实现
class ViewBindingAnalyzer {
    
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
            val screenInfo = analyzeActivity(activityFile, layoutFiles)
            screenInfos.add(screenInfo)
        }
        
        return screenInfos
    }
    
    private fun findActivityFiles(projectPath: String): List<File> {
        val activityFiles = mutableListOf<File>()
        val srcPath = Paths.get(projectPath, "app", "src", "main", "java").toFile()
        
        srcPath.walk().forEach { file ->
            if (file.isFile && file.extension == "kt" && file.name.endsWith("Activity.kt")) {
                activityFiles.add(file)
            }
        }
        
        return activityFiles
    }
    
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
    
    private fun analyzeActivity(activityFile: File, layoutFiles: List<File>): ScreenInfo {
        val content = activityFile.readText()
        val activityName = activityFile.name.replace("Activity.kt", "")
        
        println("   分析Activity: $activityName")
        
        // 提取路由信息（从Activity名称推断）
        val route = "/${activityName.lowercase()}"
        
        // 查找对应的布局文件
        val layoutFile = findMatchingLayoutFile(activityName, layoutFiles)
        
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
    
    private fun findMatchingLayoutFile(activityName: String, layoutFiles: List<File>): File? {
        // 收集所有布局文件名，用于调试
        val layoutFileNames = layoutFiles.map { it.name }
        
        // 尝试1：使用Activity名称查找，如 activity_second.xml
        val expectedLayoutName1 = "activity_${activityName.lowercase()}"
        var layoutFile = layoutFiles.find { it.name == "$expectedLayoutName1.xml" }
        
        // 尝试2：使用完整的Activity名称（包含Activity后缀）查找，如 activity_secondactivity2.xml
        if (layoutFile == null) {
            val fullLayoutName = "activity_${activityName}activity${activityName.filter { it.isDigit() }}".lowercase()
            layoutFile = layoutFiles.find { it.name == "$fullLayoutName.xml" }
        }
        
        // 尝试3：使用Activity名称（小写）直接匹配，如 second2.xml
        if (layoutFile == null) {
            val simpleLayoutName = "${activityName.lowercase()}"
            layoutFile = layoutFiles.find { it.name == "$simpleLayoutName.xml" }
        }
        
        return layoutFile
    }
    
    private fun extractComponentsFromLayout(layoutFile: File, screenName: String): List<UIComponentInfo> {
        val components = mutableListOf<UIComponentInfo>()
        
        try {
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val document = docBuilder.parse(layoutFile)
            val root = document.documentElement
            root.normalize()
            
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
            
            // 递归提取所有组件
            extractComponentsRecursively(actualRoot, screenName, components)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
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
                .joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }
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
    
    private fun extractNavigationTargetsFromActivity(content: String): List<NavigationTarget> {
        val targets = mutableListOf<NavigationTarget>()
        
        // 查找所有Intent调用
        val intentPattern = Regex("""Intent\(\s*\w+\s*,\s*(\w+)\.class\s*\)""")
        
        for (matchResult in intentPattern.findAll(content)) {
            val targetActivity = matchResult.groupValues[1]
            targets.add(
                NavigationTarget(
                    type = NavigationTargetType.NAVIGATE,
                    target = targetActivity
                )
            )
        }
        
        return targets
    }
    
    fun enrichComponentsWithEvents(activityContent: String, components: List<UIComponentInfo>): List<UIComponentInfo> {
        return components.map { component ->
            // 查找该组件的事件处理代码
            val updatedEvents = component.events.map { event ->
                when (event.eventType) {
                    "CLICK" -> {
                        // 查找点击事件处理 - 更宽松的正则表达式，支持更多格式
                        val clickPattern = Regex("""binding\.${component.componentId}\.setOnClickListener\s*(?:\{|\()(.*?)(?:\}|\))""", RegexOption.DOT_MATCHES_ALL)
                        val clickMatch = clickPattern.find(activityContent)
                        if (clickMatch != null) {
                            val clickContent = clickMatch.groupValues[1]
                            // 从点击事件中提取导航目标
                            val target = extractTargetFromClick(clickContent)
                            event.copy(target = target)
                        } else {
                            // 尝试另一种模式：onClickListener = View.OnClickListener { ... }
                            val altClickPattern = Regex("""binding\.${component.componentId}\.onClickListener\s*=\s*(?:View\.OnClickListener\s*)?(?:\{|\()(.*?)(?:\}|\))""", RegexOption.DOT_MATCHES_ALL)
                            val altClickMatch = altClickPattern.find(activityContent)
                            if (altClickMatch != null) {
                                val clickContent = altClickMatch.groupValues[1]
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
    
    private fun extractTargetFromClick(clickContent: String): String? {
        // 查找Intent调用
        val intentPattern = Regex("""Intent\(\s*\w+\s*,\s*(\w+)\.class\s*\)""")
        val match = intentPattern.find(clickContent)
        if (match != null) {
            return match.groupValues.get(1)
        }
        
        // 查找finish()调用，返回"FINISH"作为标记
        if (clickContent.contains("finish()")) {
            return "FINISH"
        }
        
        return null
    }
}

// 地图生成器实现
class MapGenerator {
    
    fun generateAppAutomationMap(screenInfos: List<ScreenInfo>): Map<String, Any> {
        // 1. 构建应用元信息
        val appMeta = mapOf(
            "appName" to "GuideSystemTest",
            "packageName" to "com.example.guidesystemtest",
            "versionName" to "1.0.0",
            "versionCode" to 1,
            "uiFramework" to "VIEW"
        )
        
        // 2. 构建UI模型
        val uiModel = buildUIModel(screenInfos)
        
        // 3. 构建状态模型
        val stateModel = buildStateModel(screenInfos)
        
        // 4. 构建意图模型
        val intentModel = buildIntentModel(screenInfos)
        
        // 5. 生成完整的AppAutomationMap
        return mapOf(
            "appMeta" to appMeta,
            "uiModel" to uiModel,
            "stateModel" to stateModel,
            "intentModel" to intentModel
        )
    }
    
    private fun buildUIModel(screenInfos: List<ScreenInfo>): Map<String, Any> {
        val pages = screenInfos.map { screenInfo ->
            mapOf(
                "pageId" to screenInfo.screenName,
                "pageName" to screenInfo.screenName,
                "route" to screenInfo.route,
                "layoutType" to "VIEW",
                "components" to screenInfo.components.map { component ->
                    mapOf(
                        "componentId" to component.componentId,
                        "viewType" to component.componentType,
                        "text" to component.properties["text"],
                        "contentDescription" to component.properties["contentDescription"],
                        "positionFormula" to mapOf(
                            "x" to "component.bounds.left",
                            "y" to "component.bounds.top"
                        ),
                        "sizeFormula" to mapOf(
                            "width" to "component.bounds.width()",
                            "height" to "component.bounds.height()"
                        ),
                        "enabled" to component.properties["enabled"],
                        "supportedTriggers" to component.events.map { event ->
                            event.eventType.uppercase()
                        }
                    )
                }
            )
        }
        
        return mapOf("pages" to pages)
    }
    
    private fun buildStateModel(screenInfos: List<ScreenInfo>): Map<String, Any> {
        val states = screenInfos.map { screenInfo ->
            val signals = mutableListOf<Map<String, Any?>>()
            
            // 优先添加组件可见信号（强信号）
            if (screenInfo.components.isNotEmpty()) {
                val firstComponent = screenInfo.components.first()
                signals.add(
                    mapOf(
                        "type" to "COMPONENT_VISIBLE",
                        "target" to firstComponent.componentId,
                        "expectedValue" to true,
                        "matcher" to "EQUALS"
                    )
                )
                
                // 如果组件有文本，添加文本可见信号
                if (firstComponent.properties["text"] is String && (firstComponent.properties["text"] as String).isNotEmpty()) {
                    signals.add(
                        mapOf(
                            "type" to "TEXT_VISIBLE",
                            "target" to firstComponent.properties["text"],
                            "expectedValue" to true,
                            "matcher" to "EQUALS"
                        )
                    )
                }
            }
            
            // 始终添加路由匹配信号
            signals.add(
                mapOf(
                    "type" to "ROUTE_MATCH",
                    "target" to screenInfo.route,
                    "expectedValue" to screenInfo.route,
                    "matcher" to "EQUALS"
                )
            )
            
            // 添加页面激活信号（补充）
            signals.add(
                mapOf(
                    "type" to "PAGE_ACTIVE",
                    "target" to screenInfo.screenName,
                    "expectedValue" to null,
                    "matcher" to "EQUALS"
                )
            )
            
            mapOf(
                "stateId" to screenInfo.screenName,
                "name" to screenInfo.screenName,
                "description" to "页面 ${screenInfo.screenName}",
                "signals" to signals,
                "relatedPageIds" to listOf(screenInfo.screenName)
            )
        }
        
        // 设置初始状态ID
        val initialStateId = "Main"
        
        return mapOf(
            "states" to states,
            "initialStateId" to initialStateId
        )
    }
    
    private fun buildIntentModel(screenInfos: List<ScreenInfo>): Map<String, Any> {
        val intents = mutableListOf<Map<String, Any>>()
        
        // 遍历屏幕信息
        for (screenInfo in screenInfos) {
            // 为每个组件创建意图
            for (component in screenInfo.components) {
                for (event in component.events) {
                    // 确定起始状态ID：当前屏幕的screenName
                    val fromStateId = screenInfo.screenName
                    
                    // 检查是否是返回按钮
                    val isBackButton = (component.properties["text"] as? String ?: "").contains("返回", ignoreCase = true) || 
                                      (component.properties["text"] as? String ?: "").contains("back", ignoreCase = true)
                    
                    // 确定目标状态ID
                    val toStateId = when {
                        // 处理返回按钮
                        isBackButton -> {
                            when (screenInfo.screenName) {
                                "Second", "Second2" -> "Main"
                                "Third", "Third2", "Third3" -> "Second"
                                else -> screenInfo.screenName // 默认保持当前状态
                            }
                        }
                        // 处理带有明确target的导航
                        event.target != null && event.target!!.isNotEmpty() -> {
                            when (event.target) {
                                "FINISH" -> {
                                    // 处理finish()方法
                                    when (screenInfo.screenName) {
                                        "Second", "Second2" -> "Main"
                                        "Third", "Third2", "Third3" -> "Second"
                                        else -> screenInfo.screenName // 默认保持当前状态
                                    }
                                }
                                else -> {
                                    // 将Activity类名转换为状态ID（移除Activity后缀）
                                    event.target!!.replace("Activity", "")
                                }
                            }
                        }
                        // 检查按钮文本是否包含导航相关词汇
                        component.properties["text"] is String -> {
                            val buttonText = component.properties["text"] as String
                            when {
                                buttonText.contains("第二层级", ignoreCase = true) || buttonText.contains("second", ignoreCase = true) -> "Second"
                                buttonText.contains("第三层级", ignoreCase = true) || buttonText.contains("third", ignoreCase = true) -> "Third"
                                else -> screenInfo.screenName // 保持在当前状态
                            }
                        }
                        else -> screenInfo.screenName // 保持在当前状态
                    }
                    
                    // 确定意图类型，细化为三类
                    val intentType = when {
                        // 返回按钮
                        isBackButton -> {
                            "NAVIGATE_BACK"
                        }
                        // 导航行为：State发生变化
                        fromStateId != toStateId -> {
                            "NAVIGATION"
                        }
                        // 内部状态变化：如开关、滑块等
                        event.eventType.uppercase() in listOf("CHECKED_CHANGE", "PROGRESS_CHANGE", "TEXT_CHANGE") -> {
                            "STATE_INTERNAL"
                        }
                        // 无状态变化：普通点击等
                        else -> {
                            "NO_STATE_CHANGE"
                        }
                    }
                    
                    // 生成状态转移后验证条件
                    val postConditions = generatePostConditions(screenInfos, toStateId)
                    
                    // 确定UI操作绑定
                    val uiBindings = listOf(
                        mapOf(
                            "componentId" to component.componentId,
                            "trigger" to event.eventType.uppercase(),
                            "parameters" to event.parameters
                        )
                    )
                    
                    // 生成唯一的intentId，格式：componentId_trigger_fromStateId
                    // 确保同名组件在不同State下的Intent有不同的ID
                    val intentId = "${component.componentId}_${event.eventType.lowercase()}_from${fromStateId}"
                    
                    // 创建意图
                    val intent = mapOf(
                        "intentId" to intentId,
                        "type" to intentType,
                        "description" to "在${fromStateId}状态下，${component.componentName}的${event.eventType}事件",
                        "fromStateId" to fromStateId,
                        "uiBindings" to uiBindings,
                        "toStateId" to toStateId,
                        "postConditions" to postConditions
                    )
                    
                    intents.add(intent)
                }
            }
        }
        
        return mapOf("intents" to intents)
    }
    
    /**
     * 生成状态转移后验证条件
     * 根据目标状态ID对应的屏幕信息生成验证条件
     */
    private fun generatePostConditions(screenInfos: List<ScreenInfo>, toStateId: String): List<Map<String, Any?>> {
        // 查找目标状态对应的屏幕信息
        val targetScreenInfo = screenInfos.find { it.screenName == toStateId }
        
        if (targetScreenInfo == null) {
            // 如果找不到目标屏幕信息，返回空列表
            return emptyList()
        }
        
        val postConditions = mutableListOf<Map<String, Any?>>()
        
        // 添加路由匹配验证条件
        postConditions.add(
            mapOf(
                "type" to "ROUTE_MATCH",
                "target" to targetScreenInfo.route,
                "expectedValue" to targetScreenInfo.route,
                "matcher" to "EQUALS"
            )
        )
        
        // 添加页面激活验证条件
        postConditions.add(
            mapOf(
                "type" to "PAGE_ACTIVE",
                "target" to toStateId,
                "expectedValue" to null,
                "matcher" to "EQUALS"
            )
        )
        
        // 添加组件可见性验证条件（使用页面的第一个组件）
        if (targetScreenInfo.components.isNotEmpty()) {
            val firstComponent = targetScreenInfo.components.first()
            postConditions.add(
                mapOf(
                    "type" to "COMPONENT_VISIBLE",
                    "target" to firstComponent.componentId,
                    "expectedValue" to true,
                    "matcher" to "EQUALS"
                )
            )
            
            // 如果第一个组件有文本，添加文本可见性验证条件
            if (firstComponent.properties["text"] is String && (firstComponent.properties["text"] as String).isNotEmpty()) {
                postConditions.add(
                    mapOf(
                        "type" to "TEXT_VISIBLE",
                        "target" to firstComponent.properties["text"],
                        "expectedValue" to true,
                        "matcher" to "EQUALS"
                    )
                )
            }
        }
        
        return postConditions
    }
}

// 地图生成器的核心逻辑
fun generateMap() {
    println("=== 开始生成应用自动化地图 ===")
    
    try {
        // 1. 创建ViewBinding分析器
        println("1. 初始化ViewBinding分析器...")
        val analyzer = ViewBindingAnalyzer()
        
        // 2. 分析项目
        println("\n2. 分析ViewBinding代码...")
        val screenInfos = analyzer.analyzeProject(projectRoot)
        println("   已分析到 ${screenInfos.size} 个页面")
        
        // 3. 生成地图
        println("\n3. 生成应用自动化地图...")
        val generator = MapGenerator()
        val appAutomationMap = generator.generateAppAutomationMap(screenInfos)
        
        // 4. 使用自定义函数序列化地图为 JSON
        val json = appAutomationMap.toJsonString()
        
        // 5. 保存到文件
        outputFile.writeText(json)
        
        println("\n✅ 地图已成功生成并保存！")
        println("📄 输出文件: ${outputFile.absolutePath}")
        
        // 计算地图统计信息
        val uiModel = appAutomationMap["uiModel"] as Map<String, Any>
        val stateModel = appAutomationMap["stateModel"] as Map<String, Any>
        val intentModel = appAutomationMap["intentModel"] as Map<String, Any>
        
        println("📊 地图统计:")
        println("   页面数量: ${(uiModel["pages"] as List<*>).size}")
        println("   状态数量: ${(stateModel["states"] as List<*>).size}")
        println("   意图数量: ${(intentModel["intents"] as List<*>).size}")
        println("📄 文件大小: ${outputFile.length()} 字节")
        
    } catch (e: Exception) {
        println("\n❌ 生成地图失败: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
    
    println("\n=== 地图生成完成 ===")
}

// 执行地图生成
if (outputFile.exists()) {
    outputFile.delete()
    println("已删除旧的地图文件")
}

generateMap()