package com.example.executor.core

import android.util.Log
import com.example.executor.action.AtomicActionExecutor
import com.example.executor.planner.ActionPath
import com.example.executor.result.ExecuteFailReason
import com.example.executor.result.ExecuteResult
import com.example.executor.state.PageStateProvider
import com.example.executor.util.UiStabilizer

class ExecutionEngine(
    private val stateProvider: PageStateProvider,
    private val atomicActionExecutor: AtomicActionExecutor,
    private val uiStabilizer: UiStabilizer,
    private val stepTimeoutMs: Long = 5000 // 每个步骤的超时时间
) : Executor {

    override fun execute(actionPath: ActionPath): ExecuteResult {
        // 1. 校验起始页面
        val currentPageId = stateProvider.getCurrentPageId()
        if (currentPageId != actionPath.startPageId) {
            return ExecuteResult.Failed(
                stepIndex = 0,
                action = actionPath.steps.firstOrNull() ?: return ExecuteResult.Success,
                reason = ExecuteFailReason.PAGE_MISMATCH
            )
        }

        // 2. 顺序执行每个动作步骤
        for ((stepIndex, step) in actionPath.steps.withIndex()) {
            // 3. 执行原子动作（带超时检测）
            val atomicResult = kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.withTimeoutOrNull(stepTimeoutMs) {
                    atomicActionExecutor.execute(step)
                }
            }

            if (atomicResult == null) {
                return ExecuteResult.Failed(
                    stepIndex = stepIndex,
                    action = step,
                    reason = ExecuteFailReason.TIMEOUT
                )
            }

            if (atomicResult is com.example.executor.action.AtomicExecuteResult.Fail) {
                // 对于返回按钮操作，如果失败，尝试继续执行
                if (step.componentId == "auto_back_btn" || step.componentId.contains("back", ignoreCase = true)) {
                    Log.w("ExecutionEngine", "返回按钮执行失败，但尝试继续执行后续步骤")
                } else {
                    return ExecuteResult.Failed(
                        stepIndex = stepIndex,
                        action = step,
                        reason = atomicResult.reason
                    )
                }
            }

            // 4. 等待UI稳定（带超时检测）
            val stabilizationResult = kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.withTimeoutOrNull(stepTimeoutMs) {
                    uiStabilizer.waitForIdle()
                }
            }

            if (stabilizationResult != true) {
                // 对于返回按钮操作，如果超时，尝试继续执行
                if (step.componentId == "auto_back_btn" || step.componentId.contains("back", ignoreCase = true)) {
                    Log.w("ExecutionEngine", "返回按钮执行后UI稳定超时，但尝试继续执行后续步骤")
                } else {
                    return ExecuteResult.Failed(
                        stepIndex = stepIndex,
                        action = step,
                        reason = ExecuteFailReason.TIMEOUT
                    )
                }
            }

            // 5. 重新感知页面状态
            val afterPage = stateProvider.getCurrentPageId()
            Log.d("ExecutionEngine", "步骤 $stepIndex 执行后页面: $afterPage")
            
            // 6. 添加短暂延迟，确保页面有足够时间完成导航
            // 特别是对于页面跳转的情况，需要给系统足够时间加载新页面
            try {
                Thread.sleep(1000) // 增加延迟到1秒，确保页面导航完成，特别是返回操作
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            
            // 7. 再次感知页面状态，确保页面已经稳定
            val finalAfterPage = stateProvider.getCurrentPageId()
            Log.d("ExecutionEngine", "步骤 $stepIndex 最终页面: $finalAfterPage")
        }

        // 所有步骤执行成功
        return ExecuteResult.Success
    }
}