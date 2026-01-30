package com.example.pathplanner.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 表示整个UI地图，包含所有状态和意图
 */
@Serializable
data class UIMap(
    val states: Map<String, State>,
    val version: String = "1.0"
)

/**
 * 表示应用的一个页面状态
 */
@Serializable
data class State(
    val stateId: String,
    val components: List<Component>,
    val intents: List<Intent>,
    val description: String? = null
)

/**
 * 表示UI组件
 */
@Serializable
data class Component(
    val componentId: String,
    val type: String,
    val text: String? = null,
    val properties: Map<String, String> = emptyMap(),
    val intents: List<Intent> = emptyList()
)

/**
 * 表示可执行的操作
 */
@Serializable
data class Intent(
    val intentId: String,
    val type: String,
    val targetStateId: String,
    val description: String? = null,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * 目标规范的基类
 */
@Serializable
sealed class TargetSpec {
    abstract val confidence: Double
    abstract val type: String
}

/**
 * 状态目标，表示用户希望到达的特定状态
 */
@Serializable
@SerialName("StateTarget")
data class StateTarget(
    val stateId: String,
    override val confidence: Double
) : TargetSpec() {
    override val type: String = "StateTarget"
}

/**
 * 组件目标，表示用户希望与特定组件交互或到达包含该组件的状态
 */
@Serializable
@SerialName("ComponentTarget")
data class ComponentTarget(
    val componentId: String? = null,
    val componentType: String? = null,
    val componentText: String? = null,
    val componentRole: String? = null,
    val componentProperties: Map<String, String> = emptyMap(),
    override val confidence: Double
) : TargetSpec() {
    override val type: String = "ComponentTarget"
}

/**
 * 执行器可消费的步骤
 */
@Serializable
data class ExecutorStep(
    val intent: String,
    val fromStateId: String,
    val expectedStateId: String,
    val uiBinding: UIBinding
)

/**
 * UI组件绑定信息
 */
@Serializable
data class UIBinding(
    val componentId: String,
    val trigger: String,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * 规划假设
 */
@Serializable
data class Assumptions(
    val startState: String,
    val confidence: Double
)

/**
 * 规划器输出
 */
@Serializable
data class PlannerOutput(
    val target: TargetSpec,
    val plannedPath: List<ExecutorStep>,
    val assumptions: Assumptions
)