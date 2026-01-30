package com.example.maprecognizer.data

/**
 * 应用自动化地图的顶级数据结构
 */
data class AppAutomationMap(
    /** 应用元信息 */
    val appMeta: AppMeta,
    /** UI模型 */
    val uiModel: UiModel,
    /** 状态模型 */
    val stateModel: StateModel,
    /** 意图模型 */
    val intentModel: IntentModel
)
