package com.example.maprecognizer.analyzer

import java.io.File
import java.nio.file.Paths

/**
 * UI组件分析器，从Compose组件代码中提取UI组件信息
 * 注意：只采集事实，不做推断
 */
class UIComponentAnalyzer : Analyzer {
    
    override fun startAnalysis() {
        // 开始分析，初始化资源
    }
    
    override fun stopAnalysis() {
        // 停止分析，释放资源
    }
    
    // 查找匹配的括号
    private fun findMatchingBrace(content: String, startIndex: Int): Int {
        var braceCount = 1
        for (i in startIndex until content.length) {
            when (content[i]) {
                '(' -> braceCount++
                ')' -> braceCount--
            }
            if (braceCount == 0) {
                return i
            }
        }
        return -1
    }
    
    /**
     * 从Button内容中提取文本
     * 支持多种Button类型（Button、OutlinedButton、TextButton、FloatingActionButton、IconButton）
     * 支持多种文本提取方式：text参数、直接字符串参数、Text组件
     * @param buttonContent Button组件的内容
     * @param buttonDeclaration Button组件的声明
     * @param buttonType Button类型
     * @return 提取到的按钮文本
     */
    private fun extractTextFromContent(buttonContent: String, buttonDeclaration: String, buttonType: String): String {
        // 首先尝试从Button声明中提取text参数
        val textParamRegex = Regex("""text\s*=\s*([\"'])(.*?)\1""", RegexOption.IGNORE_CASE)
        val textParamMatch = textParamRegex.find(buttonDeclaration)
        if (textParamMatch != null) {
            return textParamMatch.groupValues[2]
        }
        
        // 尝试从Button声明中提取直接字符串参数
        val directParamRegex = Regex("""(Button|OutlinedButton|TextButton)\s*\(([^,)]*[\"'])(.*?)([\"'][^,)]*)\)""")
        val directParamMatch = directParamRegex.find(buttonDeclaration)
        if (directParamMatch != null) {
            return directParamMatch.groupValues[3]
        }
        
        // 查找Button内容中的Text组件
        val textRegex = Regex("""Text\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
        val textMatch = textRegex.find(buttonContent)
        if (textMatch != null) {
            val fullTextComponent = textMatch.groupValues[0]
            
            // 提取文本值
            // 匹配 text = "文本" 或直接 Text("文本") 或 text = if (...) "文本1" else "文本2"
            
            // 首先尝试匹配 text = "文本"
            val textValueRegex = Regex("""text\s*=\s*(.*?)\s*(,|\))""")
            val textValueMatch = textValueRegex.find(fullTextComponent)
            
            if (textValueMatch != null) {
                val textValue = textValueMatch.groupValues[1].trim()
                
                // 检查是否是if表达式
                if (textValue.startsWith("if")) {
                    // 提取if表达式中的两个文本值
                    val ifTextRegex = Regex("""([\"'])(.*?)\1\s*else\s*([\"'])(.*?)\3""")
                    val ifTextMatch = ifTextRegex.find(textValue)
                    if (ifTextMatch != null) {
                        // 返回第一个文本值作为默认值
                        return ifTextMatch.groupValues[2]
                    }
                } else if (textValue.startsWith("when")) {
                    // 提取when表达式中的第一个文本值
                    val whenTextRegex = Regex("""->\s*([\"'])(.*?)\1""")
                    val whenTextMatch = whenTextRegex.find(textValue)
                    if (whenTextMatch != null) {
                        // 返回第一个文本值作为默认值
                        return whenTextMatch.groupValues[2]
                    }
                } else if (textValue.matches("""[\"'].+[\"']""".toRegex())) {
                    // 直接字符串值
                    return textValue.substring(1, textValue.length - 1)
                }
            } else {
                // 尝试匹配直接Text("文本")
                val directTextRegex = Regex("""\([^)]*([\"'])(.*?)\1[^)]*\)""")
                val directTextMatch = directTextRegex.find(fullTextComponent)
                if (directTextMatch != null) {
                    return directTextMatch.groupValues[2]
                }
                
                // 尝试匹配Text(when {...})
                val whenInTextRegex = Regex("""when\s*\{([^}]*)\}""", RegexOption.DOT_MATCHES_ALL)
                val whenInTextMatch = whenInTextRegex.find(fullTextComponent)
                if (whenInTextMatch != null) {
                    val whenContent = whenInTextMatch.groupValues[1]
                    
                    // 提取when表达式中的第一个文本值
                    val whenTextRegex = Regex("""->\s*([\"'])(.*?)\1""")
                    val whenTextMatch = whenTextRegex.find(whenContent)
                    if (whenTextMatch != null) {
                        return whenTextMatch.groupValues[2]
                    }
                }
            }
        }
        // 4. 尝试提取图标按钮的contentDescription
        val contentDescRegex = Regex("""contentDescription\s*=\s*([\"'])(.*?)\1""", RegexOption.IGNORE_CASE)
        contentDescRegex.find(buttonDeclaration)?.let { 
            return it.groupValues[2]
        }
        
        // 5. 从Icon组件中提取contentDescription
        val iconRegex = Regex("""Icon\s*\([^,]+,\s*contentDescription\s*=\s*([\"'])(.*?)\1""", RegexOption.IGNORE_CASE)
        iconRegex.find(buttonContent)?.let { 
            return it.groupValues[2]
        }
        
        // 6. 从IconButton中提取图标名称
        val iconNameRegex = Regex("""Icons\.(\w+)\.(\w+)""")
        iconNameRegex.find(buttonDeclaration)?.let { match ->
            val iconName = match.groupValues[2].lowercase()
            // 将图标名称转换为友好文本
            return when (iconName) {
                "add" -> "添加"
                "edit" -> "编辑"
                "delete" -> "删除"
                "refresh" -> "刷新"
                "search" -> "搜索"
                "arrowback" -> "返回"
                "arrowdropdown" -> "下拉菜单"
                "arrowupward" -> "向上箭头"
                "arrowdownward" -> "向下箭头"
                "arrowforward" -> "前进"
                "arrowright" -> "向右箭头"
                "arrowleft" -> "向左箭头"
                "close" -> "关闭"
                "check" -> "确认"
                "save" -> "保存"
                "cancel" -> "取消"
                "menu" -> "菜单"
                "morevert" -> "更多选项"
                "settings" -> "设置"
                "home" -> "首页"
                "person" -> "个人中心"
                "phone" -> "电话"
                "email" -> "邮箱"
                "location" -> "位置"
                "share" -> "分享"
                "favorite" -> "收藏"
                "star" -> "评分"
                "info" -> "信息"
                "warning" -> "警告"
                "error" -> "错误"
                "done" -> "完成"
                "clear" -> "清除"
                "filter" -> "筛选"
                "sort" -> "排序"
                "download" -> "下载"
                "upload" -> "上传"
                "print" -> "打印"
                "back" -> "返回"
                "next" -> "下一步"
                "prev" -> "上一步"
                "account" -> "账户"
                "logout" -> "退出登录"
                "login" -> "登录"
                "register" -> "注册"
                "password" -> "密码"
                "visibility" -> "显示密码"
                "visibilityoff" -> "隐藏密码"
                "lock" -> "锁定"
                "unlock" -> "解锁"
                "camera" -> "相机"
                "gallery" -> "相册"
                "mic" -> "麦克风"
                "volumeup" -> "音量增大"
                "volumedown" -> "音量减小"
                "volumeoff" -> "静音"
                "play" -> "播放"
                "pause" -> "暂停"
                "stop" -> "停止"
                "skipnext" -> "下一曲"
                "skipprev" -> "上一曲"
                "shuffle" -> "随机播放"
                "repeat" -> "重复播放"
                "repeatonce" -> "重复一次"
                "favoriteborder" -> "未收藏"
                "like" -> "点赞"
                "dislike" -> "点踩"
                "comment" -> "评论"
                "send" -> "发送"
                "reply" -> "回复"
                "forward" -> "转发"
                "copy" -> "复制"
                "cut" -> "剪切"
                "paste" -> "粘贴"
                "selectall" -> "全选"
                "deselect" -> "取消选择"
                "crop" -> "裁剪"
                "rotate" -> "旋转"
                "flip" -> "翻转"
                else -> iconName
            }
        }
        
        // 7. 从IconButton中提取图标名称（另一种格式）
        val iconNameRegex2 = Regex("""icon\s*=\s*Icons\.(\w+)\.(\w+)""")
        iconNameRegex2.find(buttonDeclaration)?.let { match ->
            val iconName = match.groupValues[2].lowercase()
            // 将图标名称转换为友好文本
            return when (iconName) {
                "arrowdropdown" -> "下拉菜单"
                "arrowback" -> "返回"
                "add" -> "添加"
                "edit" -> "编辑"
                "delete" -> "删除"
                "refresh" -> "刷新"
                "close" -> "关闭"
                "check" -> "确认"
                else -> iconName
            }
        }
        
        // 8. 如果以上都失败，根据按钮类型生成默认文本
        return when (buttonType) {
            "FloatingActionButton" -> "悬浮操作按钮"
            "IconButton" -> "图标按钮"
            "OutlinedButton" -> "轮廓按钮"
            "TextButton" -> "文本按钮"
            else -> "按钮"
        }
    }
    
    /**
     * 分析UI组件文件，提取组件信息
     */
    fun analyzeUIComponentFiles(projectPath: String): List<UIComponentInfo> {
        val uiFiles = findUIFiles(projectPath)
        val uiComponentInfoList = mutableListOf<UIComponentInfo>()
        
        for (file in uiFiles) {
            val info = analyzeSingleUIFile(file)
            uiComponentInfoList.addAll(info)
        }
        
        return uiComponentInfoList
    }

    // 查找项目中的UI文件
    private fun findUIFiles(projectPath: String): List<File> {
        val uiFiles = mutableListOf<File>()
        val srcPath = Paths.get(projectPath, "app", "src", "main", "java").toFile()
        
        srcPath.walk().forEach { file ->
            if (file.isFile && file.extension == "kt" && 
                (file.name.contains("Screen") || file.name.contains("screen") ||
                 file.name.contains("Component") || file.name.contains("component") ||
                 file.name.contains("View") || file.name.contains("view"))) {
                uiFiles.add(file)
            }
        }
        
        return uiFiles
    }

    // 分析单个UI文件
    private fun analyzeSingleUIFile(file: File): List<UIComponentInfo> {
        val uiComponentInfoList = mutableListOf<UIComponentInfo>()
        val content = file.readText()
        
        // 1. 提取页面名称
        val screenName = file.name.replace("Screen.kt", "").replace("screen.kt", "")
        
        // 2. 提取组件类型和属性
        // 2.1 提取按钮组件
        // 使用正则表达式匹配多种按钮类型：Button、OutlinedButton、TextButton、FloatingActionButton、IconButton
        val buttonRegex = Regex(
            """(Button|OutlinedButton|TextButton|FloatingActionButton|IconButton)\s*\(([^)]*)\)\s*\{([\s\S]*?)\}""",
            RegexOption.DOT_MATCHES_ALL
        )
        val buttonMatches = buttonRegex.findAll(content)
        
        var buttonIndex = 0
        
        for (buttonMatch in buttonMatches) {
            val buttonType = buttonMatch.groupValues[1]
            val buttonParams = buttonMatch.groupValues[2]
            val buttonContent = buttonMatch.groupValues[3]
            val buttonDeclaration = buttonType + "(" + buttonParams + ")"
            val componentName = "${screenName}_${buttonType.lowercase()}_${buttonIndex++}"
            val componentId = componentName
            
            // 提取按钮文本
            var buttonText = extractTextFromContent(buttonContent, buttonDeclaration, buttonType)
            
            // 特殊处理：如果文本是"按钮 N"，尝试从Text组件中提取
            if (buttonText.startsWith("按钮 ")) {
                val textInButtonRegex = Regex("""Text\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
                val textMatch = textInButtonRegex.find(buttonContent)
                if (textMatch != null) {
                    val textContent = textMatch.groupValues[1]
                    
                    // 提取Text组件中的文本
                    val directTextRegex = Regex("""([\"'])(.*?)\1""")
                    val directTextMatch = directTextRegex.find(textContent)
                    if (directTextMatch != null) {
                        buttonText = directTextMatch.groupValues[2]
                    }
                }
            }
            
            // 提取事件处理
            val onClickRegex = Regex("""onClick\s*=\s*\{([^\}]+)\}""")
            val onClickMatch = onClickRegex.find(buttonParams + buttonContent)
            val action = onClickMatch?.groupValues?.get(1)?.trim() ?: ""
            
            // 构建组件事件
            val events = mutableListOf<ComponentEvent>()
            if (action.isNotEmpty()) {
                // 尝试从action中提取目标页面
                val targetPage = extractTargetPage(action)
                events.add(ComponentEvent("CLICK", targetPage, emptyMap()))
            }
            
            // 构建组件属性
            val properties = mutableMapOf<String, Any>()
            properties["text"] = buttonText
            
            // 构建UIComponentInfo对象
            val uiComponentInfo = UIComponentInfo(
                screenName = screenName,
                componentId = componentId,
                componentName = componentName,
                componentType = buttonType,
                properties = properties,
                events = events,
                positionFormula = mapOf(
                    "x" to "screenWidth * 0.1",
                    "y" to "screenHeight * 0.1"
                ),
                sizeFormula = mapOf(
                    "width" to "screenWidth * 0.2",
                    "height" to "screenHeight * 0.06"
                )
            )
            
            uiComponentInfoList.add(uiComponentInfo)
        }
        
        // 2.2 提取文本组件
        val textRegex = Regex("""Text\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
        val textMatches = textRegex.findAll(content)
        
        for ((index, match) in textMatches.withIndex()) {
            val textContent = match.value
            val componentName = "${screenName}_text_${index}"
            val componentId = componentName
            
            // 提取文本内容
            val contentRegex = Regex("""([\"'])(.*?)\1""")
            val contentMatch = contentRegex.find(textContent)
            val text = contentMatch?.groupValues?.get(2)
            
            // 构建组件属性
            val properties = mutableMapOf<String, Any>()
            if (text != null && text.isNotBlank()) {
                properties["text"] = text
            }
            
            // 构建UIComponentInfo对象
            val uiComponentInfo = UIComponentInfo(
                screenName = screenName,
                componentId = componentId,
                componentName = componentName,
                componentType = "TEXT",
                properties = properties,
                events = emptyList(),
                positionFormula = mapOf(
                    "x" to "screenWidth * 0.2",
                    "y" to "screenHeight * 0.2"
                ),
                sizeFormula = mapOf(
                    "width" to "screenWidth * 0.6",
                    "height" to "screenHeight * 0.04"
                )
            )
            
            uiComponentInfoList.add(uiComponentInfo)
        }
        
        // 2.3 提取文本字段组件
        val textFieldRegex = Regex("""TextField\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
        val textFieldMatches = textFieldRegex.findAll(content)
        
        for ((index, match) in textFieldMatches.withIndex()) {
            val textFieldContent = match.value
            val componentName = "${screenName}_text_field_${index}"
            val componentId = componentName
            
            // 提取标签文本
            val labelRegex = Regex("""label\s*=\s*[^(]*\([^\"]*\"([^\"]+)\"\)""")
            val labelMatch = labelRegex.find(textFieldContent)
            val label = labelMatch?.groupValues?.get(1)
            
            // 提取事件处理
            val onValueChangeRegex = Regex("""onValueChange\s*=\s*\{([^\}]+)\}""")
            val onValueChangeMatch = onValueChangeRegex.find(textFieldContent)
            val action = onValueChangeMatch?.groupValues?.get(1)?.trim() ?: ""
            
            // 构建组件事件
            val events = mutableListOf<ComponentEvent>()
            if (action.isNotEmpty()) {
                events.add(ComponentEvent("TEXT_CHANGE", null, emptyMap()))
            }
            
            // 构建组件属性
            val properties = mutableMapOf<String, Any>()
            if (label != null) {
                properties["label"] = label
            }
            
            // 构建UIComponentInfo对象
            val uiComponentInfo = UIComponentInfo(
                screenName = screenName,
                componentId = componentId,
                componentName = componentName,
                componentType = "TEXT_FIELD",
                properties = properties,
                events = events,
                positionFormula = mapOf(
                    "x" to "screenWidth * 0.1",
                    "y" to "screenHeight * 0.3"
                ),
                sizeFormula = mapOf(
                    "width" to "screenWidth * 0.8",
                    "height" to "screenHeight * 0.06"
                )
            )
            
            uiComponentInfoList.add(uiComponentInfo)
        }
        
        // 2.4 提取复选框组件
        val checkboxRegex = Regex("""Checkbox\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
        val checkboxMatches = checkboxRegex.findAll(content)
        
        for ((index, match) in checkboxMatches.withIndex()) {
            val checkboxContent = match.value
            val componentName = "${screenName}_checkbox_${index}"
            val componentId = componentName
            
            // 提取事件处理
            val onCheckedChangeRegex = Regex("""onCheckedChange\s*=\s*\{([^\}]+)\}""")
            val onCheckedChangeMatch = onCheckedChangeRegex.find(checkboxContent)
            val action = onCheckedChangeMatch?.groupValues?.get(1)?.trim() ?: ""
            
            // 构建组件事件
            val events = mutableListOf<ComponentEvent>()
            if (action.isNotEmpty()) {
                events.add(ComponentEvent("CHECKED_CHANGE", null, emptyMap()))
            }
            
            // 构建组件属性
            val properties = mutableMapOf<String, Any>()
            
            // 构建UIComponentInfo对象
            val uiComponentInfo = UIComponentInfo(
                screenName = screenName,
                componentId = componentId,
                componentName = componentName,
                componentType = "CHECKBOX",
                properties = properties,
                events = events,
                positionFormula = mapOf(
                    "x" to "screenWidth * 0.1",
                    "y" to "screenHeight * 0.4"
                ),
                sizeFormula = mapOf(
                    "width" to "screenWidth * 0.05",
                    "height" to "screenHeight * 0.03"
                )
            )
            
            uiComponentInfoList.add(uiComponentInfo)
        }
        
        // 2.5 提取开关组件
        val switchRegex = Regex("""Switch\s*\((.*?)\)""", RegexOption.DOT_MATCHES_ALL)
        val switchMatches = switchRegex.findAll(content)
        
        for ((index, match) in switchMatches.withIndex()) {
            val switchContent = match.value
            val componentName = "${screenName}_switch_${index}"
            val componentId = componentName
            
            // 提取事件处理
            val onCheckedChangeRegex = Regex("""onCheckedChange\s*=\s*\{([^\}]+)\}""")
            val onCheckedChangeMatch = onCheckedChangeRegex.find(switchContent)
            val action = onCheckedChangeMatch?.groupValues?.get(1)?.trim() ?: ""
            
            // 构建组件事件
            val events = mutableListOf<ComponentEvent>()
            if (action.isNotEmpty()) {
                events.add(ComponentEvent("CHECKED_CHANGE", null, emptyMap()))
            }
            
            // 构建组件属性
            val properties = mutableMapOf<String, Any>()
            
            // 构建UIComponentInfo对象
            val uiComponentInfo = UIComponentInfo(
                screenName = screenName,
                componentId = componentId,
                componentName = componentName,
                componentType = "SWITCH",
                properties = properties,
                events = events,
                positionFormula = mapOf(
                    "x" to "screenWidth * 0.1",
                    "y" to "screenHeight * 0.45"
                ),
                sizeFormula = mapOf(
                    "width" to "screenWidth * 0.15",
                    "height" to "screenHeight * 0.03"
                )
            )
            
            uiComponentInfoList.add(uiComponentInfo)
        }
        
        return uiComponentInfoList
    }
    
    // 从事件处理中提取目标页面
    private fun extractTargetPage(action: String): String? {
        // 匹配 navigateToXXX() 格式
        val navigateRegex = Regex("""navigateTo(\w+)\(""")
        val navigateMatch = navigateRegex.find(action)
        if (navigateMatch != null) {
            return navigateMatch.groupValues[1]
        }
        
        // 匹配 navigate("XXX") 格式
        val navigateToRegex = Regex("""navigate\("(\w+)"\)""")
        val navigateToMatch = navigateToRegex.find(action)
        if (navigateToMatch != null) {
            return navigateToMatch.groupValues[1]
        }
        
        // 匹配其他导航格式
        val otherNavigateRegex = Regex("""(navigateUp|popBackStack)\(""")
        if (otherNavigateRegex.find(action) != null) {
            return "Home"
        }
        
        return null
    }
}
