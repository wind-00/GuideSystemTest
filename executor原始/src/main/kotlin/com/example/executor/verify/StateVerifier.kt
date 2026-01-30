package com.example.executor.verify

import com.example.executor.state.StateDetector

/**
 * StateVerifier interface for verifying if the current UI state matches expected states
 * 
 * This is an internal component of the app's UI execution engine.
 */
interface StateVerifier {
    fun verify(expectedStateIds: List<String>): Boolean
}

/**
 * Default implementation of StateVerifier for app-internal UI execution
 */
class DefaultStateVerifier(private val stateDetector: StateDetector) : StateVerifier {
    override fun verify(expectedStateIds: List<String>): Boolean {
        val currentState = stateDetector.detectCurrentState()
        return expectedStateIds.contains(currentState)
    }
}
