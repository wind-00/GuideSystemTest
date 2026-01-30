package com.example.executor.state

import com.example.executor.model.Signal

/**
 * StateMatcher interface for matching signals against current UI state
 */
interface StateMatcher {
    fun match(signal: Signal): Boolean
}

/**
 * Default implementation of StateMatcher
 */
class DefaultStateMatcher : StateMatcher {
    override fun match(signal: Signal): Boolean {
        // Implementation will be provided once AccessibilityTreeAdapter is available
        // For now, this is a placeholder
        return false
    }
}
