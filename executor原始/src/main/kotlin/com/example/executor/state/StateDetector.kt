package com.example.executor.state

import com.example.executor.adapter.AccessibilityTreeAdapter
import com.example.executor.model.AppAutomationMap
import com.example.executor.model.Signal

/**
 * StateDetector interface for detecting the current UI state
 * 
 * This is an internal component of the app's UI execution engine.
 */
interface StateDetector {
    fun detectCurrentState(): String?
}

/**
 * Default implementation of StateDetector for app-internal UI execution
 */
class DefaultStateDetector(
    private val map: AppAutomationMap,
    private val treeAdapter: AccessibilityTreeAdapter
) : StateDetector {
    override fun detectCurrentState(): String? {
        // Iterate through all states and check if their signals match
        for (state in map.stateModel.states) {
            if (matchSignals(state.signals)) {
                return state.stateId
            }
        }
        return null
    }
    
    private fun matchSignals(signals: List<Signal>): Boolean {
        // All signals must match for a state to be detected
        return signals.all { signal ->
            when (signal.type) {
                "PAGE_ACTIVE" -> {
                    // For PAGE_ACTIVE signals, check if the current page matches
                    val pageId = signal.target
                    val page = map.uiModel.pages.find { it.pageId == pageId }
                    page != null && treeAdapter.isPageActive(pageId)
                }
                // Add support for other signal types here
                else -> false
            }
        }
    }
}
