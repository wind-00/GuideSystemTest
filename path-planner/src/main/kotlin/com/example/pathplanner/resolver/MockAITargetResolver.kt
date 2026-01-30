package com.example.pathplanner.resolver

import com.example.pathplanner.models.ComponentTarget
import com.example.pathplanner.models.StateTarget
import com.example.pathplanner.models.TargetSpec

/**
 * 模拟的AI目标解析器，用于测试和演示
 * 在实际应用中，这个类会调用真实的AI API
 */
class MockAITargetResolver : TargetResolver {
    override suspend fun resolve(userIntent: String): TargetSpec {
        // 简单的规则匹配，模拟AI解析
        val intentLower = userIntent.lowercase()
        
        return when {
            intentLower.contains("设置") || intentLower.contains("settings") -> {
                StateTarget("Settings", 0.9)
            }
            intentLower.contains("搜索") || intentLower.contains("search") -> {
                // 生成指向SearchResults状态的目标
                StateTarget("SearchResults", 0.85)
            }
            intentLower.contains("按钮") || intentLower.contains("button") -> {
                ComponentTarget(componentType = "Button", componentText = "点击", confidence = 0.8)
            }
            intentLower.contains("主界面") || intentLower.contains("main") -> {
                StateTarget("Main", 0.95)
            }
            intentLower.contains("详情") || intentLower.contains("detail") -> {
                StateTarget("Detail", 0.88)
            }
            else -> {
                // 默认返回主界面目标
                StateTarget("Main", 0.7)
            }
        }
    }
}