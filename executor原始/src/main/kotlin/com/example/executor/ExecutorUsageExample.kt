package com.example.executor

import com.example.executor.core.Executor
import com.example.executor.core.ExecutionResult
import com.example.executor.model.AppAutomationMap
import kotlinx.serialization.json.Json

/**
 * Example usage of the app-internal UI execution engine
 * 
 * This example demonstrates how to initialize and use the Executor within an Android app
 * to perform UI operations using AccessibilityService.
 * 
 * Usage:
 * 1. Enable the app's Accessibility Service in system settings
 * 2. Initialize the Executor with the app's automation map
 * 3. Call executeIntent() with the desired intent ID
 */
object ExecutorUsageExample {
    
    /**
     * Example: Initialize the executor with the app's automation map
     * 
     * This example uses the app's own automation map file: app_automation_map_fixed.json
     */
    fun initializeExecutor() {
        // Load the app's own automation map from assets
        // val mapJson = context.assets.open("app_automation_map_fixed.json").bufferedReader().use { it.readText() }
        // val appAutomationMap = Json.decodeFromString(mapJson)
        
        // Get the accessibility service instance
        // val accessibilityService = AccessibilityExecutorService.getInstance()
        
        // Create the executor
        // val executor = ExecutorFactory.create(appAutomationMap, accessibilityService.getTreeAdapter())
        
        // Use the executor to perform UI operations within the app
        // val result = executor.executeIntent("navigate_to_addeditdoctor")
        
        // Handle the result
        // when (result) {
        //     is ExecutionResult.Success -> println("Operation completed successfully")
        //     is ExecutionResult.Failure -> println("Operation failed: ${result.reason}")
        //     is ExecutionResult.Timeout -> println("Operation timed out")
        // }
    }
    
    /**
     * Example: Execute a UI operation
     * 
     * @param executor The initialized executor instance
     * @param intentId The ID of the intent to execute (from the app's automation map)
     * @return The execution result
     */
    fun executeUiOperation(executor: Executor, intentId: String): ExecutionResult {
        // Execute the intent
        return executor.executeIntent(intentId)
    }
    
    /**
     * Example: Common use cases
     */
    fun demonstrateUseCases(executor: Executor) {
        // Navigate to a screen
        val navigateResult = executor.executeIntent("navigate_to_addeditdoctor")
        println("Navigate result: $navigateResult")
        
        // Perform a button click
        val clickResult = executor.executeIntent("doctorlist_doctorlistbutton0_click")
        println("Click result: $clickResult")
        
        // Navigate back
        val backResult = executor.executeIntent("navigate_back_home")
        println("Back result: $backResult")
    }
}
