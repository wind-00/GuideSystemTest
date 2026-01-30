package com.example.maprecognizer.api

import android.content.Context
import com.example.maprecognizer.analyzer.CodeAnalyzer
import com.example.maprecognizer.data.AppAutomationMap
import com.example.maprecognizer.generator.MapGenerator
import com.example.maprecognizer.serializer.JsonSerializer

/**
 * 地图识别器API入口，对外提供地图生成的核心功能
 */
class MapRecognizer {
    
    /**
     * 配置类，用于配置地图识别器的行为
     */
    data class Config(
        /** 是否启用静态分析 */
        val enableStaticAnalysis: Boolean = true,
        /** 是否启用运行时检测 */
        val enableRuntimeDetection: Boolean = false,
        /** 是否输出JSON格式 */
        val enableJsonOutput: Boolean = true
    )
    
    private val config: Config
    private val codeAnalyzer: CodeAnalyzer
    private val mapGenerator: MapGenerator
    private val jsonSerializer: JsonSerializer
    
    /**
     * 构造函数
     */
    constructor(config: Config = Config()) {
        this.config = config
        this.codeAnalyzer = CodeAnalyzer(config.enableRuntimeDetection)
        this.mapGenerator = MapGenerator()
        this.jsonSerializer = JsonSerializer()
    }
    
    /**
     * 生成应用自动化地图
     * @param context 上下文（可选，用于运行时检测）
     * @return 应用自动化地图
     */
    fun generateAppAutomationMap(context: Context? = null): AppAutomationMap {
        // 1. 开始运行时检测（如果启用且提供了Context）
        if (config.enableRuntimeDetection) {
            codeAnalyzer.startRuntimeDetection(context)
        }
        
        // 2. 分析导航信息
        val navigationInfo = codeAnalyzer.analyzeNavigation()
        
        // 3. 分析UI组件信息
        val uiComponents = codeAnalyzer.analyzeUIComponents()
        
        // 4. 生成应用自动化地图
        val appAutomationMap = mapGenerator.generateAppAutomationMap(navigationInfo, uiComponents)
        
        // 5. 停止运行时检测
        if (config.enableRuntimeDetection) {
            codeAnalyzer.stopRuntimeDetection()
        }
        
        return appAutomationMap
    }
    
    /**
     * 生成应用自动化地图的JSON字符串
     * @param context 上下文（可选，用于运行时检测）
     * @return JSON字符串
     */
    fun generateAppAutomationMapJson(context: Context? = null): String {
        val map = generateAppAutomationMap(context)
        return jsonSerializer.toPrettyJson(map)
    }
    
    /**
     * 保存应用自动化地图到文件
     * @param map 应用自动化地图
     * @param filePath 文件路径
     */
    fun saveAppAutomationMap(map: AppAutomationMap, filePath: String) {
        val json = jsonSerializer.toJson(map)
        java.io.File(filePath).writeText(json)
    }
    
    /**
     * 从文件加载应用自动化地图
     * @param filePath 文件路径
     * @return 应用自动化地图
     */
    fun loadAppAutomationMap(filePath: String): AppAutomationMap {
        val json = java.io.File(filePath).readText()
        return jsonSerializer.fromJson(json)
    }
    
    /**
     * 分析单个页面
     * @param pagePath 页面路径
     * @return 页面分析结果
     */
    fun analyzePage(pagePath: String) {
        codeAnalyzer.analyzePage(pagePath)
    }
    
    /**
     * 分析单个组件
     * @param componentPath 组件路径
     * @return 组件分析结果
     */
    fun analyzeComponent(componentPath: String) {
        codeAnalyzer.analyzeComponent(componentPath)
    }
}
