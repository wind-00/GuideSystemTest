package com.example.executor.adapter

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * AccessibilityExecutorService is the app-internal accessibility service that acts as the execution channel
 * for the app's UI execution engine.
 * 
 * This service is enabled by the user in system settings and allows the app to perform UI operations
 * on its own foreground UI using Accessibility APIs.
 */
class AccessibilityExecutorService : AccessibilityService() {
    
    private lateinit var treeAdapter: DefaultAccessibilityTreeAdapter
    
    /**
     * Get the AccessibilityTreeAdapter instance for the UI execution engine
     */
    fun getTreeAdapter(): AccessibilityTreeAdapter {
        if (!::treeAdapter.isInitialized) {
            treeAdapter = DefaultAccessibilityTreeAdapter(this)
        }
        return treeAdapter
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        // Initialize the tree adapter when the service is connected
        treeAdapter = DefaultAccessibilityTreeAdapter(this)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            treeAdapter.onAccessibilityEvent(it)
        }
    }
    
    override fun onInterrupt() {
        // Handle service interruption
    }
}
