package com.example.executor.planner

data class ActionStep(
    val actionId: Int,
    val componentId: String,
    val triggerType: TriggerType
)

enum class TriggerType {
    CLICK,
    LONG_CLICK,
    CHECKED_CHANGE,
    PROGRESS_CHANGE,
    TOUCH
}

data class ActionPath(
    val startPageId: String,
    val steps: List<ActionStep>
)