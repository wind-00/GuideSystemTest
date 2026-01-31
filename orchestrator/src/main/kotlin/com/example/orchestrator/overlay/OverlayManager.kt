package com.example.orchestrator.overlay

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.executor.core.Executor
import com.example.executor.state.ActivityPageStateProvider
import com.example.orchestrator.executor.ExecutorClient
import com.example.orchestrator.model.OrchestratorStatus
import com.example.orchestrator.planner.PlannerClient
import com.example.orchestrator.state.RuntimeStateProvider

class OverlayManager(private val context: Context) : OverlayView.OverlayViewListener, ActivityPageStateProvider.ActivityChangeListener {
    private lateinit var overlayView: OverlayView
    private var runtimeStateProvider: RuntimeStateProvider? = null
    private var serviceListener: OverlayService.OverlayServiceListener? = null
    private var activityPageStateProvider: ActivityPageStateProvider? = null
    private var specialDialogManager: SpecialDialogManager? = null
    private var executor: Executor? = null
    private var executorClient: ExecutorClient? = null
    private var plannerClient: PlannerClient? = null
    
    fun setRuntimeStateProvider(provider: RuntimeStateProvider) {
        runtimeStateProvider = provider
    }
    
    fun setServiceListener(listener: OverlayService.OverlayServiceListener) {
        serviceListener = listener
    }
    
    /**
     * 设置执行器，用于特殊对话结束后执行路径
     */
    fun setExecutor(executor: Executor) {
        this.executor = executor
        // 只有当 overlayView 和 plannerClient 都初始化后，才创建 SpecialDialogManager
        if (::overlayView.isInitialized && plannerClient != null) {
            createSpecialDialogManager()
        }
        Log.d("OverlayManager", "设置执行器")
    }
    
    /**
     * 设置PlannerClient，用于特殊对话结束后生成路径
     */
    fun setPlannerClient(plannerClient: PlannerClient) {
        this.plannerClient = plannerClient
        // 只有当 overlayView、executor 和 executorClient 都初始化后，才创建 SpecialDialogManager
        if (::overlayView.isInitialized && executor != null && executorClient != null) {
            createSpecialDialogManager()
        }
        Log.d("OverlayManager", "设置PlannerClient")
    }
    
    /**
     * 设置ExecutorClient，用于特殊对话结束后执行路径
     */
    fun setExecutorClient(executorClient: ExecutorClient) {
        this.executorClient = executorClient
        // 只有当 overlayView、executor 和 plannerClient 都初始化后，才创建 SpecialDialogManager
        if (::overlayView.isInitialized && executor != null && plannerClient != null) {
            createSpecialDialogManager()
        }
        Log.d("OverlayManager", "设置ExecutorClient")
    }
    
    /**
     * 创建 SpecialDialogManager 实例
     */
    private fun createSpecialDialogManager() {
        if (executor != null && executorClient != null && plannerClient != null && runtimeStateProvider != null && ::overlayView.isInitialized) {
            specialDialogManager = SpecialDialogManager(context, overlayView, executor!!, plannerClient!!, executorClient!!, runtimeStateProvider!!)
            specialDialogManager?.setOverlayManager(this)
            Log.d("OverlayManager", "初始化SpecialDialogManager")
        }
    }
    
    /**
     * 设置ActivityPageStateProvider并注册监听器
     */
    fun setActivityPageStateProvider(provider: ActivityPageStateProvider) {
        activityPageStateProvider = provider
        provider.registerActivityChangeListener(this)
        Log.d("OverlayManager", "设置ActivityPageStateProvider并注册监听器")
    }
    
    fun init(): Boolean {
        try {
            overlayView = OverlayView(context, this)
            return overlayView.create()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    fun showOverlay(): Boolean {
        try {
            if (!::overlayView.isInitialized) {
                val initSuccess = init()
                if (!initSuccess) {
                    return false
                }
                // overlayView 初始化后，尝试创建 SpecialDialogManager
                createSpecialDialogManager()
            }
            val showSuccess = overlayView.show()
            if (showSuccess) {
                updatePageId()
                updateStatus(com.example.orchestrator.model.OrchestratorStatus.IDLE)
            }
            return showSuccess
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    fun hideOverlay() {
        if (::overlayView.isInitialized) {
            overlayView.hide()
        }
    }
    
    fun updatePageId() {
        Log.d("OverlayManager", "更新页面ID")
        if (::overlayView.isInitialized) {
            val pageId = runtimeStateProvider?.getCurrentPageId()
            Log.d("OverlayManager", "获取到的页面ID: $pageId")
            overlayView.updatePageId(pageId)
        } else {
            Log.e("OverlayManager", "overlayView未初始化")
        }
    }
    
    fun updateStatus(status: OrchestratorStatus) {
        Log.d("OverlayManager", "更新状态: $status")
        if (::overlayView.isInitialized) {
            overlayView.updateStatus(status)
        } else {
            Log.e("OverlayManager", "overlayView未初始化")
        }
    }
    
    override fun onExecuteClicked(inputText: String) {
        // 检查是否是特殊对话输入
        if (specialDialogManager?.handleInput(inputText) == true) {
            // 特殊对话处理中，不需要调用原始的serviceListener
            Log.d("OverlayManager", "特殊对话处理中")
        } else {
            // 正常输入，调用原始的serviceListener
            serviceListener?.onExecuteClicked(inputText)
        }
    }
    
    override fun onStopClicked() {
        serviceListener?.onStopClicked()
    }
    
    override fun onActivityChanged(activity: Activity) {
        Log.d("OverlayManager", "收到Activity变化通知: ${activity.javaClass.simpleName}")
        // 当Activity变化时，更新页面ID
        updatePageId()
    }
}
