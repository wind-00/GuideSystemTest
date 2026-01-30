package com.example.planner

import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    // 读取现有UI地图张量
    val jsonString = File("../../fsm_transition.json").readText()
    
    try {
        // 反序列化UI地图
        val uiMap = Json.decodeFromString<UiMapModel>(jsonString)
        
        // 创建Planner实例
        val planner = Planner(uiMap)
        
        // 测试用例1：从MainActivity跳转到ThirdActivity2
        val userGoal1 = UserGoal(
            targetVisibleText = "Go to Third2",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.BFS
        )
        
        val result1 = planner.plan(userGoal1)
        println("Test 1 - BFS from MainActivity to 'Go to Third2':")
        println("Success: ${result1.success}")
        println("Action Path: ${result1.actionPath}")
        println("Reason: ${result1.reason}")
        println()
        
        // 测试用例2：使用DFS搜索
        val userGoal2 = UserGoal(
            targetVisibleText = "Go to Third2",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.DFS
        )
        
        val result2 = planner.plan(userGoal2)
        println("Test 2 - DFS from MainActivity to 'Go to Third2':")
        println("Success: ${result2.success}")
        println("Action Path: ${result2.actionPath}")
        println("Reason: ${result2.reason}")
        println()
        
        // 测试用例3：目标不存在
        val userGoal3 = UserGoal(
            targetVisibleText = "NonExistentText",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.BFS
        )
        
        val result3 = planner.plan(userGoal3)
        println("Test 3 - Target doesn't exist:")
        println("Success: ${result3.success}")
        println("Action Path: ${result3.actionPath}")
        println("Reason: ${result3.reason}")
        println()
        
        // 测试用例4：起始页面不存在
        val userGoal4 = UserGoal(
            targetVisibleText = "Go to Third2",
            startPage = "NonExistentActivity",
            searchStrategy = SearchStrategy.BFS
        )
        
        val result4 = planner.plan(userGoal4)
        println("Test 4 - Start page doesn't exist:")
        println("Success: ${result4.success}")
        println("Action Path: ${result4.actionPath}")
        println("Reason: ${result4.reason}")
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    }
}