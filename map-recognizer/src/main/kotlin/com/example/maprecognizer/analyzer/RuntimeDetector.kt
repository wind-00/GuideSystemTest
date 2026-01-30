package com.example.maprecognizer.analyzer

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 运行时检测器，用于在应用运行时检测组件信息
 * 注意：仅用于补充componentId、校验页面是否存在、发现遗漏的UI组件
 */
class RuntimeDetector : Analyzer {
    
    private var context: Context? = null
    private var isDetecting: Boolean = false
    private val navigationHistory: MutableList<NavigationEvent> = mutableListOf()
    private val runtimeComponents: MutableList<RuntimeComponentInfo> = mutableListOf()
    
    /**
     * 开始检测
     */
    override fun startAnalysis() {
        isDetecting = true
    }
    
    /**
     * 停止检测
     */
    override fun stopAnalysis() {
        isDetecting = false
    }
    
    /**
     * 开始运行时检测
     */
    fun startDetection(context: Context) {
        this.context = context
        isDetecting = true
        // 注册AccessibilityService监听
        // 注册Activity生命周期监听
    }
    
    /**
     * 停止运行时检测
     */
    fun stopDetection() {
        isDetecting = false
        // 取消注册AccessibilityService监听
        // 取消注册Activity生命周期监听
    }
    
    /**
     * 获取导航历史
     */
    fun getNavigationHistory(): List<NavigationEvent> {
        return navigationHistory.toList()
    }
    
    /**
     * 获取运行时检测到的组件信息
     */
    fun getRuntimeComponents(): List<RuntimeComponentInfo> {
        return runtimeComponents.toList()
    }
    
    /**
     * 处理AccessibilityEvent
     */
    fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isDetecting || context == null) return
        
        // 分析事件类型
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // 提取组件信息
                val rootNode = event.source ?: return
                analyzeAccessibilityNode(rootNode)
                rootNode.recycle()
            }
        }
    }
    
    /**
     * 处理Activity生命周期事件
     */
    fun onActivityLifecycleChanged(event: LifecycleEvent) {
        if (!isDetecting) return
        
        // 记录当前Activity
        // 更新导航历史
        val navigationEvent = NavigationEvent(
            timestamp = System.currentTimeMillis(),
            fromPageId = if (navigationHistory.isNotEmpty()) navigationHistory.last().toPageId else "",
            toPageId = event.activityName,
            triggerComponentId = ""
        )
        navigationHistory.add(navigationEvent)
    }
    
    /**
     * 分析AccessibilityNodeInfo，提取组件信息
     */
    private fun analyzeAccessibilityNode(node: AccessibilityNodeInfo) {
        if (!isDetecting) return
        
        // 提取组件类型、属性和状态
        val componentType = node.className?.toString() ?: ""
        val text = node.text?.toString()
        
        // 构建RuntimeComponentInfo对象
        val runtimeComponent = RuntimeComponentInfo(
            componentId = "", // 后续可以从组件信息中提取或生成
            componentType = componentType,
            text = text
        )
        
        // 添加到运行时组件列表
        runtimeComponents.add(runtimeComponent)
        
        // 递归分析子节点
        for (i in 0 until node.childCount) {
            val childNode = node.getChild(i)
            if (childNode != null) {
                analyzeAccessibilityNode(childNode)
                childNode.recycle()
            }
        }
    }
    
    /**
     * 模拟添加导航事件
     */
    fun addNavigationEvent(event: NavigationEvent) {
        if (isDetecting) {
            navigationHistory.add(event)
        }
    }
    
    /**
     * 模拟添加运行时组件信息
     */
    fun addRuntimeComponent(component: RuntimeComponentInfo) {
        if (isDetecting) {
            runtimeComponents.add(component)
        }
    }
    

}
