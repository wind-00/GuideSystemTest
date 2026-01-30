package com.example.maprecognizer.data

/**
 * 应用元信息
 */
data class AppMeta(
    /** 应用名称 */
    val appName: String,
    /** 包名 */
    val packageName: String,
    /** 版本名称 */
    val versionName: String,
    /** 版本代码 */
    val versionCode: Int,
    /** UI框架类型 */
    val uiFramework: UiFramework
)

/**
 * UI框架类型枚举
 */
enum class UiFramework {
    /** Compose UI框架 */
    COMPOSE,
    /** 传统View系统 */
    VIEW
}
