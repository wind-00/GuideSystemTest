package com.example.pathplanner

import com.example.pathplanner.models.*
import com.example.pathplanner.resolver.GLMTargetResolverFactory
import kotlinx.coroutines.runBlocking

/**
 * 路径规划器演示类
 * 展示如何使用GLM API进行路径规划
 */
class PathPlannerDemo {
    
    /**
     * 演示使用GLM API进行路径规划
     */
    fun demo() {
        // 从用户提供的API密钥创建GLM目标解析器
        val apiKey = "17fb3e69b36f471ab107c7605797f8bf.wMPUTygxdEPkBQi3"
        val glmResolver = GLMTargetResolverFactory.create(apiKey)
        
        // 创建路径规划器
        val pathPlanner = PathPlanner(targetResolver = glmResolver)
        
        // 创建一个简单的测试UI地图
        val uiMap = createSimpleTestUIMap()
        
        // 要测试的用户意图列表
        val userIntents = listOf(
            "打开设置页面",
            "搜索测试内容",
            "返回主页面"
        )
        
        runBlocking {
            for (userIntent in userIntents) {
                println("\n=== 测试用户意图: $userIntent ===")
                
                try {
                    // 执行路径规划
                    val (result, _) = pathPlanner.plan(userIntent, uiMap)
                    
                    // 输出规划结果
                    println("目标规范: ${result.target}")
                    println("规划假设: ${result.assumptions}")
                    
                    if (result.plannedPath.isEmpty()) {
                        println("没有找到可行路径")
                    } else {
                        println("规划路径:")
                        for ((index, step) in result.plannedPath.withIndex()) {
                            println("  步骤 ${index + 1}:")
                            println("    意图: ${step.intent}")
                            println("    从状态: ${step.fromStateId}")
                            println("    期望状态: ${step.expectedStateId}")
                            println("    UI绑定: ${step.uiBinding}")
                        }
                    }
                } catch (e: PlannerException) {
                    println("规划失败: ${e.message}")
                    println("错误代码: ${e.errorCode}")
                    println("错误详情: ${e.details}")
                } catch (e: Exception) {
                    println("未知错误: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * 创建一个简单的测试UI地图
     */
    private fun createSimpleTestUIMap(): UIMap {
        // 创建组件
        val settingsButton = Component(
            componentId = "settings_button",
            type = "Button",
            text = "设置",
            intents = listOf(
                Intent(
                    intentId = "open_settings",
                    type = "CLICK",
                    targetStateId = "Settings",
                    description = "打开设置页面"
                )
            )
        )
        
        val searchBar = Component(
            componentId = "search_bar",
            type = "EditText",
            text = "搜索",
            intents = listOf(
                Intent(
                    intentId = "search",
                    type = "TEXT",
                    targetStateId = "SearchResults",
                    description = "执行搜索",
                    parameters = mapOf("text" to "")
                )
            )
        )
        
        val homeButton = Component(
            componentId = "home_button",
            type = "Button",
            text = "主页",
            intents = listOf(
                Intent(
                    intentId = "go_home",
                    type = "CLICK",
                    targetStateId = "Main",
                    description = "返回主页面"
                )
            )
        )
        
        // 创建状态
        val mainState = State(
            stateId = "Main",
            components = listOf(settingsButton, searchBar),
            intents = emptyList(),
            description = "主页面"
        )
        
        val settingsState = State(
            stateId = "Settings",
            components = listOf(homeButton),
            intents = emptyList(),
            description = "设置页面"
        )
        
        val searchResultsState = State(
            stateId = "SearchResults",
            components = listOf(homeButton),
            intents = emptyList(),
            description = "搜索结果页面"
        )
        
        // 创建UI地图
        return UIMap(
            states = mapOf(
                "Main" to mainState,
                "Settings" to settingsState,
                "SearchResults" to searchResultsState
            )
        )
    }
}

/**
 * 主函数，运行路径规划器演示
 */
fun main() {
    val demo = PathPlannerDemo()
    demo.demo()
}