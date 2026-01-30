package com.example.orchestrator.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.example.orchestrator.model.OrchestratorStatus
import com.example.orchestrator.state.RuntimeStateProvider

class OverlayService : Service(), OverlayView.OverlayViewListener {
    private lateinit var overlayView: OverlayView
    private var runtimeStateProvider: RuntimeStateProvider? = null
    private var serviceListener: OverlayServiceListener? = null
    
    interface OverlayServiceListener {
        fun onExecuteClicked(inputText: String)
        fun onStopClicked()
    }
    
    fun setRuntimeStateProvider(provider: RuntimeStateProvider) {
        runtimeStateProvider = provider
    }
    
    fun setServiceListener(listener: OverlayServiceListener) {
        serviceListener = listener
    }
    
    override fun onCreate() {
        super.onCreate()
        try {
            // 检查Context是否可用
            getSystemService(Context.WINDOW_SERVICE)
            overlayView = OverlayView(this, this)
            overlayView.create()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 延迟初始化，避免崩溃
        try {
            // 检查Context是否可用
            getSystemService(Context.WINDOW_SERVICE)
            showOverlay()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
    }
    
    fun showOverlay() {
        // 确保overlayView已初始化
        if (!::overlayView.isInitialized) {
            // 检查Context是否可用
            try {
                // 尝试获取系统服务，检查Context是否有效
                getSystemService(Context.WINDOW_SERVICE)
                overlayView = OverlayView(this, this)
                overlayView.create()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                return
            }
        }
        overlayView.show()
        updatePageId()
        updateStatus(OrchestratorStatus.IDLE)
    }
    
    fun hideOverlay() {
        if (::overlayView.isInitialized) {
            overlayView.hide()
        }
    }
    
    fun updatePageId() {
        if (::overlayView.isInitialized) {
            val pageId = runtimeStateProvider?.getCurrentPageId()
            overlayView.updatePageId(pageId)
        }
    }
    
    fun updateStatus(status: OrchestratorStatus) {
        if (::overlayView.isInitialized) {
            overlayView.updateStatus(status)
        }
    }
    
    override fun onExecuteClicked(inputText: String) {
        serviceListener?.onExecuteClicked(inputText)
    }
    
    override fun onStopClicked() {
        serviceListener?.onStopClicked()
    }
}