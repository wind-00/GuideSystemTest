package com.example.orchestrator.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.orchestrator.R
import com.example.orchestrator.model.OrchestratorStatus

class OverlayView(private val context: Context, private val listener: OverlayViewListener) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var view: View
    private lateinit var windowParams: WindowManager.LayoutParams
    
    private lateinit var toggleButton: Button
    private lateinit var pageIdTextView: TextView
    private lateinit var inputEditText: EditText
    private lateinit var executeButton: Button
    private lateinit var stopButton: Button
    private lateinit var statusTextView: TextView
    
    private var isViewAdded = false
    private var isCollapsed = false
    private var x = 0
    private var y = 0
    private var initialX = 0
    private var initialY = 0
    
    interface OverlayViewListener {
        fun onExecuteClicked(inputText: String)
        fun onStopClicked()
    }
    
    fun create() {
        view = layoutInflater.inflate(R.layout.overlay_view, null)
        
        toggleButton = view.findViewById(R.id.toggleButton)
        pageIdTextView = view.findViewById(R.id.pageIdTextView)
        inputEditText = view.findViewById(R.id.inputEditText)
        executeButton = view.findViewById(R.id.executeButton)
        stopButton = view.findViewById(R.id.stopButton)
        statusTextView = view.findViewById(R.id.statusTextView)
        
        executeButton.setOnClickListener {
            listener.onExecuteClicked(inputEditText.text.toString())
        }
        
        stopButton.setOnClickListener {
            listener.onStopClicked()
        }
        
        toggleButton.setOnClickListener {
            toggleCollapse()
        }
        
        view.setOnTouchListener {
            _: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = windowParams.x
                    y = windowParams.y
                    initialX = event.rawX.toInt()
                    initialY = event.rawY.toInt()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    windowParams.x = x + (event.rawX.toInt() - initialX)
                    windowParams.y = y + (event.rawY.toInt() - initialY)
                    windowManager.updateViewLayout(view, windowParams)
                    true
                }
                else -> false
            }
        }
        
        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        
        windowParams.gravity = Gravity.TOP or Gravity.START
        windowParams.x = 0
        windowParams.y = 0
    }
    
    private fun toggleCollapse() {
        isCollapsed = !isCollapsed
        
        if (isCollapsed) {
            // 收起状态
            toggleButton.text = "展开"
            pageIdTextView.visibility = View.GONE
            inputEditText.visibility = View.GONE
            executeButton.visibility = View.GONE
            stopButton.visibility = View.GONE
            statusTextView.visibility = View.GONE
            
            // 更新窗口大小
            windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            windowManager.updateViewLayout(view, windowParams)
        } else {
            // 展开状态
            toggleButton.text = "收起"
            pageIdTextView.visibility = View.VISIBLE
            inputEditText.visibility = View.VISIBLE
            executeButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
            statusTextView.visibility = View.VISIBLE
            
            // 更新窗口大小
            windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            windowManager.updateViewLayout(view, windowParams)
        }
    }
    
    fun show() {
        if (!isViewAdded) {
            windowManager.addView(view, windowParams)
            isViewAdded = true
        }
    }
    
    fun hide() {
        if (isViewAdded) {
            windowManager.removeView(view)
            isViewAdded = false
        }
    }
    
    fun updatePageId(pageId: String?) {
        pageIdTextView.text = "当前页面: ${pageId ?: "未知"}"
    }
    
    fun updateStatus(status: OrchestratorStatus) {
        val statusText = when (status) {
            OrchestratorStatus.IDLE -> "空闲"
            OrchestratorStatus.PLANNING -> "规划中"
            OrchestratorStatus.EXECUTING -> "执行中"
            OrchestratorStatus.FAILED -> "失败"
            OrchestratorStatus.COMPLETED -> "完成"
        }
        statusTextView.text = "状态: $statusText"
    }
    
    fun isShowing(): Boolean {
        return isViewAdded
    }
}
