package com.example.planner

import kotlinx.serialization.Serializable

@Serializable
data class ActionMeta(
    val page: String,
    val componentId: String,
    val triggerType: String,
    val visibleText: String,
    val viewType: String
)

@Serializable
data class UiMapModel(
    val page_index: Map<String, Int>,
    val action_index: Map<String, Int>,
    val action_metadata: Map<Int, ActionMeta>,
    val visible_text_index: Map<String, List<Int>>,
    val transition: Map<Int, Map<Int, List<Int>>>
)

data class UserGoal(
    val targetVisibleText: String,
    val startPage: String,
    val searchStrategy: SearchStrategy
)

enum class SearchStrategy {
    BFS,
    DFS
}

data class PlannerNode(
    val pageIdx: Int,
    val path: List<Int> // actionId sequence
)

data class PlanResult(
    val success: Boolean,
    val actionPath: List<Int>,
    val reason: String?
)