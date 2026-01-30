package com.example.planner

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 演示程序：展示改进后的测试输出格式
 */
fun main() = runBlocking {
    // 读取现有UI地图张量
    val jsonString = File("../fsm_transition.json").readText()
    val uiMap = Json.decodeFromString<UiMapModel>(jsonString)
    
    // 创建简单的AI分析器
    val apiKey = "17fb3e69b36f471ab107c7605797f8bf.wMPUTygxdEPkBQi3@"
    val aiAnalyzer = object : AIAnalyzer(apiKey, uiMap) {
        override suspend fun analyzeIntent(naturalLanguageIntent: String, startPage: String): String {
            println("Mock AI analyzing intent: $naturalLanguageIntent")
            println("Current start page: $startPage")
            
            // 简单的关键词匹配
            return when {
                naturalLanguageIntent.contains("第二层级1") -> "跳转到第二层级1"
                naturalLanguageIntent.contains("第二层级2") -> "跳转到第二层级2"
                else -> "跳转到第二层级1"
            }
        }
    }
    
    // 创建Planner和增强版Planner
    val planner = Planner(uiMap)
    val enhancedPlanner = EnhancedPlanner(planner, aiAnalyzer)
    
    // 测试用例：基本意图测试
    println("=== 演示改进后的测试输出格式 ===")
    
    val intent = "我想跳转到第二层级1"
    val startPage = "MainActivity"
    val result = enhancedPlanner.planFromIntent(intent, startPage)
    
    // 展示改进后的输出格式
    println("Test: Basic intent - go to second level 1")
    println("Start State: $startPage")
    println("Intent: $intent")
    println("Result: ${result.success}")
    println("Action Path:")
    
    // 将actionId转换为详细的操作描述
    result.actionPath.forEachIndexed { index, actionId ->
        val actionMeta = uiMap.action_metadata[actionId]
        val actionDesc = if (actionMeta != null) {
            "$actionId (${actionMeta.visibleText} - ${actionMeta.componentId} - ${actionMeta.triggerType})"
        } else {
            "$actionId (Unknown action)"
        }
        println("  ${index+1}. $actionDesc")
    }
    
    // 展示路径摘要
    val pathSummary = result.actionPath.joinToString(" -> ") { actionId ->
        val actionMeta = uiMap.action_metadata[actionId]
        if (actionMeta != null) {
            "${actionMeta.visibleText} (${actionId})"
        } else {
            "Unknown (${actionId})"
        }
    }
    println("Action Path Summary: $pathSummary")
    println("Reason: ${result.reason}")
    println("=== 演示结束 ===")
    
    // 关闭AI分析器
    aiAnalyzer.close()
}