package com.example.planner

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 演示程序：测试GLM API调用和Planner集成
 */
fun main() = runBlocking {
    val apiKey = "17fb3e69b36f471ab107c7605797f8bf.wMPUTygxdEPkBQi3@"
    
    try {
        // 读取现有UI地图张量
        val jsonString = File("../fsm_transition.json").readText()
        val uiMap = Json.decodeFromString<UiMapModel>(jsonString)
        
        println("UI Map loaded successfully!")
        println("Available visible texts: ${uiMap.visible_text_index.keys}")
        
        // 创建AI分析器
        val aiAnalyzer = AIAnalyzer(apiKey, uiMap)
        
        // 测试AI分析器
        val testIntent = "我想跳转到第二层级1"
        println("\nTesting AI Analyzer with intent: $testIntent")
        
        val targetText = aiAnalyzer.analyzeIntent(testIntent, "MainActivity")
        println("AI Analyzer returned target text: $targetText")
        
        // 创建Planner和增强版Planner
        val planner = Planner(uiMap)
        val enhancedPlanner = EnhancedPlanner(planner, aiAnalyzer)
        
        // 测试增强版Planner
        println("\nTesting Enhanced Planner...")
        val result = enhancedPlanner.planFromIntent(testIntent)
        println("Plan Result: $result")
        
        aiAnalyzer.close()
        println("\nTest completed successfully!")
        
    } catch (e: Exception) {
        println("Error occurred:")
        e.printStackTrace()
    }
}