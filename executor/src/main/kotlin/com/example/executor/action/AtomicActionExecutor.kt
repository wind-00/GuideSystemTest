package com.example.executor.action

import com.example.executor.planner.ActionStep
import com.example.executor.result.ExecuteFailReason

interface AtomicActionExecutor {
    fun execute(action: ActionStep): AtomicExecuteResult
}

sealed class AtomicExecuteResult {
    object Success : AtomicExecuteResult()
    data class Fail(val reason: ExecuteFailReason) : AtomicExecuteResult()
}