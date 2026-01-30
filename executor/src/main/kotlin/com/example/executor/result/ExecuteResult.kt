package com.example.executor.result

import com.example.executor.planner.ActionStep

sealed class ExecuteResult {
    object Success : ExecuteResult()
    data class Failed(
        val stepIndex: Int,
        val action: ActionStep,
        val reason: ExecuteFailReason
    ) : ExecuteResult()
}

enum class ExecuteFailReason {
    PAGE_MISMATCH,
    COMPONENT_NOT_FOUND,
    COMPONENT_NOT_INTERACTABLE,
    TRIGGER_NOT_SUPPORTED,
    PAGE_NOT_CHANGED,
    TIMEOUT
}