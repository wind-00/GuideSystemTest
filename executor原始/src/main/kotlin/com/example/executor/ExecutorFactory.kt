package com.example.executor

import android.content.Context
import com.example.executor.action.ActionRunner
import com.example.executor.action.DefaultActionRunner
import com.example.executor.adapter.AccessibilityTreeAdapter
import com.example.executor.core.Executor
import com.example.executor.core.DefaultExecutor
import com.example.executor.core.ExecutorConfig
import com.example.executor.intent.ActionResolver
import com.example.executor.intent.DefaultActionResolver
import com.example.executor.model.AppAutomationMap
import com.example.executor.state.DefaultStateDetector
import com.example.executor.state.StateDetector
import com.example.executor.util.FormulaEvaluator
import com.example.executor.verify.DefaultStateVerifier
import com.example.executor.verify.StateVerifier

/**
 * Factory class for creating Executor instances
 */
object ExecutorFactory {
    /**
     * Create a new Executor instance with the given parameters
     */
    fun create(
        map: AppAutomationMap,
        treeAdapter: AccessibilityTreeAdapter,
        context: Context,
        config: ExecutorConfig = ExecutorConfig()
    ): Executor {
        // Create components
        val stateDetector: StateDetector = DefaultStateDetector(map, treeAdapter)
        val actionResolver: ActionResolver = DefaultActionResolver(map)
        val formulaEvaluator = FormulaEvaluator(context.resources)
        val actionRunner: ActionRunner = DefaultActionRunner(treeAdapter, map, formulaEvaluator)
        val stateVerifier: StateVerifier = DefaultStateVerifier(stateDetector)
        
        // Create and return executor
        return DefaultExecutor(
            map = map,
            stateDetector = stateDetector,
            actionResolver = actionResolver,
            actionRunner = actionRunner,
            stateVerifier = stateVerifier,
            config = config
        )
    }
}
