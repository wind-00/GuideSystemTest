package com.example.maprecognizer.data

/**
 * 意图模型，包含应用的交互意图信息
 */
data class IntentModel(
    /** 意图列表 */
    val intents: List<Intent>
)

/**
 * 意图数据结构
 * 注意：Intent表示用户在某个UI上可以做的原子交互动作，明确表达状态转移关系
 */
data class Intent(
    /** 意图ID */
    val intentId: String,
    /** 意图类型 */
    val type: IntentType,
    /** 意图描述 */
    val description: String,
    /** 起始状态ID */
    val fromStateId: String,
    /** UI操作绑定列表 */
    val uiBindings: List<UIActionBinding>,
    /** 目标状态ID */
    val toStateId: String,
    /** 状态转移后验证条件列表 */
    val postConditions: List<PostCondition>
)

/**
 * UI操作绑定，描述哪个组件的哪个事件会触发这个意图
 */
data class UIActionBinding(
    /** 组件ID */
    val componentId: String,
    /** 触发类型 */
    val trigger: TriggerType,
    /** 参数映射 */
    val parameters: Map<String, Any>
)

/**
 * 状态转移验证条件
 */
data class PostCondition(
    /** 验证条件类型 */
    val type: PostConditionType,
    /** 验证目标 */
    val target: String,
    /** 预期值 */
    val expectedValue: Any? = null,
    /** 匹配方式 */
    val matcher: SignalMatcher = SignalMatcher.EQUALS
)

/**
 * 验证条件类型枚举
 */
enum class PostConditionType {
    /** 组件可见性验证 */
    COMPONENT_VISIBLE,
    /** 文本可见性验证 */
    TEXT_VISIBLE,
    /** 路由匹配验证 */
    ROUTE_MATCH,
    /** 页面激活验证 */
    PAGE_ACTIVE
}

/**
 * 意图类型枚举
 */
enum class IntentType {
    /** 点击意图 */
    CLICK,
    /** 导航意图 - State发生变化 */
    NAVIGATION,
    /** 内部状态变化意图 - State不变，但UI内部状态改变 */
    STATE_INTERNAL,
    /** 无状态变化意图 - 纯UI行为，不影响状态 */
    NO_STATE_CHANGE,
    /** 输入意图 */
    INPUT,
    /** 返回导航意图 */
    NAVIGATE_BACK,
    /** 提交意图 */
    SUBMIT,
    /** 搜索意图 */
    SEARCH,
    /** 选择意图 */
    SELECT,
    /** 登录意图 */
    LOGIN,
    /** 其他意图 */
    OTHER
}

/**
 * 风险级别枚举
 */
enum class RiskLevel {
    /** 低风险 */
    LOW,
    /** 中风险 */
    MEDIUM,
    /** 高风险 */
    HIGH
}
