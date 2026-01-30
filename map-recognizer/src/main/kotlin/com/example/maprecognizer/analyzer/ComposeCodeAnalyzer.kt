package com.example.maprecognizer.analyzer

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

/**
 * Compose代码分析器，用于从Compose文件中提取屏幕和组件信息
 */
class ComposeCodeAnalyzer {
    
    /**
     * 分析项目中的所有Compose屏幕
     */
    fun analyzeProject(projectPath: String): List<ScreenInfo> {
        val screenInfos = mutableListOf<ScreenInfo>()
        
        // 查找所有Screen.kt文件
        val screenFiles = findScreenFiles(projectPath)
        println("   找到 ${screenFiles.size} 个Screen.kt文件")
        
        // 分析每个屏幕文件
        for (file in screenFiles) {
            val screenInfo = analyzeScreenFile(file)
            screenInfos.add(screenInfo)
        }
        
        return screenInfos
    }
    
    /**
     * 查找项目中的所有Screen.kt文件
     */
    private fun findScreenFiles(projectPath: String): List<File> {
        val screenFiles = mutableListOf<File>()
        val appSrcPath = Paths.get(projectPath, "app", "src", "main", "java")
        
        if (Files.exists(appSrcPath)) {
            appSrcPath.toFile().walk().forEach { file ->
                if (file.isFile && file.name.endsWith("Screen.kt")) {
                    screenFiles.add(file)
                }
            }
        }
        
        return screenFiles
    }
    
    /**
     * 分析单个屏幕文件
     */
    private fun analyzeScreenFile(file: File): ScreenInfo {
        val content = file.readText()
        val fileName = file.name
        val screenName = fileName.replace("Screen.kt", "")
        
        println("   分析屏幕: $screenName")
        
        // 提取路由信息（从文件名推断）
        val route = "/${screenName.lowercase()}"
        
        // 提取组件信息
        val components = extractComponents(content, screenName)
        
        // 提取导航目标
        val navigationTargets = extractNavigationTargets(content)
        
        return ScreenInfo(
            screenName = screenName,
            route = route,
            components = components,
            navigationTargets = navigationTargets
        )
    }
    
    /**
     * 提取组件信息
     */
    private fun extractComponents(content: String, screenName: String): List<UIComponentInfo> {
        val components = mutableListOf<UIComponentInfo>()
        
        // 简单的正则表达式匹配FunctionCard组件
        val functionCardPattern = Pattern.compile(
            """FunctionCard\(\s*title\s*=\s*(stringResource\(id\s*=\s*R\.string\.(\w+)\)|"([^"]+)")\s*,""",
            Pattern.MULTILINE
        )
        
        val matcher = functionCardPattern.matcher(content)
        var buttonIndex = 0
        
        while (matcher.find()) {
            val resourceId = matcher.group(2) ?: matcher.group(3)
            val buttonText = resourceId ?: "Button ${buttonIndex}"
            
            val component = UIComponentInfo(
                screenName = screenName,
                componentId = "${screenName}Button${buttonIndex}",
                componentName = "FunctionCard",
                componentType = "BUTTON",
                properties = mapOf(
                    "text" to buttonText,
                    "enabled" to true,
                    "contentDescription" to "Button component in $screenName"
                ),
                events = listOf(
                    ComponentEvent(
                        eventType = "CLICK",
                        target = extractNavigationTargetFromClick(content, matcher.start()),
                        parameters = emptyMap()
                    )
                )
            )
            
            components.add(component)
            buttonIndex++
        }
        
        return components
    }
    
    /**
     * 从点击事件中提取导航目标
     */
    private fun extractNavigationTargetFromClick(content: String, startIndex: Int): String {
        // 查找当前FunctionCard后的onClick参数
        val onClickPattern = Pattern.compile(
            """onClick\s*=\s*\{\s*(navigationActions\.)?(\w+)\(\)\s*\}""",
            Pattern.MULTILINE
        )
        
        val substring = content.substring(startIndex)
        val matcher = onClickPattern.matcher(substring)
        
        if (matcher.find()) {
            val navigateMethod = matcher.group(2) ?: return ""
            
            // 从导航方法名提取目标屏幕
            return when (navigateMethod) {
                "navigateToDoctorList" -> "DoctorList"
                "navigateToPatientList" -> "PatientList"
                "navigateToAppointmentList" -> "AppointmentList"
                "navigateToBookAppointment" -> "BookAppointment"
                "navigateToPrescriptionList" -> "PrescriptionList"
                "navigateToPharmacy" -> "Pharmacy"
                else -> ""
            }
        }
        
        return ""
    }
    
    /**
     * 提取导航目标
     */
    private fun extractNavigationTargets(content: String): List<NavigationTarget> {
        val targets = mutableListOf<NavigationTarget>()
        
        // 查找所有navigationActions.navigateTo调用
        val navigatePattern = Pattern.compile(
            """navigationActions\.(navigateTo\w+)\(\)""",
            Pattern.MULTILINE
        )
        
        val matcher = navigatePattern.matcher(content)
        
        while (matcher.find()) {
            val navigateMethod = matcher.group(1) ?: continue
            
            val targetScreen = when (navigateMethod) {
                "navigateToDoctorList" -> "DoctorList"
                "navigateToPatientList" -> "PatientList"
                "navigateToAppointmentList" -> "AppointmentList"
                "navigateToBookAppointment" -> "BookAppointment"
                "navigateToPrescriptionList" -> "PrescriptionList"
                "navigateToPharmacy" -> "Pharmacy"
                else -> continue
            }
            
            targets.add(
                NavigationTarget(
                    type = NavigationTargetType.NAVIGATE,
                    target = targetScreen
                )
            )
        }
        
        return targets
    }
}

/**
 * 屏幕信息数据类
 */
data class ScreenInfo(
    val screenName: String,
    val route: String,
    val components: List<UIComponentInfo>,
    val navigationTargets: List<NavigationTarget>
)
