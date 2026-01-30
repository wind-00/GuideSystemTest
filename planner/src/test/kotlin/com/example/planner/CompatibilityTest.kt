package com.example.planner

import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompatibilityTest {
    @Test
    fun `test compatibility with existing fsm_transition json`() {
        // 读取现有UI地图张量
        val jsonString = File("../fsm_transition.json").readText()
        
        // 反序列化UI地图
        val uiMap = Json.decodeFromString<UiMapModel>(jsonString)
        
        // 创建Planner实例
        val planner = Planner(uiMap)
        
        // 测试用例1：从MainActivity跳转到SecondActivity
        val userGoal1 = UserGoal(
            targetVisibleText = "跳转到第二层级1",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.BFS
        )
        
        val result1 = planner.plan(userGoal1)
        assertTrue(result1.success, "BFS should find path from MainActivity to '跳转到第二层级1'")
        assertFalse(result1.actionPath.isEmpty(), "Action path should not be empty")
        
        // 测试用例2：使用DFS搜索
        val userGoal2 = UserGoal(
            targetVisibleText = "跳转到第二层级1",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.DFS
        )
        
        val result2 = planner.plan(userGoal2)
        assertTrue(result2.success, "DFS should find path from MainActivity to '跳转到第二层级1'")
        assertFalse(result2.actionPath.isEmpty(), "Action path should not be empty")
        
        // 测试用例3：目标不存在
        val userGoal3 = UserGoal(
            targetVisibleText = "NonExistentText",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.BFS
        )
        
        val result3 = planner.plan(userGoal3)
        assertFalse(result3.success, "Should return false for non-existent target")
        
        // 测试用例4：起始页面不存在
        val userGoal4 = UserGoal(
            targetVisibleText = "跳转到第二层级1",
            startPage = "NonExistentActivity",
            searchStrategy = SearchStrategy.BFS
        )
        
        val result4 = planner.plan(userGoal4)
        assertFalse(result4.success, "Should return false for non-existent start page")
    }
}