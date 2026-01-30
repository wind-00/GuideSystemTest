package com.example.guidesystemtest

import android.app.Activity
import android.app.Application
import com.example.executor.core.ExecutionEngine
import com.example.executor.action.ViewAtomicActionExecutor
import com.example.executor.planner.ActionPath
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType
import com.example.executor.state.ActivityPageStateProvider
import com.example.executor.util.LooperUiStabilizer

object ExecutorTestUtils {

    /**
     * 创建测试用的执行引擎
     */
    fun createTestExecutor(application: Application): ExecutionEngine {
        val pageStateProvider = ActivityPageStateProvider(application)
        val atomicActionExecutor = ViewAtomicActionExecutor(pageStateProvider)
        val uiStabilizer = LooperUiStabilizer()
        
        return ExecutionEngine(
            pageStateProvider,
            atomicActionExecutor,
            uiStabilizer
        )
    }

    /**
     * 创建点击动作的ActionPath
     */
    fun createClickActionPath(startPageId: String, componentId: String): ActionPath {
        return ActionPath(
            startPageId = startPageId,
            steps = listOf(
                ActionStep(
                    actionId = 1,
                    componentId = componentId,
                    triggerType = TriggerType.CLICK
                )
            )
        )
    }

    /**
     * 创建长按动作的ActionPath
     */
    fun createLongClickActionPath(startPageId: String, componentId: String): ActionPath {
        return ActionPath(
            startPageId = startPageId,
            steps = listOf(
                ActionStep(
                    actionId = 2,
                    componentId = componentId,
                    triggerType = TriggerType.LONG_CLICK
                )
            )
        )
    }

    /**
     * 创建触摸动作的ActionPath
     */
    fun createTouchActionPath(startPageId: String, componentId: String): ActionPath {
        return ActionPath(
            startPageId = startPageId,
            steps = listOf(
                ActionStep(
                    actionId = 3,
                    componentId = componentId,
                    triggerType = TriggerType.TOUCH
                )
            )
        )
    }

    /**
     * 创建复选框状态改变动作的ActionPath
     */
    fun createCheckedChangeActionPath(startPageId: String, componentId: String): ActionPath {
        return ActionPath(
            startPageId = startPageId,
            steps = listOf(
                ActionStep(
                    actionId = 4,
                    componentId = componentId,
                    triggerType = TriggerType.CHECKED_CHANGE
                )
            )
        )
    }

    /**
     * 创建进度条改变动作的ActionPath
     */
    fun createProgressChangeActionPath(startPageId: String, componentId: String): ActionPath {
        return ActionPath(
            startPageId = startPageId,
            steps = listOf(
                ActionStep(
                    actionId = 5,
                    componentId = componentId,
                    triggerType = TriggerType.PROGRESS_CHANGE
                )
            )
        )
    }

    /**
     * 获取当前活动的Activity
     */
    fun getCurrentActivity(): Activity? {
        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
            val currentActivityThread = currentActivityThreadMethod.invoke(null)
            val activitiesField = activityThreadClass.getDeclaredField("mActivities")
            activitiesField.isAccessible = true
            val activities = activitiesField.get(currentActivityThread) as? Map<Any, Any> ?: emptyMap()
            for (activityRecord in activities.values) {
                val activityRecordClass = activityRecord?.javaClass ?: continue
                val pausedField = activityRecordClass.getDeclaredField("paused")
                pausedField.isAccessible = true
                val isPaused = pausedField.getBoolean(activityRecord)
                if (!isPaused) {
                    val activityField = activityRecordClass.getDeclaredField("activity")
                    activityField.isAccessible = true
                    return activityField.get(activityRecord) as? Activity
                }
            }
        } catch (e: Exception) {
            // 忽略异常
        }
        return null
    }

    /**
     * 生成唯一的ActionId
     */
    fun generateActionId(): Int {
        return System.currentTimeMillis().toInt()
    }

}
