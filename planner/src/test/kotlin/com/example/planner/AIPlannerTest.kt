package com.example.planner

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AIPlannerTest {
    private val apiKey = "17fb3e69b36f471ab107c7605797f8bf.wMPUTygxdEPkBQi3@"
    private lateinit var enhancedPlanner: EnhancedPlanner
    private lateinit var aiAnalyzer: AIAnalyzer
    private lateinit var uiMap: UiMapModel

    @Before
    fun setUp() {
        // 读取现有UI地图张量
        val jsonString = File("../fsm_transition.json").readText()
        uiMap = Json.decodeFromString<UiMapModel>(jsonString)
        
        // 简单的AI分析器实现，直接从可见文本列表中匹配目标
        this.aiAnalyzer = object : AIAnalyzer(apiKey, uiMap) {
            override suspend fun analyzeIntent(naturalLanguageIntent: String, startPage: String): String {
                println("Mock AI analyzing intent: $naturalLanguageIntent")
                println("Current start page: $startPage")
                
                // 匹配关键词
                val matchedText = when {
                    naturalLanguageIntent.contains("第二层级1") || naturalLanguageIntent.contains("第二层 级1") -> "跳转到第二层级1"
                    naturalLanguageIntent.contains("第二层级2") -> "跳转到第二层级2"
                    naturalLanguageIntent.contains("第三层级1") -> "跳转到第三层级1"
                    naturalLanguageIntent.contains("第三层级2") -> "跳转到第三层级2"
                    naturalLanguageIntent.contains("返回第一层级") -> {
                        // 对于返回第一层级的意图，直接匹配MainActivity的跳转
                        // 检查当前页面是否有直接返回MainActivity的按钮
                        val currentPageIdx = uiMap.page_index[startPage] ?: 0
                        val currentPageTransitions = uiMap.transition[currentPageIdx] ?: emptyMap()
                        val directBackAction = currentPageTransitions.entries.find { (_, toPages) -> 
                            toPages.contains(0) // 0是MainActivity的pageIdx
                        }
                        
                        if (directBackAction != null) {
                            // 如果有直接返回的按钮，使用其visibleText
                            val actionMeta = uiMap.action_metadata[directBackAction.key]
                            if (actionMeta != null && actionMeta.visibleText.isNotEmpty()) {
                                println("Mock AI found direct back action: ${actionMeta.visibleText}")
                                return actionMeta.visibleText
                            }
                        }
                        // 否则返回默认的返回第一层级文本
                        "返回第一层级"
                    }
                    naturalLanguageIntent.contains("查看已完成任务") -> "查看已完成任务"
                    naturalLanguageIntent.contains("创建新任务") -> "创建新任务"
                    naturalLanguageIntent.contains("删除任务") -> "删除任务"
                    naturalLanguageIntent.contains("编辑任务") -> "编辑任务"
                    naturalLanguageIntent.contains("查看待办任务") -> "查看待办任务"
                    naturalLanguageIntent.contains("搜索任务") -> "搜索任务"
                    else -> uiMap.visible_text_index.keys.firstOrNull() ?: ""
                }
                
                println("Mock AI returned target text: $matchedText")
                return matchedText
            }
        }
        
        // 创建Planner和增强版Planner
        val planner = Planner(uiMap)
        enhancedPlanner = EnhancedPlanner(planner, aiAnalyzer)
    }
    
    /**
     * 将actionId转换为详细的操作描述
     */
    private fun getActionDescription(actionId: Int): String {
        val actionMeta = uiMap.action_metadata[actionId]
        return if (actionMeta != null) {
            "$actionId (${actionMeta.visibleText} - ${actionMeta.componentId} - ${actionMeta.triggerType})"
        } else {
            "$actionId (Unknown action)"
        }
    }
    
    /**
     * 将actionPath转换为详细的操作描述列表
     */
    private fun getActionPathDescription(actionPath: List<Int>): String {
        return actionPath.joinToString(" -> ") { getActionDescription(it) }
    }

    @After
    fun tearDown() {
        // 关闭AI分析器的HTTP客户端
        aiAnalyzer.close()
    }

    @Test
    fun `test basic intent - go to second level 1`() = runBlocking {
        println("Test 1: Basic intent - go to second level 1")
        
        val intent = "我想跳转到第二层级1"
        val startPage = "MainActivity"
        val result = enhancedPlanner.planFromIntent(intent, startPage)
        
        assertTrue(result.success, "Should find path for basic intent")
        assertFalse(result.actionPath.isEmpty(), "Action path should not be empty")
        
        println("Start State: $startPage")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 1 completed, waiting...")
        delay(3000) // 等待3秒
    }

    @Test
    fun `test basic intent - go to second level 2`() = runBlocking {
        println("\nTest 2: Basic intent - go to second level 2")
        
        val intent = "我需要跳转到第二层级2"
        val startPage = "MainActivity"
        val result = enhancedPlanner.planFromIntent(intent, startPage)
        
        assertTrue(result.success, "Should find path for basic intent")
        assertFalse(result.actionPath.isEmpty(), "Action path should not be empty")
        
        println("Start State: $startPage")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 2 completed, waiting...")
        delay(3000) // 等待3秒
    }

    @Test
    fun `test complex intent - access third level function`() = runBlocking {
        println("\nTest 3: Complex intent - access third level function")
        
        val intent = "我需要访问第三个页面的功能"
        val startPage = "MainActivity"
        val result = enhancedPlanner.planFromIntent(intent, startPage)
        
        assertTrue(result.success, "Should find path for complex intent")
        assertFalse(result.actionPath.isEmpty(), "Action path should not be empty")
        
        println("Start State: $startPage")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 3 completed, waiting...")
        delay(3000) // 等待3秒
    }

    @Test
    fun `test BFS search strategy`() = runBlocking {
        println("\nTest 4: BFS search strategy")
        
        val intent = "跳转到第二层级1"
        val startPage = "MainActivity"
        val result = enhancedPlanner.planFromIntent(
            intent,
            startPage = startPage,
            searchStrategy = SearchStrategy.BFS
        )
        
        assertTrue(result.success, "BFS should find path")
        assertFalse(result.actionPath.isEmpty(), "Action path should not be empty")
        
        println("Start State: $startPage")
        println("Search Strategy: BFS")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 4 completed, waiting...")
        delay(3000) // 等待3秒
    }

    @Test
    fun `test DFS search strategy`() = runBlocking {
        println("\nTest 5: DFS search strategy")
        
        val intent = "跳转到第二层级1"
        val startPage = "MainActivity"
        val result = enhancedPlanner.planFromIntent(
            intent,
            startPage = startPage,
            searchStrategy = SearchStrategy.DFS
        )
        
        assertTrue(result.success, "DFS should find path")
        assertFalse(result.actionPath.isEmpty(), "Action path should not be empty")
        
        println("Start State: $startPage")
        println("Search Strategy: DFS")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 5 completed, waiting...")
        delay(3000) // 等待3秒
    }

    @Test
    fun `test different start page`() = runBlocking {
        println("\nTest 6: Different start page")
        
        val intent = "返回第一层级"
        val startPage = "SecondActivity"
        val result = enhancedPlanner.planFromIntent(
            intent,
            startPage = startPage
        )
        
        println("Start State: $startPage")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 6 completed, waiting...")
        delay(3000) // 等待3秒
    }

    @Test
    fun `test intent with different wording`() = runBlocking {
        println("\nTest 7: Intent with different wording")
        
        val intent = "我想点击跳转到第二层级1的按钮"
        val startPage = "MainActivity"
        val result = enhancedPlanner.planFromIntent(intent, startPage)
        
        assertTrue(result.success, "Should handle intent with different wording")
        assertFalse(result.actionPath.isEmpty(), "Action path should not be empty")
        
        println("Start State: $startPage")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 7 completed, waiting...")
        delay(3000) // 等待3秒
    }
    
    @Test
    fun `test from second level to third level`() = runBlocking {
        println("\nTest 8: From second level to third level")
        
        val intent = "我想跳转到第三层级1"
        val startPage = "SecondActivity2"
        val result = enhancedPlanner.planFromIntent(
            intent,
            startPage = startPage
        )
        
        println("Start State: $startPage")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 8 completed, waiting...")
        delay(3000) // 等待3秒
    }
    
    @Test
    fun `test from third level to second level`() = runBlocking {
        println("\nTest 9: From third level to second level")
        
        val intent = "我想返回第二层级"
        val startPage = "ThirdActivity"
        val result = enhancedPlanner.planFromIntent(
            intent,
            startPage = startPage
        )
        
        println("Start State: $startPage")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 9 completed, waiting...")
        delay(3000) // 等待3秒
    }
    
    @Test
    fun `test cross level jump from main to third`() = runBlocking {
        println("\nTest 10: Cross level jump from main to third")
        
        val intent = "我想直接跳转到第三层级2"
        val startPage = "MainActivity"
        val result = enhancedPlanner.planFromIntent(intent, startPage)
        
        println("Start State: $startPage")
        println("Intent: $intent")
        println("Result: ${result.success}")
        println("Action Path:")
        result.actionPath.forEachIndexed { index, actionId ->
            println("  ${index+1}. ${getActionDescription(actionId)}")
        }
        println("Action Path Summary: ${getActionPathDescription(result.actionPath)}")
        println("Reason: ${result.reason}")
        println("Test 10 completed, waiting...")
        delay(3000) // 等待3秒
    }
}
