package com.example.maprecognizer.data

/**
 * 状态模型，包含应用的状态信息
 */
data class StateModel(
    /** 状态列表 */
    val states: List<State>,
    /** 初始状态ID */
    val initialStateId: String
)

/**
 * 状态数据结构
 * 注意：State与Page是1:1映射关系，仅用于判断页面是否处于前台
 */
data class State(
    /** 状态ID */
    val stateId: String,
    /** 状态名称 */
    val name: String,
    /** 状态描述 */
    val description: String,
    /** 状态信号列表，用于判断页面是否处于前台 */
    val signals: List<StateSignal>,
    /** 关联的页面ID列表（1:1映射，只有一个元素） */
    val relatedPageIds: List<String>
)

/**
 * 状态信号，用于判断页面是否处于前台
 */
data class StateSignal(
    /** 信号类型 */
    val type: SignalType,
    /** 信号目标 */
    val target: String,
    /** 预期值 */
    val expectedValue: Any?,
    /** 匹配器类型 */
    val matcher: SignalMatcher
)

/**
 * 信号类型枚举
 */
enum class SignalType {
    /** 组件可见信号（基于componentId） */
    COMPONENT_VISIBLE,
    /** 视图ID存在信号 */
    VIEW_ID_EXISTS,
    /** 文本可见信号 */
    TEXT_VISIBLE,
    /** 内容描述可见信号 */
    CONTENT_DESC_VISIBLE,
    /** 页面激活信号 */
    PAGE_ACTIVE,
    /** 路由匹配信号 */
    ROUTE_MATCH
}

/**
 * 信号匹配器类型枚举
 */
enum class SignalMatcher {
    /** 等于匹配 */
    EQUALS,
    /** 包含匹配 */
    CONTAINS,
    /** 正则匹配 */
    REGEX
}
