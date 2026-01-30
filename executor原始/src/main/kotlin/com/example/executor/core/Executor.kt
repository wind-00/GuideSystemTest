package com.example.executor.core

import com.example.executor.action.ActionRunner
import com.example.executor.intent.ActionResolver
import com.example.executor.model.AppAutomationMap
import com.example.executor.model.Intent
import com.example.executor.state.StateDetector
import com.example.executor.verify.StateVerifier
import java.util.logging.Logger

/**
 * Executor interface for executing UI actions defined in the app's automation map
 * 
 * This is an internal app component that uses AccessibilityService to perform UI operations
 * on the current foreground UI, which may be the app itself.
 */
interface Executor {
    fun executeIntent(intentId: String): ExecutionResult
}

/**
 * Default implementation of Executor for app-internal UI execution
 * 
 * This executor uses AccessibilityService as its execution channel to interact with the app's UI.
 * It does not rely on external tools or frameworks, and operates entirely within the app's runtime.
 * 
 * Strictly follows "try first → verify later" execution logic:
 * - No semantic judgments about intent applicability
 * - Only physical executability checks
 * - State detection only for logging and verification
 */
class DefaultExecutor(
    private val map: AppAutomationMap,
    private val stateDetector: StateDetector,
    private val actionResolver: ActionResolver,
    private val actionRunner: ActionRunner,
    private val stateVerifier: StateVerifier,
    private val config: ExecutorConfig = ExecutorConfig()
) : Executor {
    
    private val logger = Logger.getLogger(DefaultExecutor::class.java.name)
    
    override fun executeIntent(intentId: String): ExecutionResult {
        logger.info("Executing intent: $intentId")
        
        try {
            // 1. Detect current state (only for logging and debugging)
            val currentState = stateDetector.detectCurrentState()
            logger.info("Current state: $currentState")
            
            // 2. Find intent in map
            val intent = map.intentModel.intents.find { it.intentId == intentId }
                ?: return ExecutionResult.Failure("Intent not found: $intentId")
            
            // 3. Resolve actions for intent (physical executability check happens here)
            val action = actionResolver.resolve(intentId)
            logger.info("Resolved action: $action")
            
            // 4. Execute actions ("try first" step)
            val actionResult = actionRunner.run(action)
            if (!actionResult) {
                return ExecutionResult.Failure("Failed to execute action: $action")
            }
            logger.info("Action executed successfully: $action")
            
            // 5. Wait for UI to stabilize
            Thread.sleep(config.uiStabilizationDelayMs)
            
            // 6. Verify expected state ("verify later" step)
            if (!stateVerifier.verify(intent.expectedNextStateIds)) {
                return ExecutionResult.Failure("State verification failed for intent: $intentId")
            }
            
            // 7. Return success result
            logger.info("Execution successful for intent: $intentId")
            return ExecutionResult.Success
        } catch (e: Exception) {
            logger.severe("Execution failed with exception: ${e.message}")
            return ExecutionResult.Failure("Execution failed: ${e.message}")
        }
    }
}
