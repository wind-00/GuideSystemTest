package com.example.orchestrator.executor

import com.example.executor.core.Executor
import com.example.executor.planner.ActionPath
import com.example.executor.planner.ActionStep
import com.example.executor.result.ExecuteResult

class ExecutorClientImpl(private val executor: Executor) : ExecutorClient {
    override fun execute(step: ActionStep, currentPageId: String): ExecuteResult {
        // 创建一个只包含当前步骤的 ActionPath
        val actionPath = ActionPath(
            startPageId = currentPageId,
            steps = listOf(step)
        )
        
        // 调用 Executor 执行动作路径
        return executor.execute(actionPath)
    }
}