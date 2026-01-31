package com.example.orchestrator.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.orchestrator.R
import com.example.orchestrator.model.OrchestratorStatus

class OverlayView(private val context: Context, private val listener: OverlayViewListener) {
    private var windowManager: WindowManager? = null
    private var layoutInflater: LayoutInflater? = null
    private var view: View? = null
    private var windowParams: WindowManager.LayoutParams? = null
    
    // 移除init块，改为在create()方法中初始化
    
    private lateinit var pageIdTextView: TextView
    private lateinit var inputEditText: EditText
    private lateinit var executeButton: Button
    private lateinit var stopButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var toggleButton: Button
    
    private var isViewAdded = false
    private var isExpanded = true
    private var x = 0
    private var y = 0
    private var initialX = 0
    private var initialY = 0
    private val mainHandler = Handler(Looper.getMainLooper())
    
    interface OverlayViewListener {
        fun onExecuteClicked(inputText: String)
        fun onStopClicked()
    }
    
    fun create(): Boolean {
        try {
            // 初始化系统服务
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            
            if (windowManager == null || layoutInflater == null) {
                throw NullPointerException("无法获取系统服务")
            }
            
            view = layoutInflater!!.inflate(R.layout.overlay_view, null)
            
            pageIdTextView = view!!.findViewById(R.id.pageIdTextView)
            inputEditText = view!!.findViewById(R.id.inputEditText)
            executeButton = view!!.findViewById(R.id.executeButton)
            stopButton = view!!.findViewById(R.id.stopButton)
            statusTextView = view!!.findViewById(R.id.statusTextView)
            
            // 尝试找到收起/展开按钮，如果没有则创建
            try {
                toggleButton = view!!.findViewById(R.id.toggleButton)
            } catch (e: Exception) {
                // 如果布局中没有toggleButton，创建一个
                toggleButton = Button(context)
                toggleButton.text = "收起"
                toggleButton.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                (view as ViewGroup).addView(toggleButton)
            }
            
            // 设置按钮文字为中文
            executeButton.text = "执行"
            stopButton.text = "停止"
            toggleButton.text = "收起"
            inputEditText.hint = "请输入目标..."
            
            executeButton.setOnClickListener {
                listener.onExecuteClicked(inputEditText.text.toString())
            }
            
            stopButton.setOnClickListener {
                listener.onStopClicked()
            }
            
            toggleButton.setOnClickListener {
                toggleExpanded()
            }
            
            view!!.setOnTouchListener {
                _: View, event: MotionEvent ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = windowParams?.x ?: 0
                        y = windowParams?.y ?: 0
                        initialX = event.rawX.toInt()
                        initialY = event.rawY.toInt()
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val params = windowParams ?: return@setOnTouchListener false
                        params.x = x + (event.rawX.toInt() - initialX)
                        params.y = y + (event.rawY.toInt() - initialY)
                        windowManager?.updateViewLayout(view, params)
                        true
                    }
                    else -> false
                }
            }
            
            windowParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
            
            windowParams!!.gravity = Gravity.TOP or Gravity.START
            windowParams!!.x = 0
            windowParams!!.y = 0
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    fun show(): Boolean {
        try {
            if (!isViewAdded && windowManager != null && view != null && windowParams != null) {
                windowManager!!.addView(view!!, windowParams!!)
                isViewAdded = true
                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    fun hide(): Boolean {
        try {
            if (isViewAdded && windowManager != null && view != null) {
                windowManager!!.removeView(view!!)
                isViewAdded = false
                return true
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    fun updatePageId(pageId: String?) {
        Log.d("OverlayView", "更新页面ID: $pageId")
        try {
            mainHandler.post {
                try {
                    if (::pageIdTextView.isInitialized) {
                        pageIdTextView.text = "当前页面: ${pageId ?: "未知"}"
                        Log.d("OverlayView", "页面ID更新成功: $pageId")
                    } else {
                        Log.e("OverlayView", "pageIdTextView未初始化")
                    }
                } catch (e: Exception) {
                    Log.e("OverlayView", "更新页面ID时出错: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e("OverlayView", "post到UI线程时出错: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun updateStatus(status: OrchestratorStatus) {
        Log.d("OverlayView", "更新状态: $status")
        try {
            mainHandler.post {
                try {
                    if (::statusTextView.isInitialized) {
                        val statusText = when (status) {
                            OrchestratorStatus.IDLE -> "空闲"
                            OrchestratorStatus.PLANNING -> "规划中"
                            OrchestratorStatus.EXECUTING -> "执行中"
                            OrchestratorStatus.COMPLETED -> "完成"
                            OrchestratorStatus.FAILED -> "失败"
                        }
                        statusTextView.text = "状态: $statusText"
                        Log.d("OverlayView", "状态更新成功: $statusText")
                    } else {
                        Log.e("OverlayView", "statusTextView未初始化")
                    }
                } catch (e: Exception) {
                    Log.e("OverlayView", "更新状态时出错: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e("OverlayView", "post到UI线程时出错: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 设置输入框文本
     */
    fun setInputText(text: String) {
        try {
            mainHandler.post {
                try {
                    if (::inputEditText.isInitialized) {
                        inputEditText.setText(text)
                    }
                } catch (e: Exception) {
                    Log.e("OverlayView", "设置输入框文本时出错: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e("OverlayView", "post到UI线程时出错: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 设置状态文本
     */
    fun setStatusText(text: String) {
        try {
            mainHandler.post {
                try {
                    if (::statusTextView.isInitialized) {
                        statusTextView.text = text
                    }
                } catch (e: Exception) {
                    Log.e("OverlayView", "设置状态文本时出错: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e("OverlayView", "post到UI线程时出错: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun isShowing(): Boolean {
        return isViewAdded
    }
    
    private fun toggleExpanded() {
        try {
            isExpanded = !isExpanded
            
            if (isExpanded) {
                // 展开状态：显示所有控件
                pageIdTextView.visibility = View.VISIBLE
                inputEditText.visibility = View.VISIBLE
                executeButton.visibility = View.VISIBLE
                stopButton.visibility = View.VISIBLE
                statusTextView.visibility = View.VISIBLE
                toggleButton.text = "收起"
            } else {
                // 收起状态：只显示状态和收起按钮
                pageIdTextView.visibility = View.GONE
                inputEditText.visibility = View.GONE
                executeButton.visibility = View.GONE
                stopButton.visibility = View.GONE
                statusTextView.visibility = View.GONE
                toggleButton.text = "展开"
            }
            
            // 更新窗口大小
            if (windowManager != null && view != null && windowParams != null) {
                windowManager!!.updateViewLayout(view!!, windowParams!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}