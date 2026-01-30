package com.example.maprecognizer.analyzer

import android.content.Context

/**
 * 代码分析器，整合所有分析器，负责分析应用代码并提取信息
 */
class CodeAnalyzer(private val enableRuntimeDetection: Boolean = false) {
    
    private val navigationAnalyzer: NavigationAnalyzer = NavigationAnalyzer()
    private val uiComponentAnalyzer: UIComponentAnalyzer = UIComponentAnalyzer()
    private val runtimeDetector: RuntimeDetector = RuntimeDetector()
    
    /**
     * 开始运行时检测
     */
    fun startRuntimeDetection(context: Context?) {
        if (enableRuntimeDetection && context != null) {
            runtimeDetector.startDetection(context)
        }
    }
    
    /**
     * 停止运行时检测
     */
    fun stopRuntimeDetection() {
        if (enableRuntimeDetection) {
            runtimeDetector.stopDetection()
        }
    }
    
    /**
     * 分析导航信息
     */
    fun analyzeNavigation(projectPath: String = "."): List<NavigationInfo> {
        return navigationAnalyzer.analyzeNavigationFiles(projectPath)
    }
    
    /**
     * 分析UI组件信息
     */
    fun analyzeUIComponents(projectPath: String = "."): List<UIComponentInfo> {
        return uiComponentAnalyzer.analyzeUIComponentFiles(projectPath)
    }
    
    /**
     * 分析单个页面
     */
    fun analyzePage(pagePath: String, projectPath: String = "."): UIComponentInfo? {
        val uiComponents = uiComponentAnalyzer.analyzeUIComponentFiles(projectPath)
        return uiComponents.find { it.screenName == pagePath }
    }
    
    /**
     * 分析单个组件
     */
    fun analyzeComponent(componentPath: String, projectPath: String = "."): UIComponentInfo? {
        val uiComponents = uiComponentAnalyzer.analyzeUIComponentFiles(projectPath)
        return uiComponents.find { it.componentId == componentPath }
    }
    
    /**
     * 获取导航历史
     */
    fun getNavigationHistory(): List<NavigationEvent> {
        return runtimeDetector.getNavigationHistory()
    }
    
    /**
     * 获取运行时检测到的组件信息
     */
    fun getRuntimeComponents(): List<RuntimeComponentInfo> {
        return runtimeDetector.getRuntimeComponents()
    }
}
