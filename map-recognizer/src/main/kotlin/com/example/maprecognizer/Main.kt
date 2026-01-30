package com.example.maprecognizer

import com.example.maprecognizer.adapter.PathPlannerMapAdapter
import com.example.maprecognizer.analyzer.ViewBindingAnalyzer
import com.example.maprecognizer.data.AppAutomationMap
import com.example.maprecognizer.generator.MapGenerator
import com.example.maprecognizer.serializer.JsonSerializer
import java.io.File

/**
 * 地图生成器主类，用于从命令行生成应用自动化地图
 */
fun main(args: Array<String>) {
    println("=== 开始生成应用自动化地图 ===")
    
    try {
        // 获取输出路径
        val outputPath = System.getProperty("map.output.file", "app_automation_map_from_module.json")
        val rootDir = System.getProperty("project.root.dir", ".")
        
        println("输出路径: $outputPath")
        println("项目根目录: $rootDir")
        
        // 创建ViewBinding分析器
        println("\n1. 初始化ViewBinding分析器...")
        val codeAnalyzer = ViewBindingAnalyzer()
        
        // 分析ViewBinding代码
        println("\n2. 分析ViewBinding代码...")
        val screenInfos = codeAnalyzer.analyzeProject(rootDir)
        println("   已分析到 ${screenInfos.size} 个页面")
        
        // 生成地图
        println("\n3. 生成应用自动化地图...")
        val mapGenerator = MapGenerator()
        val appAutomationMap = mapGenerator.generateAppAutomationMap(
            screenInfos,
            "GuideSystemTest",
            "com.example.guidesystemtest"
        )
        
        // 使用序列化器保存到文件
        println("\n4. 序列化地图数据...")
        val serializer = JsonSerializer()
        val json = serializer.toJson(appAutomationMap)
        
        // 保存到文件
        println("\n5. 保存地图到文件...")
        val file = File(outputPath)
        
        // 确保目录存在
        val parentDir = file.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }
        
        file.writeText(json)
        
        // 生成path-planner格式的地图
        println("\n6. 生成path-planner格式地图...")
        val pathPlannerAdapter = PathPlannerMapAdapter()
        val pathPlannerJson = pathPlannerAdapter.convertToPathPlannerJson(appAutomationMap)
        
        // 保存path-planner格式地图
        val pathPlannerFile = File(outputPath.replace(".json", "_path_planner.json"))
        pathPlannerFile.writeText(pathPlannerJson)
        
        println("\n✅ 地图已成功生成并保存！")
        println("📄 输出文件: ${file.absolutePath}")
        println("📄 path-planner格式文件: ${pathPlannerFile.absolutePath}")
        println("📊 地图统计:")
        println("   页面数量: ${appAutomationMap.uiModel.pages.size}")
        println("   状态数量: ${appAutomationMap.stateModel.states.size}")
        println("   意图数量: ${appAutomationMap.intentModel.intents.size}")
        println("📄 文件大小: ${file.length()} 字节")
        println("📄 path-planner文件大小: ${pathPlannerFile.length()} 字节")
        
    } catch (e: Exception) {
        println("\n❌ 生成地图失败: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
    
    println("\n=== 地图生成完成 ===")
}