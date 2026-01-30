package com.example.pathplanner.search

import com.example.pathplanner.models.ComponentTarget
import com.example.pathplanner.models.Intent
import com.example.pathplanner.models.UIMap

/**
 * 路径搜索结果，包含路径和完整状态序列
 */
data class PathResult(
    val path: List<Intent>,
    val stateSequence: List<String>
)

/**
 * 路径搜索器，使用BFS算法在UI地图中搜索路径
 */
class PathSearcher {
    
    /**
     * 搜索从起始状态到目标状态的所有可能路径
     * @param uiMap UI地图
     * @param startStateId 起始状态ID
     * @param targetStateIds 目标状态ID集合
     * @param targetSpec 目标规范，用于检查ComponentTarget的组件是否在当前状态
     * @return 所有可能的路径结果，每条路径包含Intent序列和完整状态序列
     */
    fun search(
        uiMap: UIMap,
        startStateId: String,
        targetStateIds: Set<String>,
        targetSpec: com.example.pathplanner.models.TargetSpec? = null
    ): List<PathResult> {
        val allPaths = mutableListOf<PathResult>()
        
        println("[PathSearcher] 开始搜索路径")
        println("- 起始状态：$startStateId")
        println("- 目标状态：$targetStateIds")
        
        // 检查起始状态是否存在
        if (!uiMap.states.containsKey(startStateId)) {
            println("[PathSearcher] 错误：起始状态 $startStateId 不存在")
            return allPaths
        }
        
        val currentState = uiMap.states[startStateId] ?: return allPaths
        
        // 检查起始状态是否就是目标状态
        if (targetStateIds.contains(startStateId)) {
            // 对于ComponentTarget，需要检查目标组件是否真的在当前状态中
            val isComponentTarget = targetSpec is com.example.pathplanner.models.ComponentTarget
            val targetComponentInCurrentState = if (isComponentTarget) {
                currentState.components.any { component ->
                    matchesComponent(component, targetSpec as com.example.pathplanner.models.ComponentTarget)
                }
            } else {
                true
            }
            
            if (targetComponentInCurrentState) {
                println("[PathSearcher] 起始状态 $startStateId 就是目标状态，且目标组件在当前状态中")
                
                // 收集所有可用的意图，包括状态级别和组件级别的
                val allAvailableIntents = mutableListOf<Intent>()
                
                // 1. 收集状态级别的意图
                allAvailableIntents.addAll(currentState.intents)
                println("[PathSearcher] 收集到 ${currentState.intents.size} 个状态级意图")
                
                // 2. 收集组件级别的意图
                for (component in currentState.components) {
                    allAvailableIntents.addAll(component.intents)
                }
                println("[PathSearcher] 总共收集到 ${allAvailableIntents.size} 个可用意图")
                
                if (allAvailableIntents.isNotEmpty()) {
                    // 对于ComponentTarget，只返回与目标组件相关的意图
                    val relevantIntents = if (isComponentTarget) {
                        allAvailableIntents.filter { intent ->
                            // 找到包含该意图的组件
                            currentState.components.any { component ->
                                component.intents.any { it.intentId == intent.intentId }
                            }
                        }
                    } else {
                        // 对于StateTarget，过滤意图，只保留停留在当前状态的意图
                        allAvailableIntents.filter { intent ->
                            intent.targetStateId == startStateId
                        }
                    }
                    
                    if (relevantIntents.isNotEmpty()) {
                        // 返回相关意图
                        for (intent in relevantIntents) {
                            allPaths.add(PathResult(
                                path = listOf(intent),
                                stateSequence = listOf(startStateId, intent.targetStateId)
                            ))
                        }
                        println("[PathSearcher] 返回 ${allPaths.size} 条停留在当前状态的路径结果")
                    } else {
                        // 如果没有相关意图，返回所有可用意图
                        for (intent in allAvailableIntents) {
                            allPaths.add(PathResult(
                                path = listOf(intent),
                                stateSequence = listOf(startStateId, intent.targetStateId)
                            ))
                        }
                        println("[PathSearcher] 返回 ${allPaths.size} 条路径结果")
                    }
                } else {
                    // 如果没有可用意图，返回一个包含空路径的结果
                    allPaths.add(PathResult(emptyList(), listOf(startStateId)))
                    println("[PathSearcher] 没有可用意图，返回空路径结果")
                }
                return allPaths
            } else {
                // 目标组件不在当前状态，继续执行BFS搜索
                println("[PathSearcher] 起始状态 $startStateId 是目标状态，但目标组件不在当前状态，继续搜索")
            }
        }
        
        // BFS队列，存储当前状态、已走过的路径和状态序列
        val queue = ArrayDeque<Triple<String, List<Intent>, List<String>>>()
        queue.add(Triple(startStateId, emptyList(), listOf(startStateId)))
        println("[PathSearcher] 初始化BFS队列，起始状态：$startStateId")
        
        // 使用map记录每个状态的最短路径长度，允许在特定条件下重新访问状态
        val shortestPathLength = mutableMapOf<String, Int>()
        shortestPathLength[startStateId] = 0
        
        // 记录访问过的状态，避免循环
        val visited = mutableSetOf<String>()
        visited.add(startStateId)
        
        while (queue.isNotEmpty()) {
            val (currentStateId, currentPath, currentStateSequence) = queue.removeFirst()
            println("[PathSearcher] 处理状态：$currentStateId，当前路径长度：${currentPath.size}")
            
            // 获取当前状态
            val currentState = uiMap.states[currentStateId] ?: continue
            
            // 遍历当前状态的所有意图
            for (intent in currentState.intents) {
                processIntent(
                    uiMap = uiMap,
                    intent = intent,
                    currentStateId = currentStateId,
                    currentPath = currentPath,
                    currentStateSequence = currentStateSequence,
                    targetStateIds = targetStateIds,
                    allPaths = allPaths,
                    queue = queue,
                    shortestPathLength = shortestPathLength,
                    visited = visited
                )
            }
            
            // 遍历当前状态所有组件的意图
            for (component in currentState.components) {
                for (intent in component.intents) {
                    processIntent(
                        uiMap = uiMap,
                        intent = intent,
                        currentStateId = currentStateId,
                        currentPath = currentPath,
                        currentStateSequence = currentStateSequence,
                        targetStateIds = targetStateIds,
                        allPaths = allPaths,
                        queue = queue,
                        shortestPathLength = shortestPathLength,
                        visited = visited
                    )
                }
            }
        }
        
        println("[PathSearcher] 搜索完成，找到 ${allPaths.size} 条路径")
        return allPaths
    }
    
    /**
     * 处理单个意图，检查是否到达目标状态或需要继续搜索
     */
    private fun processIntent(
        uiMap: UIMap,
        intent: Intent,
        currentStateId: String,
        currentPath: List<Intent>,
        currentStateSequence: List<String>,
        targetStateIds: Set<String>,
        allPaths: MutableList<PathResult>,
        queue: ArrayDeque<Triple<String, List<Intent>, List<String>>>,
        shortestPathLength: MutableMap<String, Int>,
        visited: MutableSet<String>
    ) {
        val nextStateId = intent.targetStateId
        val newPath = currentPath + intent
        val newStateSequence = currentStateSequence + nextStateId
        
        println("[PathSearcher] 处理意图：${intent.intentId}，从 $currentStateId 到 $nextStateId")
        
        // 检查是否到达目标状态
        if (targetStateIds.contains(nextStateId)) {
            // 找到一条路径，添加到结果中
            allPaths.add(PathResult(newPath, newStateSequence))
            println("[PathSearcher] 找到目标路径：${newPath.joinToString { it.intentId }}")
        } else {
            // 检查下一个状态是否存在
            if (!uiMap.states.containsKey(nextStateId)) {
                println("[PathSearcher] 跳过：下一个状态 $nextStateId 不存在")
                return
            }
            
            // 检查是否应该继续搜索该路径
            val currentLength = newPath.size
            val existingLength = shortestPathLength.getOrDefault(nextStateId, Int.MAX_VALUE)
            
            // 如果当前路径长度小于等于已记录的最短路径长度，允许继续搜索
            if (currentLength <= existingLength) {
                shortestPathLength[nextStateId] = currentLength
                queue.add(Triple(nextStateId, newPath, newStateSequence))
                visited.add(nextStateId)
                println("[PathSearcher] 继续搜索：添加状态 $nextStateId 到队列")
            } else {
                println("[PathSearcher] 跳过：已有更短路径到达 $nextStateId")
            }
        }
    }
    
    /**
     * 搜索从起始状态到目标状态的所有可能路径（简化版，仅返回Intent序列）
     * @param uiMap UI地图
     * @param startStateId 起始状态ID
     * @param targetStateIds 目标状态ID集合
     * @return 所有可能的路径，每条路径是Intent序列
     */
    fun searchIntents(
        uiMap: UIMap,
        startStateId: String,
        targetStateIds: Set<String>
    ): List<List<Intent>> {
        val pathResults = search(uiMap, startStateId, targetStateIds)
        return pathResults.map { it.path }
    }
    
    /**
     * 搜索从起始状态到目标状态的最短路径
     * @param uiMap UI地图
     * @param startStateId 起始状态ID
     * @param targetStateIds 目标状态ID集合
     * @return 最短路径结果，如果没有找到则返回null
     */
    fun searchShortestPath(
        uiMap: UIMap,
        startStateId: String,
        targetStateIds: Set<String>
    ): PathResult? {
        val allPaths = search(uiMap, startStateId, targetStateIds)
        
        return if (allPaths.isEmpty()) {
            null
        } else {
            // 返回最短路径
            allPaths.minByOrNull { it.path.size }
        }
    }
    
    /**
     * 搜索从起始状态到目标状态的最短路径（简化版，仅返回Intent序列）
     * @param uiMap UI地图
     * @param startStateId 起始状态ID
     * @param targetStateIds 目标状态ID集合
     * @return 最短路径，如果没有找到则返回null
     */
    fun searchShortestPathIntents(
        uiMap: UIMap,
        startStateId: String,
        targetStateIds: Set<String>
    ): List<Intent>? {
        return searchShortestPath(uiMap, startStateId, targetStateIds)?.path
    }
    
    /**
     * 检查组件是否匹配ComponentTarget
     */
    private fun matchesComponent(
        component: com.example.pathplanner.models.Component,
        targetSpec: ComponentTarget
    ): Boolean {
        var matchScore = 0.0
        var totalPossibleScore = 0.0
        
        // 获取组件的语义角色
        val componentSemanticRole = component.properties["semanticRole"] ?: ""
        val componentIntentTags = component.properties["intentTags"]?.split(",") ?: emptyList()
        
        // 检查组件ID（如果提供）
        if (targetSpec.componentId != null) {
            totalPossibleScore += 2
            if (component.componentId == targetSpec.componentId) {
                matchScore += 2
            } else {
                // 检查组件ID是否包含关键字
                val targetId = targetSpec.componentId.lowercase()
                val componentId = component.componentId.lowercase()
                
                // 检查完整包含关系
                if (targetId.contains(componentId) || componentId.contains(targetId)) {
                    matchScore += 1
                }
            }
        }
        
        // 检查组件文本（如果提供）
        if (targetSpec.componentText != null && component.text != null) {
            totalPossibleScore += 1.5
            val targetText = targetSpec.componentText.lowercase()
            val componentText = component.text.lowercase()
            
            if (componentText == targetText) {
                matchScore += 1.5
            } else if (componentText.contains(targetText)) {
                matchScore += 1.25
            } else if (targetText.contains(componentText)) {
                matchScore += 1.0
            }
        }
        
        // 检查语义角色匹配
        if (targetSpec.componentText != null) {
            totalPossibleScore += 1.0
            val targetText = targetSpec.componentText.lowercase()
            
            // 语义角色匹配
            if (targetText.contains("返回") || targetText.contains("back")) {
                if (componentSemanticRole.contains("NAVIGATE") || componentSemanticRole.contains("BACK")) {
                    matchScore += 1.0
                }
            } else if (targetText.contains("跳转到") || targetText.contains("goto")) {
                if (componentSemanticRole.contains("ACTION")) {
                    matchScore += 0.75
                }
            }
        }
        
        // 计算匹配度
        val matchPercentage = if (totalPossibleScore == 0.0) 0.0 else matchScore / totalPossibleScore.toDouble()
        
        // 返回匹配度是否大于35%
        return matchPercentage >= 0.35
    }
}