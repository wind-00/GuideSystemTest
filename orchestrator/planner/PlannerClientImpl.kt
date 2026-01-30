package com.example.orchestrator.planner

import com.example.orchestrator.model.PlanningResult
import com.example.orchestrator.model.UserRequest
import com.example.planner.PlanResult
import com.example.planner.Planner
import com.example.planner.SearchStrategy
import com.example.planner.UserGoal
import com.example.executor.planner.ActionPath
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType

class PlannerClientImpl(private val planner: Planner) : PlannerClient {
    override fun plan(request: UserRequest): PlanningResult {
        val userGoal = UserGoal(
            targetVisibleText = request.rawText,
            startPage = request.startPageId,
            searchStrategy = SearchStrategy.BFS
        )
        
        val planResult = planner.plan(userGoal)
        
        return if (planResult.success) {
            val actionSteps = planResult.actionPath.map {
                // 这里需要根据 actionId 获取对应的 ActionStep 信息
                // 暂时使用默认值，实际应用中需要从 Planner 的 UI 地图中获取
                ActionStep(
                    actionId = it,
                    componentId = "",
                    triggerType = TriggerType.CLICK
                )
            }
            
            PlanningResult.Success(
                ActionPath(
                    startPageId = request.startPage,
                    steps = actionSteps
                )
            )
        } else {
            PlanningResult.Failed(planResult.reason ?: "Unknown error")
        }
    }
}
