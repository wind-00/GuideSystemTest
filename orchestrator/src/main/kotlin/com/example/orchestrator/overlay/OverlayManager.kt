package com.example.orchestrator.overlay

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.executor.state.ActivityPageStateProvider
import com.example.orchestrator.model.OrchestratorStatus
import com.example.orchestrator.state.RuntimeStateProvider

class OverlayManager(private val context: Context) : OverlayView.OverlayViewListener, ActivityPageStateProvider.ActivityChangeListener {
    private lateinit var overlayView: OverlayView
    private var runtimeStateProvider: RuntimeStateProvider? = null
    private var serviceListener: OverlayService.OverlayServiceListener? = null
    private var activityPageStateProvider: ActivityPageStateProvider? = null
    
    fun setRuntimeStateProvider(provider: RuntimeStateProvider) {
        runtimeStateProvider = provider
    }
    
    fun setServiceListener(listener: OverlayService.OverlayServiceListener) {
        serviceListener = listener
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
        serviceListener?.onExecuteClicked(inputText)
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
