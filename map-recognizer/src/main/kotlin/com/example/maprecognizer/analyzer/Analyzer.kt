package com.example.maprecognizer.analyzer

/**
 * 分析器基础接口
 */
interface Analyzer {
    /**
     * 开始分析
     */
    fun startAnalysis()
    
    /**
     * 停止分析
     */
    fun stopAnalysis()
}

/**
 * 导航信息数据类
 */
data class NavigationInfo(
    /** 屏幕名称 */
    val screenName: String,
    /** 屏幕路由 */
    val route: String,
    /** 导航目标列表 */
    val navigationTargets: List<NavigationTarget>,
    /** 导航类型 */
    val navigationType: NavigationType = NavigationType.COMPOSE_NAVIGATION
)



/**
 * 导航类型枚举
 */
enum class NavigationType {
    /** Compose导航 */
    COMPOSE_NAVIGATION,
    /** XML导航 */
    XML_NAVIGATION,
    /** 自定义导航 */
    CUSTOM_NAVIGATION
}

/**
 * 导航目标数据类
 */
data class NavigationTarget(
    /** 目标类型 */
    val type: NavigationTargetType,
    /** 目标值 */
    val target: String
)

/**
 * 导航目标类型枚举
 */
enum class NavigationTargetType {
    /** 显式导航到指定路由 */
    NAVIGATE,
    /** 导航返回 */
    NAVIGATE_BACK,
    /** 弹出返回栈 */
    POP_BACK_STACK,
    /** 未知类型 */
    UNKNOWN
}

/**
 * UI组件信息数据类
 */
data class UIComponentInfo(
    /** 屏幕名称 */
    val screenName: String,
    /** 组件ID */
    val componentId: String,
    /** 组件名称 */
    val componentName: String,
    /** 组件类型 */
    val componentType: String,
    /** 组件属性映射 */
    val properties: Map<String, Any>,
    /** 组件事件列表 */
    val events: List<ComponentEvent>,
    /** 组件位置计算公式 */
    val positionFormula: Map<String, String>? = null,
    /** 组件大小计算公式 */
    val sizeFormula: Map<String, String>? = null
)

/**
 * 组件事件数据类
 */
data class ComponentEvent(
    /** 事件类型 */
    val eventType: String,
    /** 事件目标 */
    val target: String?,
    /** 事件参数 */
    val parameters: Map<String, Any>,
    /** 事件动作 */
    val action: String = ""
)

/**
 * 导航事件数据类（用于运行时检测）
 */
data class NavigationEvent(
    /** 事件时间戳 */
    val timestamp: Long,
    /** 源页面ID */
    val fromPageId: String,
    /** 目标页面ID */
    val toPageId: String,
    /** 触发组件ID */
    val triggerComponentId: String,
    /** 源Activity */
    val fromActivity: String? = null,
    /** 目标Activity */
    val toActivity: String? = null,
    /** 事件类型 */
    val eventType: NavigationEventType = NavigationEventType.ACTIVITY_STARTED
)

/**
 * 导航事件类型枚举
 */
enum class NavigationEventType {
    /** Activity启动 */
    ACTIVITY_STARTED,
    /** Activity恢复 */
    ACTIVITY_RESUMED,
    /** Activity暂停 */
    ACTIVITY_PAUSED,
    /** Activity停止 */
    ACTIVITY_STOPPED,
    /** Fragment附加 */
    FRAGMENT_ATTACHED,
    /** Fragment分离 */
    FRAGMENT_DETACHED
}

/**
 * 运行时组件信息数据类（用于运行时检测）
 */
data class RuntimeComponentInfo(
    /** 组件ID */
    val componentId: String,
    /** 组件类型 */
    val componentType: String,
    /** 组件文本 */
    val text: String?,
    /** 组件名称 */
    val name: String = "",
    /** 类名 */
    val className: String = "",
    /** 包名 */
    val packageName: String = "",
    /** 是否可点击 */
    val isClickable: Boolean = false,
    /** 父组件ID */
    val parentId: String? = null,
    /** 子组件列表 */
    val children: List<RuntimeComponentInfo> = emptyList()
)



/**
 * 生命周期事件数据类
 */
data class LifecycleEvent(
    /** Activity名称 */
    val activityName: String,
    /** 生命周期状态 */
    val lifecycleState: LifecycleState
)

/**
 * 生命周期状态枚举
 */
enum class LifecycleState {
    /** 创建 */
    CREATED,
    /** 启动 */
    STARTED,
    /** 恢复 */
    RESUMED,
    /** 暂停 */
    PAUSED,
    /** 停止 */
    STOPPED,
    /** 销毁 */
    DESTROYED
}
