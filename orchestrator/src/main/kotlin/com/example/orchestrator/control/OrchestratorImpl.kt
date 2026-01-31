package com.example.orchestrator.control

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.orchestrator.executor.ExecutorClient
import com.example.orchestrator.model.OrchestratorStatus
import com.example.orchestrator.model.PlanningResult
import com.example.orchestrator.model.UserRequest
import com.example.orchestrator.overlay.OverlayManager
import com.example.orchestrator.overlay.OverlayService
import com.example.orchestrator.planner.PlannerClient
import com.example.orchestrator.state.RuntimeStateProvider

class OrchestratorImpl(
    private val context: Context,
    private val runtimeStateProvider: RuntimeStateProvider,
    private val plannerClient: PlannerClient,
    private val executorClient: ExecutorClient,
    private val overlayManager: OverlayManager
) : Orchestrator, OverlayService.OverlayServiceListener {
    
    private var currentStatus = OrchestratorStatus.IDLE
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isExecutionStopped = false
    
    init {
        overlayManager.setServiceListener(this)
        overlayManager.setPlannerClient(plannerClient)
        overlayManager.setExecutorClient(executorClient)
    }
    
    override fun startExecution(inputText: String) {
        if (currentStatus == OrchestratorStatus.EXECUTING || currentStatus == OrchestratorStatus.PLANNING) {
            return
        }
        
        isExecutionStopped = false
        updateStatus(OrchestratorStatus.PLANNING)
        
        // 在后台线程执行规划和执行操作
        Thread {
            try {
                Log.d("OrchestratorImpl", "开始执行任务，输入文本: $inputText")
                val currentPageId = runtimeStateProvider.getCurrentPageId()
                Log.d("OrchestratorImpl", "当前页面ID: $currentPageId")
                
                val currentPageIdNotNull = currentPageId ?: run {
                    Log.e("OrchestratorImpl", "获取不到当前页面ID，任务失败")
                    updateStatus(OrchestratorStatus.FAILED)
                    return@Thread
                }
                
                val userRequest = UserRequest(inputText, currentPageIdNotNull)
                Log.d("OrchestratorImpl", "创建用户请求: $userRequest")
                val planningResult = plannerClient.plan(userRequest)
                Log.d("OrchestratorImpl", "规划结果: $planningResult")
                
                if (isExecutionStopped) {
                    updateStatus(OrchestratorStatus.IDLE)
                    return@Thread
                }
                
                when (planningResult) {
                    is PlanningResult.Success -> {
                        Log.d("OrchestratorImpl", "规划成功，开始执行操作")
                        updateStatus(OrchestratorStatus.EXECUTING)
                        
                        for ((index, step) in planningResult.actionPath.steps.withIndex()) {
                            Log.d("OrchestratorImpl", "执行步骤 $index: $step")
                            if (isExecutionStopped) {
                                Log.d("OrchestratorImpl", "任务被停止")
                                updateStatus(OrchestratorStatus.IDLE)
                                return@Thread
                            }
                            
                            // 更新当前页面ID
                            mainHandler.post {
                                overlayManager.updatePageId()
                            }
                            
                            // 短暂延迟，确保页面状态更新
                            Thread.sleep(500)
                            
                            val currentPage = runtimeStateProvider.getCurrentPageId()
                            Log.d("OrchestratorImpl", "当前页面: $currentPage")
                            
                            val currentPageNotNull = currentPage ?: run {
                                Log.e("OrchestratorImpl", "获取不到当前页面ID，任务失败")
                                updateStatus(OrchestratorStatus.FAILED)
                                return@Thread
                            }
                            
                            Log.d("OrchestratorImpl", "执行操作: $step 在页面: $currentPageNotNull")
                            val executeResult = executorClient.execute(step, currentPageNotNull)
                            Log.d("OrchestratorImpl", "执行结果: $executeResult")
                            
                            if (executeResult is com.example.executor.result.ExecuteResult.Failed) {
                                Log.e("OrchestratorImpl", "执行失败: ${executeResult.reason}")
                                updateStatus(OrchestratorStatus.FAILED)
                                return@Thread
                            }
                            
                            // 执行后短暂延迟，确保页面导航完成
                            Thread.sleep(1000)
                        }
                        
                        Log.d("OrchestratorImpl", "任务执行完成")
                        updateStatus(OrchestratorStatus.COMPLETED)
                        // 完成后一段时间后回到 IDLE 状态
                        mainHandler.postDelayed(Runnable {
                            updateStatus(OrchestratorStatus.IDLE)
                        }, 2000)
                    }
                    is PlanningResult.Failed -> {
                        Log.e("OrchestratorImpl", "规划失败: ${planningResult.reason}")
                        updateStatus(OrchestratorStatus.FAILED)
                    }
                }
            } catch (e: Exception) {
                Log.e("OrchestratorImpl", "执行过程中发生异常", e)
                e.printStackTrace()
                updateStatus(OrchestratorStatus.FAILED)
            }
        }.start()
    }
    
    override fun stopExecution() {
        isExecutionStopped = true
        updateStatus(OrchestratorStatus.IDLE)
    }
    
    override fun getCurrentStatus(): OrchestratorStatus {
        return currentStatus
    }
    
    override fun onExecuteClicked(inputText: String) {
        startExecution(inputText)
    }
    
    override fun onStopClicked() {
        stopExecution()
    }
    
    private fun updateStatus(status: OrchestratorStatus) {
        currentStatus = status
        mainHandler.post {
            overlayManager.updatePageId()
            overlayManager.updateStatus(status)
        }
    }
}