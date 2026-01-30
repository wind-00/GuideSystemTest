package com.example.planner

import kotlinx.serialization.json.Json

class Planner(private val uiMap: UiMapModel) {
    companion object {
        const val REASON_NO_TARGET_ACTION = "NO_TARGET_ACTION"
        const val REASON_INVALID_START_PAGE = "INVALID_START_PAGE"
        const val REASON_NO_PATH_FOUND = "NO_PATH_FOUND"
    }

    /**
     * 计算两个字符串的相似度（基于Levenshtein距离）
     */
    private fun calculateSimilarity(s1: String, s2: String): Double {
        val maxLength = maxOf(s1.length, s2.length)
        if (maxLength == 0) return 1.0
        
        val distance = levenshteinDistance(s1, s2)
        return 1.0 - (distance.toDouble() / maxLength)
    }

    /**
     * 计算Levenshtein距离
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        
        return dp[m][n]
    }

    /**
     * 查找最相似的visibleText
     */
    private fun findMostSimilarVisibleText(text: String): String? {
        var bestMatch: String? = null
        var highestSimilarity = 0.7 // 阈值，只匹配相似度高于70%的
        
        for (visibleText in uiMap.visible_text_index.keys) {
            val similarity = calculateSimilarity(text, visibleText)
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity
                bestMatch = visibleText
            }
        }
        
        return bestMatch
    }

    fun plan(userGoal: UserGoal): PlanResult {
        // 阶段1：目标Action和页面确定
        var targetActions = uiMap.visible_text_index[userGoal.targetVisibleText]
        
        // 如果精确匹配失败，尝试模糊匹配
        if (targetActions == null) {
            val similarText = findMostSimilarVisibleText(userGoal.targetVisibleText)
            if (similarText != null) {
                targetActions = uiMap.visible_text_index[similarText]
                println("Using similar visible text: $similarText (matched from: ${userGoal.targetVisibleText})")
            }
        }
        
        if (targetActions == null) {
            return PlanResult(
                success = false,
                actionPath = emptyList(),
                reason = REASON_NO_TARGET_ACTION
            )
        }
        
        // 收集所有目标action所在的页面
        val targetPages = mutableSetOf<Int>()
        val actionToPageMap = mutableMapOf<Int, Int>() // actionId -> pageIdx
        
        for (actionId in targetActions) {
            val actionMeta = uiMap.action_metadata[actionId]
            if (actionMeta != null) {
                val pageIdx = uiMap.page_index[actionMeta.page] ?: continue
                targetPages.add(pageIdx)
                actionToPageMap[actionId] = pageIdx
            }
        }
        
        if (targetPages.isEmpty()) {
            return PlanResult(
                success = false,
                actionPath = emptyList(),
                reason = REASON_NO_TARGET_ACTION
            )
        }

        // 阶段2：初始状态构建
        val startPageIdx = uiMap.page_index[userGoal.startPage] ?: return PlanResult(
            success = false,
            actionPath = emptyList(),
            reason = REASON_INVALID_START_PAGE
        )

        val initialNode = PlannerNode(pageIdx = startPageIdx, path = emptyList())

        // 阶段3：图搜索 - 寻找最短路径到任何目标页面，然后执行对应的action
        // 先寻找到达目标页面的路径
        var bestPath: List<Int> = emptyList()
        var bestActionId: Int = -1
        var found = false
        
        when (userGoal.searchStrategy) {
            SearchStrategy.BFS -> {
                // 对每个目标页面执行BFS
                for (targetPageIdx in targetPages) {
                    val pathToPage = bfsToPage(initialNode, targetPageIdx)
                    if (pathToPage.success) {
                        // 找到路径后，查找该页面上的目标action
                        val pageActions = uiMap.transition[targetPageIdx] ?: emptyMap()
                        for ((actionId, _) in pageActions) {
                            if (actionId in targetActions && actionToPageMap[actionId] == targetPageIdx) {
                                bestPath = pathToPage.actionPath + actionId
                                bestActionId = actionId
                                found = true
                                break
                            }
                        }
                        if (found) break
                    }
                }
            }
            SearchStrategy.DFS -> {
                // 对每个目标页面执行DFS
                for (targetPageIdx in targetPages) {
                    val pathToPage = dfsToPage(initialNode, targetPageIdx)
                    if (pathToPage.success) {
                        // 找到路径后，查找该页面上的目标action
                        val pageActions = uiMap.transition[targetPageIdx] ?: emptyMap()
                        for ((actionId, _) in pageActions) {
                            if (actionId in targetActions && actionToPageMap[actionId] == targetPageIdx) {
                                bestPath = pathToPage.actionPath + actionId
                                bestActionId = actionId
                                found = true
                                break
                            }
                        }
                        if (found) break
                    }
                }
            }
        }
        
        if (found && bestPath.isNotEmpty()) {
            return PlanResult(
                success = true,
                actionPath = bestPath,
                reason = null
            )
        }
        
        // 阶段4：如果找不到直接到目标页面的路径，尝试原有的搜索方式
        // 寻找包含目标action的页面的路径
        return when (userGoal.searchStrategy) {
            SearchStrategy.BFS -> bfs(initialNode, targetActions)
            SearchStrategy.DFS -> dfs(initialNode, targetActions)
        }
    }

    private fun bfs(initialNode: PlannerNode, targetActions: List<Int>): PlanResult {
        val queue = ArrayDeque<PlannerNode>()
        val visited = mutableSetOf<Pair<Int, Int>>() // (pageIdx, actionId) 对

        queue.add(initialNode)

        while (queue.isNotEmpty()) {
            val currentNode = queue.removeFirst()

            // 获取当前页面的所有可执行action
            val actions = uiMap.transition[currentNode.pageIdx] ?: emptyMap()

            for ((actionId, toPages) in actions) {
                // 检查是否到达目标
                if (actionId in targetActions) {
                    return PlanResult(
                        success = true,
                        actionPath = currentNode.path + actionId,
                        reason = null
                    )
                }

                // 检查是否已经访问过这个(pageIdx, actionId)对
                val visitedKey = Pair(currentNode.pageIdx, actionId)
                if (visitedKey in visited) {
                    continue
                }
                visited.add(visitedKey)

                // 遍历所有可能的跳转页面
                for (toPageIdx in toPages) {
                    val newPath = currentNode.path + actionId
                    val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                    queue.add(newNode)
                }
            }
        }

        // 未找到路径
        return PlanResult(
            success = false,
            actionPath = emptyList(),
            reason = REASON_NO_PATH_FOUND
        )
    }

    private fun dfs(initialNode: PlannerNode, targetActions: List<Int>): PlanResult {
        val stack = ArrayDeque<PlannerNode>()
        val visited = mutableSetOf<Pair<Int, Int>>() // (pageIdx, actionId) 对

        stack.add(initialNode)

        while (stack.isNotEmpty()) {
            val currentNode = stack.removeLast()

            // 获取当前页面的所有可执行action
            val actions = uiMap.transition[currentNode.pageIdx] ?: emptyMap()

            for ((actionId, toPages) in actions) {
                // 检查是否到达目标
                if (actionId in targetActions) {
                    return PlanResult(
                        success = true,
                        actionPath = currentNode.path + actionId,
                        reason = null
                    )
                }

                // 检查是否已经访问过这个(pageIdx, actionId)对
                val visitedKey = Pair(currentNode.pageIdx, actionId)
                if (visitedKey in visited) {
                    continue
                }
                visited.add(visitedKey)

                // 遍历所有可能的跳转页面
                for (toPageIdx in toPages) {
                    val newPath = currentNode.path + actionId
                    val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                    stack.add(newNode)
                }
            }
        }

        // 未找到路径
        return PlanResult(
            success = false,
            actionPath = emptyList(),
            reason = REASON_NO_PATH_FOUND
        )
    }
    
    /**
     * BFS搜索，寻找直接到达目标页面的路径
     * @param initialNode 初始节点
     * @param targetPageIdx 目标页面索引
     * @return 路径规划结果
     */
    private fun bfsToPage(initialNode: PlannerNode, targetPageIdx: Int): PlanResult {
        val queue = ArrayDeque<PlannerNode>()
        val visited = mutableSetOf<Pair<Int, Int>>() // (pageIdx, actionId) 对

        queue.add(initialNode)

        while (queue.isNotEmpty()) {
            val currentNode = queue.removeFirst()
            
            // 检查当前页面是否是目标页面
            if (currentNode.pageIdx == targetPageIdx) {
                return PlanResult(
                    success = true,
                    actionPath = currentNode.path,
                    reason = null
                )
            }

            // 获取当前页面的所有可执行action
            val actions = uiMap.transition[currentNode.pageIdx] ?: emptyMap()

            for ((actionId, toPages) in actions) {
                // 检查是否到达目标页面
                if (toPages.contains(targetPageIdx)) {
                    return PlanResult(
                        success = true,
                        actionPath = currentNode.path + actionId,
                        reason = null
                    )
                }

                // 检查是否已经访问过这个(pageIdx, actionId)对
                val visitedKey = Pair(currentNode.pageIdx, actionId)
                if (visitedKey in visited) {
                    continue
                }
                visited.add(visitedKey)

                // 遍历所有可能的跳转页面
                for (toPageIdx in toPages) {
                    val newPath = currentNode.path + actionId
                    val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                    queue.add(newNode)
                }
            }
        }

        // 未找到路径
        return PlanResult(
            success = false,
            actionPath = emptyList(),
            reason = REASON_NO_PATH_FOUND
        )
    }
    
    /**
     * DFS搜索，寻找直接到达目标页面的路径
     * @param initialNode 初始节点
     * @param targetPageIdx 目标页面索引
     * @return 路径规划结果
     */
    private fun dfsToPage(initialNode: PlannerNode, targetPageIdx: Int): PlanResult {
        val stack = ArrayDeque<PlannerNode>()
        val visited = mutableSetOf<Pair<Int, Int>>() // (pageIdx, actionId) 对

        stack.add(initialNode)

        while (stack.isNotEmpty()) {
            val currentNode = stack.removeLast()
            
            // 检查当前页面是否是目标页面
            if (currentNode.pageIdx == targetPageIdx) {
                return PlanResult(
                    success = true,
                    actionPath = currentNode.path,
                    reason = null
                )
            }

            // 获取当前页面的所有可执行action
            val actions = uiMap.transition[currentNode.pageIdx] ?: emptyMap()

            for ((actionId, toPages) in actions) {
                // 检查是否到达目标页面
                if (toPages.contains(targetPageIdx)) {
                    return PlanResult(
                        success = true,
                        actionPath = currentNode.path + actionId,
                        reason = null
                    )
                }

                // 检查是否已经访问过这个(pageIdx, actionId)对
                val visitedKey = Pair(currentNode.pageIdx, actionId)
                if (visitedKey in visited) {
                    continue
                }
                visited.add(visitedKey)

                // 遍历所有可能的跳转页面
                for (toPageIdx in toPages) {
                    val newPath = currentNode.path + actionId
                    val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                    stack.add(newNode)
                }
            }
        }

        // 未找到路径
        return PlanResult(
            success = false,
            actionPath = emptyList(),
            reason = REASON_NO_PATH_FOUND
        )
    }

    // 移除不再使用的getNodeKey方法

}

// 辅助函数：从JSON字符串创建Planner
fun createPlannerFromJson(jsonString: String): Planner {
    val uiMap = Json.decodeFromString<UiMapModel>(jsonString)
    return Planner(uiMap)
}