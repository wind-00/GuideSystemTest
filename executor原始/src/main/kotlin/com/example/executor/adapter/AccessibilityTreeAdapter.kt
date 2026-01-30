package com.example.executor.adapter

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.LinkedList

/**
 * AccessibilityTreeAdapter interface for interacting with the app's Accessibility Service
 * 
 * This is the execution channel for the app's internal UI execution engine.
 */
interface AccessibilityTreeAdapter {
    fun isPageActive(pageId: String): Boolean
    fun findNodeByComponentId(componentId: String): AccessibilityNodeInfo?
    fun findNodeByPosition(x: Int, y: Int, width: Int, height: Int): AccessibilityNodeInfo?
    fun findNodeByType(type: String): AccessibilityNodeInfo?
    fun performClick(node: AccessibilityNodeInfo): Boolean
    fun performInput(node: AccessibilityNodeInfo, text: String): Boolean
    fun performNavigateBack(): Boolean
}

/**
 * Default implementation of AccessibilityTreeAdapter for app-internal UI execution
 */
class DefaultAccessibilityTreeAdapter(private val accessibilityService: AccessibilityService) : AccessibilityTreeAdapter {
    private var rootNode: AccessibilityNodeInfo? = null
    
    override fun isPageActive(pageId: String): Boolean {
        // For simplicity, we'll check if any node with the pageId in its contentDescription exists
        val root = accessibilityService.rootInActiveWindow ?: return false
        return findNodeWithContentDescription(root, pageId) != null
    }
    
    override fun findNodeByComponentId(componentId: String): AccessibilityNodeInfo? {
        val root = accessibilityService.rootInActiveWindow ?: return null
        return findNodeWithContentDescription(root, componentId)
    }
    
    override fun findNodeByPosition(x: Int, y: Int, width: Int, height: Int): AccessibilityNodeInfo? {
        val root = accessibilityService.rootInActiveWindow ?: return null
        return findNodeAtPosition(root, x, y, width, height)
    }
    
    override fun findNodeByType(type: String): AccessibilityNodeInfo? {
        val root = accessibilityService.rootInActiveWindow ?: return null
        return findNodeWithType(root, type)
    }
    
    override fun performClick(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
    
    override fun performInput(node: AccessibilityNodeInfo, text: String): Boolean {
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }
    
    override fun performNavigateBack(): Boolean {
        return accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }
    
    private fun findNodeWithContentDescription(root: AccessibilityNodeInfo, contentDescription: String): AccessibilityNodeInfo? {
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)
        
        while (queue.isNotEmpty()) {
            val currentNode = queue.poll()!! // Since queue.isNotEmpty(), poll() won't return null
            
            // Check if current node matches
            if (currentNode.contentDescription?.contains(contentDescription) == true) {
                return currentNode
            }
            
            // Add children to queue
            for (i in 0 until currentNode.childCount) {
                val child = currentNode.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }
        
        return null
    }
    
    private fun findNodeAtPosition(root: AccessibilityNodeInfo, targetX: Int, targetY: Int, targetWidth: Int, targetHeight: Int): AccessibilityNodeInfo? {
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)
        
        while (queue.isNotEmpty()) {
            val currentNode = queue.poll()!!
            
            // Get current node bounds
            val bounds = android.graphics.Rect()
            currentNode.getBoundsInScreen(bounds)
            
            // Check if node is within the target area
            val isInXRange = bounds.left <= targetX + targetWidth / 2 && bounds.right >= targetX + targetWidth / 2
            val isInYRange = bounds.top <= targetY + targetHeight / 2 && bounds.bottom >= targetY + targetHeight / 2
            
            if (isInXRange && isInYRange) {
                return currentNode
            }
            
            // Add children to queue
            for (i in 0 until currentNode.childCount) {
                val child = currentNode.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }
        
        return null
    }
    
    private fun findNodeWithType(root: AccessibilityNodeInfo, type: String): AccessibilityNodeInfo? {
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(root)
        
        while (queue.isNotEmpty()) {
            val currentNode = queue.poll()!!
            
            // Check if current node matches the type
            if (currentNode.className?.contains(type) == true) {
                return currentNode
            }
            
            // Add children to queue
            for (i in 0 until currentNode.childCount) {
                val child = currentNode.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
        }
        
        return null
    }
    
    fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Update root node when window state changes
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            rootNode = accessibilityService.rootInActiveWindow
        }
    }
}
