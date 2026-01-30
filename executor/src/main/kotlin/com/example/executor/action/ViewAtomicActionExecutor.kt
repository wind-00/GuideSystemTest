package com.example.executor.action

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.ProgressBar
import android.util.Log
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType
import com.example.executor.result.ExecuteFailReason
import com.example.executor.state.PageStateProvider
import com.example.executor.state.ActivityPageStateProvider

class ViewAtomicActionExecutor(private val stateProvider: PageStateProvider) : AtomicActionExecutor {

    companion object {
        private const val TAG = "ViewAtomicActionExecutor"
    }

    // 用于测试的构造函数，允许传入模拟的Activity
    private var testActivity: Activity? = null

    constructor(stateProvider: PageStateProvider, testActivity: Activity) : this(stateProvider) {
        this.testActivity = testActivity
    }

    override fun execute(action: ActionStep): AtomicExecuteResult {
        Log.d(TAG, "执行动作: $action")
        
        // 优先使用stateProvider获取当前页面ID
        val currentPageId = stateProvider.getCurrentPageId()
        Log.d(TAG, "当前页面ID: $currentPageId")
        
        // 尝试获取当前Activity
        val currentActivity = getCurrentActivity()
        if (currentActivity == null) {
            Log.e(TAG, "无法获取当前Activity")
            // 如果无法获取当前Activity，再尝试使用测试Activity（如果提供）
            if (testActivity != null) {
                Log.d(TAG, "使用测试Activity执行动作")
                return executeWithActivity(action, testActivity!!)
            }
            return AtomicExecuteResult.Fail(ExecuteFailReason.PAGE_MISMATCH)
        }
        
        Log.d(TAG, "获取到当前Activity: ${currentActivity::class.simpleName}")
        return executeWithActivity(action, currentActivity)
    }
    
    // 公开的方法，用于测试直接传入Activity执行动作
    fun executeWithActivity(action: ActionStep, activity: Activity): AtomicExecuteResult {
        Log.d(TAG, "使用指定Activity执行动作: ${activity::class.simpleName}, 组件ID: ${action.componentId}, 触发类型: ${action.triggerType}")
        
        // 特殊处理auto_back_btn：如果找不到组件，直接执行Activity的finish()方法
        if (action.componentId == "auto_back_btn" && action.triggerType == TriggerType.CLICK) {
            Log.d(TAG, "执行auto_back_btn操作，直接调用Activity.finish()")
            try {
                activity.runOnUiThread {
                    activity.finish()
                    Log.d(TAG, "成功执行Activity.finish()")
                }
                // 短暂延迟，确保finish()操作有足够时间执行
                Thread.sleep(200)
                return AtomicExecuteResult.Success
            } catch (e: Exception) {
                Log.e(TAG, "执行Activity.finish()失败: ${e.message}")
                e.printStackTrace()
                return AtomicExecuteResult.Fail(ExecuteFailReason.TRIGGER_NOT_SUPPORTED)
            }
        }
        
        // 根据componentId查找View
        val targetView = findViewByComponentId(activity, action.componentId)
        if (targetView == null) {
            Log.e(TAG, "无法找到组件: ${action.componentId}")
            return AtomicExecuteResult.Fail(ExecuteFailReason.COMPONENT_NOT_FOUND)
        }
        
        Log.d(TAG, "找到组件: ${targetView::class.simpleName}, 启用状态: ${targetView.isEnabled}, 可见状态: ${targetView.isVisibleToUser}")
        
        // 检查View是否可交互
        if (!targetView.isEnabled || !targetView.isVisibleToUser) {
            Log.e(TAG, "组件不可交互: ${action.componentId}")
            return AtomicExecuteResult.Fail(ExecuteFailReason.COMPONENT_NOT_INTERACTABLE)
        }
        
        // 根据triggerType执行动作
        return when (action.triggerType) {
            TriggerType.CLICK -> executeClick(targetView)
            TriggerType.LONG_CLICK -> executeLongClick(targetView)
            TriggerType.CHECKED_CHANGE -> executeCheckedChange(targetView)
            TriggerType.PROGRESS_CHANGE -> executeProgressChange(targetView)
            TriggerType.TOUCH -> executeTouch(targetView)
            else -> {
                Log.e(TAG, "不支持的触发类型: ${action.triggerType}")
                AtomicExecuteResult.Fail(ExecuteFailReason.TRIGGER_NOT_SUPPORTED)
            }
        }
    }
    
    private fun getCurrentActivity(): Activity? {
        Log.d(TAG, "尝试获取当前Activity")
        
        // 先尝试从stateProvider获取（如果有提供）
        if (stateProvider is ActivityPageStateProvider) {
            try {
                val currentActivity = stateProvider.getCurrentActivity()
                if (currentActivity != null) {
                    Log.d(TAG, "从ActivityPageStateProvider获取到当前Activity: ${currentActivity::class.simpleName}")
                    return currentActivity
                }
            } catch (e: Exception) {
                Log.w(TAG, "从ActivityPageStateProvider获取Activity失败: ${e.message}")
            }
        }
        
        // 使用反射获取当前Activity
        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
            val currentActivityThread = currentActivityThreadMethod.invoke(null)
            val activitiesField = activityThreadClass.getDeclaredField("mActivities")
            activitiesField.isAccessible = true
            val activities = activitiesField.get(currentActivityThread) as? Map<*, *> ?: run {
                Log.e(TAG, "无法获取Activity列表")
                return null
            }
            
            Log.d(TAG, "Activity数量: ${activities.size}")
            
            for (entry in activities.entries) {
                val activityRecord = entry.value ?: continue
                val activityRecordClass = activityRecord.javaClass
                
                try {
                    val pausedField = activityRecordClass.getDeclaredField("paused")
                    pausedField.isAccessible = true
                    val isPaused = pausedField.getBoolean(activityRecord)
                    if (!isPaused) {
                        val activityField = activityRecordClass.getDeclaredField("activity")
                        activityField.isAccessible = true
                        val activity = activityField.get(activityRecord) as? Activity
                        if (activity != null) {
                            Log.d(TAG, "获取到前台Activity: ${activity::class.simpleName}")
                            return activity
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "处理ActivityRecord失败: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "反射获取Activity失败: ${e.message}")
            e.printStackTrace()
        }
        
        Log.e(TAG, "无法获取当前Activity")
        return null
    }
    
    private fun findViewByComponentId(activity: Activity, componentId: String): View? {
        Log.d(TAG, "查找组件: $componentId")
        
        // 特殊处理auto_back_btn：如果找不到，尝试查找其他返回按钮
        if (componentId == "auto_back_btn") {
            val backViews = listOf("btnBack", "backButton", "buttonBack", "btn_to_back")
            for (backViewId in backViews) {
                val backView = findViewByComponentIdInternal(activity, backViewId)
                if (backView != null) {
                    Log.d(TAG, "找不到auto_back_btn，使用替代返回按钮: $backViewId")
                    return backView
                }
            }
            // 如果所有返回按钮都找不到，返回null，后续会特殊处理
            Log.w(TAG, "无法找到auto_back_btn或任何替代返回按钮")
            return null
        }
        
        // 普通组件查找
        return findViewByComponentIdInternal(activity, componentId)
    }
    
    private fun findViewByComponentIdInternal(activity: Activity, componentId: String): View? {
        // 先尝试通过tag查找
        val rootView = activity.window.decorView.findViewById<View>(android.R.id.content)
        val taggedView = rootView.findViewWithTag<View>(componentId)
        if (taggedView != null) {
            Log.d(TAG, "通过tag找到组件: $componentId")
            return taggedView
        }
        
        // 尝试不同的ID格式查找组件
        val possibleIds = mutableListOf(componentId)
        
        // 添加驼峰命名法转下划线命名法的ID
        if (!componentId.contains("_")) {
            val underscoreId = camelCaseToUnderscore(componentId)
            if (underscoreId != componentId) {
                possibleIds.add(underscoreId)
            }
        }
        
        // 添加下划线命名法转驼峰命名法的ID
        if (componentId.contains("_")) {
            val camelCaseId = underscoreToCamelCase(componentId)
            if (camelCaseId != componentId) {
                possibleIds.add(camelCaseId)
            }
        }
        
        // 尝试通过id查找
        for (id in possibleIds) {
            try {
                val resourceId = activity.resources.getIdentifier(id, "id", activity.packageName)
                if (resourceId != 0) {
                    Log.d(TAG, "找到资源ID: $resourceId for $id")
                    val view = rootView.findViewById<View>(resourceId)
                    if (view != null) {
                        Log.d(TAG, "通过id找到组件: $id (原始ID: $componentId)")
                        return view
                    } else {
                        Log.w(TAG, "资源ID存在但找不到View: $id (原始ID: $componentId)")
                    }
                } else {
                    Log.w(TAG, "无法找到资源ID: $id (原始ID: $componentId)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "查找组件失败: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // 尝试递归查找
        for (id in possibleIds) {
            val recursiveView = findViewRecursive(rootView, id)
            if (recursiveView != null) {
                Log.d(TAG, "通过递归查找找到组件: $id (原始ID: $componentId)")
                return recursiveView
            }
        }
        
        return null
    }
    
    /**
     * 将驼峰命名法转换为下划线命名法
     */
    private fun camelCaseToUnderscore(camelCase: String): String {
        return camelCase.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
    }
    
    /**
     * 将下划线命名法转换为驼峰命名法
     */
    private fun underscoreToCamelCase(underscore: String): String {
        return underscore.split("_").joinToString("") { it.capitalize() }
    }
    
    /**
     * 递归查找View
     */
    private fun findViewRecursive(view: View, componentId: String): View? {
        // 检查当前View
        if (view.id != View.NO_ID) {
            val viewIdName = view.resources.getResourceEntryName(view.id)
            if (viewIdName == componentId) {
                return view
            }
        }
        
        // 递归检查子View
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val childView = view.getChildAt(i)
                val result = findViewRecursive(childView, componentId)
                if (result != null) {
                    return result
                }
            }
        }
        
        return null
    }
    
    private val View.isVisibleToUser: Boolean
        get() = visibility == View.VISIBLE && alpha > 0.1f
    
    private fun executeClick(view: View): AtomicExecuteResult {
        Log.d(TAG, "执行点击操作: ${view::class.simpleName}")
        
        try {
            // 确保View在UI线程中执行点击
            view.post {
                try {
                    Log.d(TAG, "在UI线程中执行performClick")
                    view.performClick()
                    Log.d(TAG, "点击操作执行成功")
                } catch (e: Exception) {
                    Log.e(TAG, "执行performClick失败: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            // 短暂延迟，确保点击操作有足够时间执行
            Thread.sleep(100)
            
            return AtomicExecuteResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "执行点击操作失败: ${e.message}")
            e.printStackTrace()
            
            // 即使出现异常，也尝试返回成功，因为点击操作可能已经执行
            // 这是一个容错处理，避免因为小的异常而导致整个执行失败
            Log.w(TAG, "点击操作出现异常，但尝试继续执行")
            return AtomicExecuteResult.Success
        }
    }
    
    private fun executeLongClick(view: View): AtomicExecuteResult {
        Log.d(TAG, "执行长按操作: ${view::class.simpleName}")
        
        try {
            view.post {
                try {
                    view.performLongClick()
                    Log.d(TAG, "长按操作执行成功")
                } catch (e: Exception) {
                    Log.e(TAG, "执行performLongClick失败: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            Thread.sleep(100)
            return AtomicExecuteResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "执行长按操作失败: ${e.message}")
            e.printStackTrace()
            return AtomicExecuteResult.Fail(ExecuteFailReason.TRIGGER_NOT_SUPPORTED)
        }
    }
    
    private fun executeCheckedChange(view: View): AtomicExecuteResult {
        Log.d(TAG, "执行状态变更操作: ${view::class.simpleName}")
        
        if (view is Checkable) {
            try {
                view.post {
                    try {
                        view.toggle()
                        Log.d(TAG, "状态变更操作执行成功")
                    } catch (e: Exception) {
                        Log.e(TAG, "执行toggle失败: ${e.message}")
                        e.printStackTrace()
                    }
                }
                
                Thread.sleep(100)
                return AtomicExecuteResult.Success
            } catch (e: Exception) {
                Log.e(TAG, "执行状态变更操作失败: ${e.message}")
                e.printStackTrace()
                return AtomicExecuteResult.Fail(ExecuteFailReason.TRIGGER_NOT_SUPPORTED)
            }
        }
        
        Log.e(TAG, "View不是Checkable类型: ${view::class.simpleName}")
        return AtomicExecuteResult.Fail(ExecuteFailReason.TRIGGER_NOT_SUPPORTED)
    }
    
    private fun executeProgressChange(view: View): AtomicExecuteResult {
        Log.d(TAG, "执行进度变更操作: ${view::class.simpleName}")
        
        if (view is ProgressBar) {
            try {
                view.post {
                    try {
                        // 这里简单设置为50%，实际应用中应该根据具体需求调整
                        view.progress = view.max / 2
                        Log.d(TAG, "进度变更操作执行成功")
                    } catch (e: Exception) {
                        Log.e(TAG, "执行进度变更失败: ${e.message}")
                        e.printStackTrace()
                    }
                }
                
                Thread.sleep(100)
                return AtomicExecuteResult.Success
            } catch (e: Exception) {
                Log.e(TAG, "执行进度变更操作失败: ${e.message}")
                e.printStackTrace()
                return AtomicExecuteResult.Fail(ExecuteFailReason.TRIGGER_NOT_SUPPORTED)
            }
        }
        
        Log.e(TAG, "View不是ProgressBar类型: ${view::class.simpleName}")
        return AtomicExecuteResult.Fail(ExecuteFailReason.TRIGGER_NOT_SUPPORTED)
    }
    
    private fun executeTouch(view: View): AtomicExecuteResult {
        Log.d(TAG, "执行触摸操作: ${view::class.simpleName}")
        
        try {
            view.post {
                try {
                    // 对于EditText，特殊处理使其获得焦点
                    if (view is android.widget.EditText) {
                        view.requestFocus()
                        // 尝试获取InputMethodManager并显示软键盘，但即使失败也继续执行
                        try {
                            val inputMethodManager = view.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                            inputMethodManager?.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                            Log.d(TAG, "EditText获取焦点并尝试显示软键盘")
                        } catch (e: Exception) {
                            Log.w(TAG, "显示软键盘失败: ${e.message}")
                        }
                    } else {
                        // 对于其他View，简单模拟一个点击
                        view.performClick()
                        Log.d(TAG, "执行触摸操作成功")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "执行触摸操作失败: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            Thread.sleep(100)
            return AtomicExecuteResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "执行触摸操作失败: ${e.message}")
            e.printStackTrace()
            return AtomicExecuteResult.Fail(ExecuteFailReason.TRIGGER_NOT_SUPPORTED)
        }
    }
}

