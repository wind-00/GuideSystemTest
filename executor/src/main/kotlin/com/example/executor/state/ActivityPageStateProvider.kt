package com.example.executor.state

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import java.lang.ref.WeakReference

class ActivityPageStateProvider(private val application: Application) : PageStateProvider {

    private var currentActivityRef: WeakReference<Activity>? = null
    private val activityChangeListeners = mutableListOf<ActivityChangeListener>()

    interface ActivityChangeListener {
        fun onActivityChanged(activity: Activity)
    }

    init {
        Log.d("ActivityPageStateProvider", "初始化并注册生命周期回调")
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.d("ActivityPageStateProvider", "Activity创建: ${activity.javaClass.simpleName}")
            }

            override fun onActivityStarted(activity: Activity) {
                Log.d("ActivityPageStateProvider", "Activity启动: ${activity.javaClass.simpleName}")
            }

            override fun onActivityResumed(activity: Activity) {
                Log.d("ActivityPageStateProvider", "Activity恢复: ${activity.javaClass.simpleName}")
                val previousActivity = currentActivityRef?.get()
                currentActivityRef = WeakReference(activity)
                Log.d("ActivityPageStateProvider", "当前Activity更新为: ${activity.javaClass.simpleName}")
                
                // 如果当前Activity发生变化，通知所有监听器
                if (previousActivity?.javaClass?.simpleName != activity.javaClass.simpleName) {
                    Log.d("ActivityPageStateProvider", "Activity发生变化，通知监听器: ${activity.javaClass.simpleName}")
                    notifyActivityChanged(activity)
                }
            }

            override fun onActivityPaused(activity: Activity) {
                Log.d("ActivityPageStateProvider", "Activity暂停: ${activity.javaClass.simpleName}")
                // 不要在onPause时设置为null，因为此时可能还有其他Activity在前台
                // if (currentActivityRef?.get() == activity) {
                //     currentActivityRef = null
                // }
            }

            override fun onActivityStopped(activity: Activity) {
                Log.d("ActivityPageStateProvider", "Activity停止: ${activity.javaClass.simpleName}")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // Do nothing
            }

            override fun onActivityDestroyed(activity: Activity) {
                Log.d("ActivityPageStateProvider", "Activity销毁: ${activity.javaClass.simpleName}")
                if (currentActivityRef?.get() == activity) {
                    currentActivityRef = null
                    Log.d("ActivityPageStateProvider", "当前Activity设置为null")
                }
            }
        })
    }

    /**
     * 注册Activity变化监听器
     */
    fun registerActivityChangeListener(listener: ActivityChangeListener) {
        if (!activityChangeListeners.contains(listener)) {
            activityChangeListeners.add(listener)
            Log.d("ActivityPageStateProvider", "注册Activity变化监听器，当前监听器数量: ${activityChangeListeners.size}")
        }
    }

    /**
     * 取消注册Activity变化监听器
     */
    fun unregisterActivityChangeListener(listener: ActivityChangeListener) {
        activityChangeListeners.remove(listener)
        Log.d("ActivityPageStateProvider", "取消注册Activity变化监听器，当前监听器数量: ${activityChangeListeners.size}")
    }

    /**
     * 通知所有监听器Activity发生变化
     */
    private fun notifyActivityChanged(activity: Activity) {
        for (listener in activityChangeListeners) {
            try {
                listener.onActivityChanged(activity)
            } catch (e: Exception) {
                Log.e("ActivityPageStateProvider", "通知监听器时出错: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * 手动设置当前Activity
     */
    fun setCurrentActivity(activity: Activity) {
        Log.d("ActivityPageStateProvider", "手动设置当前Activity: ${activity.javaClass.simpleName}")
        val previousActivity = currentActivityRef?.get()
        currentActivityRef = WeakReference(activity)
        
        // 如果当前Activity发生变化，通知所有监听器
        if (previousActivity?.javaClass?.simpleName != activity.javaClass.simpleName) {
            Log.d("ActivityPageStateProvider", "手动设置Activity发生变化，通知监听器: ${activity.javaClass.simpleName}")
            notifyActivityChanged(activity)
        }
    }

    override fun getCurrentPageId(): String? {
        val currentActivity = currentActivityRef?.get()
        val pageId = currentActivity?.javaClass?.simpleName
        Log.d("ActivityPageStateProvider", "获取当前页面ID: $pageId, 当前Activity: $currentActivity")
        return pageId
    }

    /**
     * 获取当前Activity
     */
    fun getCurrentActivity(): Activity? {
        val currentActivity = currentActivityRef?.get()
        Log.d("ActivityPageStateProvider", "获取当前Activity: $currentActivity")
        return currentActivity
    }
}