package com.example.executor.model

import kotlinx.serialization.Serializable

/**
 * UI Model data class representing the UI structure of the app
 */
@Serializable
data class UiModel(
    val pages: List<UiPage>
)

/**
 * UI Page data class representing a single screen in the app
 */
@Serializable
data class UiPage(
    val pageId: String,
    val pageName: String,
    val route: String,
    val layoutType: String,
    val components: List<UiComponent>
)

/**
 * UI Component data class representing a single UI element
 */
@Serializable
data class UiComponent(
    val componentId: String,
    val viewType: String,
    val text: String? = null,
    val contentDescription: String? = null,
    val position: Position? = null,
    val size: Size? = null,
    val positionFormula: Map<String, String>? = null,
    val sizeFormula: Map<String, String>? = null,
    val enabled: Boolean = true,
    val supportedTriggers: List<String>
)

/**
 * Position data class representing the coordinates of a UI component
 */
@Serializable
data class Position(
    val x: Int,
    val y: Int
)

/**
 * Size data class representing the dimensions of a UI component
 */
@Serializable
data class Size(
    val width: Int,
    val height: Int
)
