package com.example.executor.intent

import com.example.executor.model.AppAutomationMap

/**
 * ActionResolver interface for resolving intent IDs to action specifications
 * 
 * This is an internal component of the app's UI execution engine.
 */
interface ActionResolver {
    fun resolve(intentId: String): ActionSpec
}

/**
 * Default implementation of ActionResolver for app-internal UI execution
 */
class DefaultActionResolver(private val map: AppAutomationMap) : ActionResolver {
    override fun resolve(intentId: String): ActionSpec {
        // Find the intent in the model
        val intent = map.intentModel.intents.find { it.intentId == intentId }
            ?: throw IllegalArgumentException("No intent found for intentId: $intentId")
        
        // Resolve based on intent type and predefined mappings
        // This resolver strictly follows the mappings defined in the map
        // It does not generate, select, or replace intent IDs
        return when (intent.type) {
            "NAVIGATE" -> {
                // For navigation intents, use predefined component mappings
                val componentId = when (intentId) {
                    "navigate_to_detail" -> "GoDetailButton"
                    "navigate_back_home" -> "BackButton"
                    else -> throw IllegalArgumentException("Unknown navigation intent: $intentId")
                }
                ActionSpec.Click(componentId)
            }
            "CLICK" -> {
                // For click intents, extract componentId from intentId
                // Format: PageName_ComponentId_click
                val parts = intentId.split("_")
                if (parts.size < 3) {
                    throw IllegalArgumentException("Invalid click intent format: $intentId")
                }
                val componentId = parts[1]
                ActionSpec.Click(componentId)
            }
            "OTHER" -> {
                // For back navigation
                ActionSpec.NavigateBack
            }
            else -> throw IllegalArgumentException("Unsupported intent type: ${intent.type}")
        }
    }
}
