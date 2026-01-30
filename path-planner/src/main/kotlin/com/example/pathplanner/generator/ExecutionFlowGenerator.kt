package com.example.pathplanner.generator

import com.example.pathplanner.models.ExecutorStep
import com.example.pathplanner.models.Intent
import com.example.pathplanner.models.UIBinding
import com.example.pathplanner.models.UIMap
import com.example.pathplanner.search.PathResult

/**
 * 执行流程生成器，负责将意图路径转换为执行器可消费的步骤序列
 */
class ExecutionFlowGenerator {
    
    /**
     * 将路径结果转换为执行器步骤序列
     * @param uiMap UI地图
     * @param pathResult 路径结果，包含意图路径和状态序列
     * @return 执行器步骤序列
     */
    fun generate(
        uiMap: UIMap,
        pathResult: PathResult
    ): List<ExecutorStep> {
        val steps = mutableListOf<ExecutorStep>()
        val path = pathResult.path
        val stateSequence = pathResult.stateSequence
        
        // 确保状态序列长度大于等于路径长度 + 1
        if (stateSequence.size < path.size + 1) {
            return steps
        }
        
        for (i in path.indices) {
            val intent = path[i]
            val fromStateId = stateSequence[i]
            val expectedStateId = stateSequence[i + 1]
            
            // 获取当前状态
            val currentState = uiMap.states[fromStateId] ?: continue
            
            // 查找触发该意图的组件
            val triggeringComponent = findTriggeringComponent(currentState, intent)
            
            // 构建UI绑定信息
            val uiBinding = UIBinding(
                componentId = triggeringComponent?.componentId ?: "unknown",
                trigger = intent.type.ifEmpty { "CLICK" },
                parameters = intent.parameters
            )
            
            // 构建执行器步骤
            val step = ExecutorStep(
                intent = intent.intentId,
                fromStateId = fromStateId,
                expectedStateId = expectedStateId,
                uiBinding = uiBinding
            )
            
            steps.add(step)
        }
        
        return steps
    }
    
    /**
     * 将意图路径转换为执行器步骤序列（兼容旧接口）
     * @param uiMap UI地图
     * @param startStateId 起始状态ID
     * @param path 意图路径
     * @return 执行器步骤序列
     */
    fun generate(
        uiMap: UIMap,
        startStateId: String,
        path: List<Intent>
    ): List<ExecutorStep> {
        // 构建简单的状态序列
        val stateSequence = mutableListOf(startStateId)
        var currentStateId = startStateId
        
        for (intent in path) {
            currentStateId = intent.targetStateId
            stateSequence.add(currentStateId)
        }
        
        val pathResult = PathResult(path, stateSequence)
        return generate(uiMap, pathResult)
    }
    
    /**
     * 查找触发指定意图的组件
     */
    private fun findTriggeringComponent(
        state: com.example.pathplanner.models.State,
        intent: Intent
    ): com.example.pathplanner.models.Component? {
        // 首先检查状态级别的意图
        if (state.intents.any { it.intentId == intent.intentId }) {
            // 状态级别的意图，返回第一个组件或null
            return state.components.firstOrNull()
        }
        
        // 检查组件级别的意图
        for (component in state.components) {
            if (component.intents.any { it.intentId == intent.intentId }) {
                return component
            }
        }
        
        return null
    }
}