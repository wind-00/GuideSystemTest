package com.example.executor.intent

/**
 * ActionSpec sealed class representing different types of UI actions
 */
sealed class ActionSpec {
    data class Click(val componentId: String) : ActionSpec()
    data class Input(val componentId: String, val text: String) : ActionSpec()
    object NavigateBack : ActionSpec()
}
