package com.example.orchestrator.executor

import com.example.executor.planner.ActionStep
import com.example.executor.result.ExecuteResult

interface ExecutorClient {
    fun execute(step: ActionStep, currentPageId: String): ExecuteResult
}
