package com.example.orchestrator.planner

import android.content.Context
import com.example.orchestrator.model.PlanningResult
import com.example.orchestrator.model.UserRequest

interface PlannerClient {
    fun plan(request: UserRequest): PlanningResult
    fun setContext(context: Context)
}