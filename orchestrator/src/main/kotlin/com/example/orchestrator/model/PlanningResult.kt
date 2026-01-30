package com.example.orchestrator.model

import com.example.executor.planner.ActionPath

sealed class PlanningResult {
    data class Success(val actionPath: ActionPath) : PlanningResult()
    data class Failed(val reason: String) : PlanningResult()
}