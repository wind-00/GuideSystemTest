package com.example.executor.adapter

import android.view.accessibility.AccessibilityNodeInfo

/**
 * AccessibilityNodeMatcher interface for matching AccessibilityNodeInfo objects
 */
interface AccessibilityNodeMatcher {
    fun matches(node: AccessibilityNodeInfo): Boolean
}

/**
 * ContentDescriptionMatcher for matching nodes by contentDescription
 */
class ContentDescriptionMatcher(private val contentDescription: String) : AccessibilityNodeMatcher {
    override fun matches(node: AccessibilityNodeInfo): Boolean {
        return node.contentDescription?.contains(contentDescription) == true
    }
}

/**
 * TextMatcher for matching nodes by text
 */
class TextMatcher(private val text: String) : AccessibilityNodeMatcher {
    override fun matches(node: AccessibilityNodeInfo): Boolean {
        return node.text?.contains(text) == true
    }
}

/**
 * ViewTypeMatcher for matching nodes by view type
 */
class ViewTypeMatcher(private val viewType: String) : AccessibilityNodeMatcher {
    override fun matches(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString() ?: return false
        return when (viewType) {
            "BUTTON" -> className.contains("Button", ignoreCase = true)
            "TEXT_VIEW" -> className.contains("TextView", ignoreCase = true)
            "EDIT_TEXT" -> className.contains("EditText", ignoreCase = true)
            else -> false
        }
    }
}

/**
 * EnabledMatcher for matching nodes by enabled state
 */
class EnabledMatcher(private val enabled: Boolean) : AccessibilityNodeMatcher {
    override fun matches(node: AccessibilityNodeInfo): Boolean {
        return node.isEnabled == enabled
    }
}

/**
 * CompositeMatcher for combining multiple matchers
 */
class CompositeMatcher(private val matchers: List<AccessibilityNodeMatcher>) : AccessibilityNodeMatcher {
    override fun matches(node: AccessibilityNodeInfo): Boolean {
        return matchers.all { it.matches(node) }
    }
}
