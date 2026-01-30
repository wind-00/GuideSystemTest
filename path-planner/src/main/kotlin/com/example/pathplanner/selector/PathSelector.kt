package com.example.pathplanner.selector

import com.example.pathplanner.models.Intent
import com.example.pathplanner.search.PathResult

/**
 * 路径选择器接口，负责从多条路径中选择最优路径
 */
interface PathSelector {
    /**
     * 从多条路径结果中选择最优路径
     * @param pathResults 待选择的路径结果列表
     * @return 最优路径结果，如果没有路径则返回null
     */
    fun select(pathResults: List<PathResult>): PathResult?
    
    /**
     * 从多条路径中选择最优路径（简化版，兼容旧接口）
     * @param paths 待选择的路径列表
     * @return 最优路径，如果没有路径则返回null
     */
    fun select(paths: List<List<Intent>>): List<Intent>? {
        // 将路径转换为PathResult，状态序列默认为空
        val pathResults = paths.map { PathResult(it, emptyList()) }
        return select(pathResults)?.path
    }
}

/**
 * 默认路径选择器，选择最短路径，如果有多个最短路径，则选择能够改变状态的路径
 */
class DefaultPathSelector : PathSelector {
    override fun select(pathResults: List<PathResult>): PathResult? {
        if (pathResults.isEmpty()) {
            return null
        }
        
        // 首先选择最短路径
        val shortestPaths = pathResults.filter { 
            it.path.size == pathResults.minOf { it.path.size }
        }
        
        if (shortestPaths.size == 1) {
            // 如果只有一条最短路径，直接返回
            return shortestPaths.first()
        } else {
            // 如果有多个最短路径，选择能够改变状态的路径
            val pathsThatChangeState = shortestPaths.filter { pathResult ->
                pathResult.stateSequence.size >= 2 && 
                pathResult.stateSequence.first() != pathResult.stateSequence.last()
            }
            
            if (pathsThatChangeState.isNotEmpty()) {
                // 如果有能够改变状态的路径，返回第一条
                return pathsThatChangeState.first()
            } else {
                // 如果没有能够改变状态的路径，返回第一条最短路径
                return shortestPaths.first()
            }
        }
    }
}

/**
 * 基于权重的路径选择器，预留接口用于实现风险评估和稳定性权重
 */
class WeightedPathSelector(
    private val riskAssessment: (Intent) -> Double = { _ -> 1.0 },
    private val stabilityWeight: (Intent) -> Double = { _ -> 1.0 }
) : PathSelector {
    override fun select(pathResults: List<PathResult>): PathResult? {
        if (pathResults.isEmpty()) {
            return null
        }
        
        // 计算每条路径的总权重，选择权重最低的路径
        return pathResults.minByOrNull { pathResult ->
            pathResult.path.sumOf { intent ->
                // 权重计算公式：风险评估值 * 稳定性权重值
                riskAssessment(intent) * stabilityWeight(intent)
            }
        }
    }
}