package com.example.executor.model

import kotlinx.serialization.Serializable

/**
 * App Automation Map data class representing the complete automation map for an app
 */
@Serializable
data class AppAutomationMap(
    val appMeta: AppMeta,
    val uiModel: UiModel,
    val stateModel: StateModel,
    val intentModel: IntentModel
)

/**
 * App Meta data class representing metadata about the app
 */
@Serializable
data class AppMeta(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val uiFramework: String
)
