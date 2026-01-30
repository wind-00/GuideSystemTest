package com.example.executor.action

import com.example.executor.adapter.AccessibilityTreeAdapter
import com.example.executor.intent.ActionSpec

/**
 * ActionRunner interface for running UI actions
 * 
 * This is an internal component of the app's UI execution engine.
 */
interface ActionRunner {
    fun run(action: ActionSpec): Boolean
}

/**
 * Default implementation of ActionRunner for app-internal UI execution
 */
class DefaultActionRunner(
    private val treeAdapter: AccessibilityTreeAdapter,
    private val map: com.example.executor.model.AppAutomationMap,
    private val formulaEvaluator: com.example.executor.util.FormulaEvaluator
) : ActionRunner {
    override fun run(action: ActionSpec): Boolean {
        return when (action) {
            is ActionSpec.Click -> runClick(action)
            is ActionSpec.Input -> runInput(action)
            is ActionSpec.NavigateBack -> runNavigateBack()
        }
    }
    
    private fun runClick(action: ActionSpec.Click): Boolean {
        val node = findComponentNode(action.componentId)
        return node != null && treeAdapter.performClick(node)
    }
    
    private fun runInput(action: ActionSpec.Input): Boolean {
        val node = findComponentNode(action.componentId)
        return node != null && treeAdapter.performInput(node, action.text)
    }
    
    private fun runNavigateBack(): Boolean {
        return treeAdapter.performNavigateBack()
    }
    
    /**
     * Finds a UI component node using multiple strategies:
     * 1. First by contentDescription
     * 2. Then by position formula (if available)
     * 3. Then by type (if contentDescription and position formula fail)
     */
    private fun findComponentNode(componentId: String): android.view.accessibility.AccessibilityNodeInfo? {
        // Find component in the map
        val component = map.uiModel.pages
            .flatMap { it.components }
            .find { it.componentId == componentId }
        
        if (component == null) {
            // Fallback to contentDescription lookup
            return treeAdapter.findNodeByComponentId(componentId)
        }
        
        // Strategy 1: Find by contentDescription
        val nodeByDesc = treeAdapter.findNodeByComponentId(componentId)
        if (nodeByDesc != null) {
            return nodeByDesc
        }
        
        // Strategy 2: Find by position formula (if available)
        if (component.positionFormula != null) {
            val calculatedPosition = formulaEvaluator.evaluatePosition(component.positionFormula)
            val calculatedSize = formulaEvaluator.evaluateSize(component.sizeFormula)
            
            if (calculatedPosition != null && calculatedSize != null) {
                val nodeByPosition = treeAdapter.findNodeByPosition(
                    calculatedPosition.x,
                    calculatedPosition.y,
                    calculatedSize.width,
                    calculatedSize.height
                )
                if (nodeByPosition != null) {
                    return nodeByPosition
                }
            }
        }
        
        // Strategy 3: Find by type (if available)
        if (component.viewType.isNotEmpty()) {
            return treeAdapter.findNodeByType(component.viewType)
        }
        
        return null
    }
}
