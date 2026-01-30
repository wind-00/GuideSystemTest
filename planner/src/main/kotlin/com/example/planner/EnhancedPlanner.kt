package com.example.planner

import kotlinx.serialization.json.Json

class EnhancedPlanner(
    private val planner: Planner,
    private val aiAnalyzer: AIAnalyzer
) {
    /**
     * 从自然语言意图生成路径规划
     * @param naturalLanguageIntent 用户的自然语言意图
     * @param startPage 起始页面，默认为MainActivity
     * @param searchStrategy 搜索策略，默认为BFS
     * @return 路径规划结果
     */
    suspend fun planFromIntent(
        naturalLanguageIntent: String,
        startPage: String = "MainActivity",
        searchStrategy: SearchStrategy = SearchStrategy.BFS
    ): PlanResult {
        // 1. 分析用户意图，获取目标visibleText，传入起始页面
        val targetVisibleText = aiAnalyzer.analyzeIntent(naturalLanguageIntent, startPage)
        
        // 2. 创建UserGoal
        val userGoal = UserGoal(
            targetVisibleText = targetVisibleText,
            startPage = startPage,
            searchStrategy = searchStrategy
        )
        
        // 3. 调用现有Planner进行路径规划
        return planner.plan(userGoal)
    }
}

// 辅助函数：从JSON字符串创建EnhancedPlanner
fun createEnhancedPlannerFromJson(jsonString: String, apiKey: String): EnhancedPlanner {
    val uiMap = Json.decodeFromString<UiMapModel>(jsonString)
    val aiAnalyzer = AIAnalyzer(apiKey, uiMap)
    val planner = Planner(uiMap)
    return EnhancedPlanner(planner, aiAnalyzer)
}