package com.example.maprecognizer.analyzer

import java.io.File
import java.nio.file.Paths

/**
 * 导航分析器，从Compose导航图中提取页面和路由信息
 * 注意：只采集事实，不做推断
 */
class NavigationAnalyzer : Analyzer {
    
    override fun startAnalysis() {
        // 开始分析，初始化资源
    }
    
    override fun stopAnalysis() {
        // 停止分析，释放资源
    }
    
    /**
     * 分析导航文件，提取导航信息
     */
    fun analyzeNavigationFiles(projectPath: String): List<NavigationInfo> {
        val navigationFiles = findNavigationFiles(projectPath)
        val navigationInfoList = mutableListOf<NavigationInfo>()
        
        for (file in navigationFiles) {
            val info = analyzeSingleNavigationFile(file)
            navigationInfoList.addAll(info)
        }
        
        return navigationInfoList
    }

    // 查找项目中的导航文件
    private fun findNavigationFiles(projectPath: String): List<File> {
        val navigationFiles = mutableListOf<File>()
        val srcPath = Paths.get(projectPath, "app", "src", "main", "java").toFile()
        
        srcPath.walk().forEach { file ->
            if (file.isFile && file.extension == "kt" && 
                (file.name.contains("Navigation") || file.name.contains("navigation"))) {
                navigationFiles.add(file)
            }
        }
        
        return navigationFiles
    }

    // 分析单个导航文件
    private fun analyzeSingleNavigationFile(file: File): List<NavigationInfo> {
        val navigationInfoList = mutableListOf<NavigationInfo>()
        val content = file.readText()
        
        // 1. 提取页面路由（如Screen枚举）
        val screenRegex = Regex("""object\s+(\w+)\s*:\s+Screen\(["']([^"]+)["']\)""")
        val screenMatches = screenRegex.findAll(content)
        
        for (match in screenMatches) {
            val screenName = match.groupValues[1]
            val route = match.groupValues[2]
            
            // 2. 提取导航目标
            val navigationTargets = extractNavigationTargets(content, screenName)
            
            // 3. 构建NavigationInfo对象
            val navigationInfo = NavigationInfo(
                screenName = screenName,
                route = route,
                navigationTargets = navigationTargets
            )
            
            navigationInfoList.add(navigationInfo)
        }
        
        return navigationInfoList
    }
    
    /**
     * 从代码中提取导航目标
     */
    private fun extractNavigationTargets(code: String, screenName: String): List<NavigationTarget> {
        val navigationTargets = mutableListOf<NavigationTarget>()
        
        // 匹配 navigateToXXX() 格式
        val navigateRegex = Regex("""navigateTo($screenName|\w+)\(""")
        val navigateMatches = navigateRegex.findAll(code)
        navigateMatches.forEach { match ->
            val target = match.groupValues[1]
            navigationTargets.add(NavigationTarget(
                type = NavigationTargetType.NAVIGATE,
                target = target
            ))
        }
        
        // 匹配 navigate("XXX") 格式
        val navigateStringRegex = Regex("""navigate\("([^"]+)"\)""")
        val navigateStringMatches = navigateStringRegex.findAll(code)
        navigateStringMatches.forEach { match ->
            val target = match.groupValues[1]
            navigationTargets.add(NavigationTarget(
                type = NavigationTargetType.NAVIGATE,
                target = target
            ))
        }
        
        // 匹配 navigateUp() 格式
        val navigateUpRegex = Regex("""navigateUp\(""")
        if (navigateUpRegex.find(code) != null) {
            navigationTargets.add(NavigationTarget(
                type = NavigationTargetType.NAVIGATE_BACK,
                target = ""
            ))
        }
        
        // 匹配 popBackStack() 格式
        val popBackStackRegex = Regex("""popBackStack\(""")
        if (popBackStackRegex.find(code) != null) {
            navigationTargets.add(NavigationTarget(
                type = NavigationTargetType.POP_BACK_STACK,
                target = ""
            ))
        }
        
        return navigationTargets
    }
}
