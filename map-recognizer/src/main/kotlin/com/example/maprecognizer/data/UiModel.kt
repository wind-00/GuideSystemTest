package com.example.maprecognizer.data

/**
 * UI模型，包含应用的页面和组件信息
 */
data class UiModel(
    /** 页面列表 */
    val pages: List<Page>
)

/**
 * 页面数据结构
 */
data class Page(
    /** 页面ID */
    val pageId: String,
    /** 页面名称 */
    val pageName: String,
    /** 页面路由 */
    val route: String,
    /** 布局类型 */
    val layoutType: LayoutType,
    /** 页面组件列表 */
    val components: List<Component>
)

/**
 * 布局类型枚举
 */
enum class LayoutType {
    /** Compose布局 */
    COMPOSE,
    /** 传统View布局 */
    VIEW,
    /** 混合布局 */
    MIXED
}

/**
 * 组件数据结构
 */
data class Component(
    /** 组件ID */
    val componentId: String,
    /** 组件类型 */
    val viewType: ViewType,
    /** 组件文本 */
    val text: String?,
    /** 组件内容描述 */
    val contentDescription: String?,
    /** 组件位置 */
    val position: Position,
    /** 组件大小 */
    val size: Size,
    /** 组件是否可用 */
    val enabled: Boolean,
    /** 支持的触发类型 */
    val supportedTriggers: List<TriggerType>
)

/**
 * 组件类型枚举
 */
enum class ViewType {
    /** 按钮 */
    BUTTON,
    /** 图标按钮 */
    ICON_BUTTON,
    /** 文本字段 */
    TEXT_FIELD,
    /** 文本组件 */
    TEXT,
    /** 开关 */
    SWITCH,
    /** 复选框 */
    CHECKBOX,
    /** 单选按钮 */
    RADIO_BUTTON,
    /** 滑块 */
    SEEK_BAR,
    /** 列表 */
    LIST,
    /** 卡片 */
    CARD,
    /** 其他组件 */
    OTHER
}

/**
 * 触发类型枚举
 */
enum class TriggerType {
    /** 点击事件 */
    CLICK,
    /** 文本改变事件 */
    TEXT_CHANGE,
    /** 选择事件 */
    SELECT,
    /** 长点击事件 */
    LONG_CLICK,
    /** 选中状态改变事件 */
    CHECKED_CHANGE,
    /** 进度改变事件 */
    PROGRESS_CHANGE
}

/**
 * 位置数据结构
 */
data class Position(
    /** X坐标 */
    val x: Int,
    /** Y坐标 */
    val y: Int
)

/**
 * 大小数据结构
 */
data class Size(
    /** 宽度 */
    val width: Int,
    /** 高度 */
    val height: Int
)
