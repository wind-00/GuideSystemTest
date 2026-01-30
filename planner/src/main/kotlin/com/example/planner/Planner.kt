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
        println("=== Planner.plan() called with goal: ${userGoal.targetVisibleText}, startPage: ${userGoal.startPage}")
        
        // 阶段1：目标Action和页面确定
        var targetActions = uiMap.visible_text_index[userGoal.targetVisibleText]
        
        println("Initial targetActions: $targetActions")
        
        // 如果精确匹配失败，尝试模糊匹配
        if (targetActions == null) {
            val similarText = findMostSimilarVisibleText(userGoal.targetVisibleText)
            if (similarText != null) {
                targetActions = uiMap.visible_text_index[similarText]
                println("Using similar visible text: $similarText (matched from: ${userGoal.targetVisibleText})")
                println("Similar targetActions: $targetActions")
            }
        }
        
        if (targetActions == null) {
            println("No target actions found for: ${userGoal.targetVisibleText}")
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
                println("ActionId $actionId mapped to page $pageIdx (${actionMeta.page})")
            }
        }
        
        if (targetPages.isEmpty()) {
            println("No target pages found for actions: $targetActions")
            return PlanResult(
                success = false,
                actionPath = emptyList(),
                reason = REASON_NO_TARGET_ACTION
            )
        }
        
        println("Target pages: $targetPages")

        // 阶段2：初始状态构建
        val startPageIdx = uiMap.page_index[userGoal.startPage] ?: return PlanResult(
            success = false,
            actionPath = emptyList(),
            reason = REASON_INVALID_START_PAGE
        )

        println("Start page index: $startPageIdx (${userGoal.startPage})")
        val initialNode = PlannerNode(pageIdx = startPageIdx, path = emptyList())

        // 阶段3：直接尝试增强的搜索方式，确保能够找到多步骤路径
        println("Trying enhanced search first...")
        when (userGoal.searchStrategy) {
            SearchStrategy.BFS -> {
                val result = enhancedBfs(initialNode, targetActions, targetPages, actionToPageMap)
                if (result.success) {
                    println("=== Enhanced BFS successful with path: ${result.actionPath}")
                    return result
                }
            }
            SearchStrategy.DFS -> {
                val result = enhancedDfs(initialNode, targetActions, targetPages, actionToPageMap)
                if (result.success) {
                    println("=== Enhanced DFS successful with path: ${result.actionPath}")
                    return result
                }
            }
        }
        
        // 阶段4：图搜索 - 寻找最短路径到任何目标页面，然后执行对应的action
        // 先寻找到达目标页面的路径
        var bestPath: List<Int> = emptyList()
        var bestActionId: Int = -1
        var found = false
        
        when (userGoal.searchStrategy) {
            SearchStrategy.BFS -> {
                println("Using BFS strategy")
                // 对每个目标页面执行BFS
                for (targetPageIdx in targetPages) {
                    println("Searching for path to target page: $targetPageIdx")
                    val pathToPage = bfsToPage(initialNode, targetPageIdx)
                    if (pathToPage.success) {
                        println("Found path to target page $targetPageIdx: ${pathToPage.actionPath}")
                        // 找到路径后，查找该页面上的目标action
                        val pageActions = uiMap.transition[targetPageIdx] ?: emptyMap()
                        println("Actions on target page $targetPageIdx: ${pageActions.keys}")
                        for ((actionId, _) in pageActions) {
                            if (actionId in targetActions && actionToPageMap[actionId] == targetPageIdx) {
                                bestPath = pathToPage.actionPath + actionId
                                bestActionId = actionId
                                found = true
                                println("Found target action $actionId on page $targetPageIdx")
                                println("Best path: $bestPath")
                                break
                            }
                        }
                        if (found) break
                    }
                }
            }
            SearchStrategy.DFS -> {
                println("Using DFS strategy")
                // 对每个目标页面执行DFS
                for (targetPageIdx in targetPages) {
                    println("Searching for path to target page: $targetPageIdx")
                    val pathToPage = dfsToPage(initialNode, targetPageIdx)
                    if (pathToPage.success) {
                        println("Found path to target page $targetPageIdx: ${pathToPage.actionPath}")
                        // 找到路径后，查找该页面上的目标action
                        val pageActions = uiMap.transition[targetPageIdx] ?: emptyMap()
                        println("Actions on target page $targetPageIdx: ${pageActions.keys}")
                        for ((actionId, _) in pageActions) {
                            if (actionId in targetActions && actionToPageMap[actionId] == targetPageIdx) {
                                bestPath = pathToPage.actionPath + actionId
                                bestActionId = actionId
                                found = true
                                println("Found target action $actionId on page $targetPageIdx")
                                println("Best path: $bestPath")
                                break
                            }
                        }
                        if (found) break
                    }
                }
            }
        }
        
        if (found && bestPath.isNotEmpty()) {
            println("=== Planning successful with path: $bestPath")
            return PlanResult(
                success = true,
                actionPath = bestPath,
                reason = null
            )
        }
        
        // 阶段5：尝试原有的搜索方式
        println("All search strategies failed, trying original search...")
        val originalResult = when (userGoal.searchStrategy) {
            SearchStrategy.BFS -> bfs(initialNode, targetActions)
            SearchStrategy.DFS -> dfs(initialNode, targetActions)
        }
        
        println("=== Original search result: ${originalResult.success}, path: ${originalResult.actionPath}, reason: ${originalResult.reason}")
        return originalResult
    }
    
    /**
     * 增强的BFS搜索，寻找经过多个中间页面到达目标action的路径
     */
    private fun enhancedBfs(initialNode: PlannerNode, targetActions: List<Int>, targetPages: Set<Int>, actionToPageMap: Map<Int, Int>): PlanResult {
        val queue = ArrayDeque<PlannerNode>()
        val visited = mutableSetOf<Pair<Int, Int>>() // (pageIdx, actionId) 对

        queue.add(initialNode)

        while (queue.isNotEmpty()) {
            val currentNode = queue.removeFirst()

            // 检查当前页面是否是目标页面之一
            if (currentNode.pageIdx in targetPages) {
                // 查找该页面上的目标action
                val pageActions = uiMap.transition[currentNode.pageIdx] ?: emptyMap()
                for ((actionId, _) in pageActions) {
                    if (actionId in targetActions && actionToPageMap[actionId] == currentNode.pageIdx) {
                        return PlanResult(
                            success = true,
                            actionPath = currentNode.path + actionId,
                            reason = null
                        )
                    }
                }
            }

            // 获取当前页面的所有可执行action
            val actions = uiMap.transition[currentNode.pageIdx] ?: emptyMap()

            for ((actionId, toPages) in actions) {
                // 检查是否到达目标
                if (actionId in targetActions && actionToPageMap[actionId] == currentNode.pageIdx) {
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

                // 处理返回按钮等特殊情况（toPages为空数组）
                if (toPages.isEmpty()) {
                    // 对于返回按钮等动作，假设它会返回到上一个页面
                    // 我们仍然将其纳入搜索路径，让执行器尝试执行这个动作
                    val newPath = currentNode.path + actionId
                    // 暂时保持在当前页面，因为我们不知道返回后会到哪个页面
                    val newNode = PlannerNode(pageIdx = currentNode.pageIdx, path = newPath)
                    queue.add(newNode)
                } else {
                    // 遍历所有可能的跳转页面
                    for (toPageIdx in toPages) {
                        val newPath = currentNode.path + actionId
                        val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                        queue.add(newNode)
                    }
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
     * 增强的DFS搜索，寻找经过多个中间页面到达目标action的路径
     */
    private fun enhancedDfs(initialNode: PlannerNode, targetActions: List<Int>, targetPages: Set<Int>, actionToPageMap: Map<Int, Int>): PlanResult {
        val stack = ArrayDeque<PlannerNode>()
        val visited = mutableSetOf<Pair<Int, Int>>() // (pageIdx, actionId) 对

        stack.add(initialNode)

        while (stack.isNotEmpty()) {
            val currentNode = stack.removeLast()

            // 检查当前页面是否是目标页面之一
            if (currentNode.pageIdx in targetPages) {
                // 查找该页面上的目标action
                val pageActions = uiMap.transition[currentNode.pageIdx] ?: emptyMap()
                for ((actionId, _) in pageActions) {
                    if (actionId in targetActions && actionToPageMap[actionId] == currentNode.pageIdx) {
                        return PlanResult(
                            success = true,
                            actionPath = currentNode.path + actionId,
                            reason = null
                        )
                    }
                }
            }

            // 获取当前页面的所有可执行action
            val actions = uiMap.transition[currentNode.pageIdx] ?: emptyMap()

            for ((actionId, toPages) in actions) {
                // 检查是否到达目标
                if (actionId in targetActions && actionToPageMap[actionId] == currentNode.pageIdx) {
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

                // 处理返回按钮等特殊情况（toPages为空数组）
                if (toPages.isEmpty()) {
                    // 对于返回按钮等动作，假设它会返回到上一个页面
                    // 我们仍然将其纳入搜索路径，让执行器尝试执行这个动作
                    val newPath = currentNode.path + actionId
                    // 暂时保持在当前页面，因为我们不知道返回后会到哪个页面
                    val newNode = PlannerNode(pageIdx = currentNode.pageIdx, path = newPath)
                    stack.add(newNode)
                } else {
                    // 遍历所有可能的跳转页面
                    for (toPageIdx in toPages) {
                        val newPath = currentNode.path + actionId
                        val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                        stack.add(newNode)
                    }
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

                // 处理返回按钮等特殊情况（toPages为空数组）
                if (toPages.isEmpty()) {
                    // 对于返回按钮等动作，假设它会返回到上一个页面
                    // 我们仍然将其纳入搜索路径，让执行器尝试执行这个动作
                    val newPath = currentNode.path + actionId
                    // 暂时保持在当前页面，因为我们不知道返回后会到哪个页面
                    val newNode = PlannerNode(pageIdx = currentNode.pageIdx, path = newPath)
                    queue.add(newNode)
                } else {
                    // 遍历所有可能的跳转页面
                    for (toPageIdx in toPages) {
                        val newPath = currentNode.path + actionId
                        val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                        queue.add(newNode)
                    }
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

                // 处理返回按钮等特殊情况（toPages为空数组）
                if (toPages.isEmpty()) {
                    // 对于返回按钮等动作，假设它会返回到上一个页面
                    // 我们仍然将其纳入搜索路径，让执行器尝试执行这个动作
                    val newPath = currentNode.path + actionId
                    // 暂时保持在当前页面，因为我们不知道返回后会到哪个页面
                    val newNode = PlannerNode(pageIdx = currentNode.pageIdx, path = newPath)
                    stack.add(newNode)
                } else {
                    // 遍历所有可能的跳转页面
                    for (toPageIdx in toPages) {
                        val newPath = currentNode.path + actionId
                        val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                        stack.add(newNode)
                    }
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

                // 处理返回按钮等特殊情况（toPages为空数组）
                if (toPages.isEmpty()) {
                    // 对于返回按钮等动作，假设它会返回到上一个页面
                    // 我们仍然将其纳入搜索路径，让执行器尝试执行这个动作
                    val newPath = currentNode.path + actionId
                    // 暂时保持在当前页面，因为我们不知道返回后会到哪个页面
                    val newNode = PlannerNode(pageIdx = currentNode.pageIdx, path = newPath)
                    queue.add(newNode)
                } else {
                    // 遍历所有可能的跳转页面
                    for (toPageIdx in toPages) {
                        val newPath = currentNode.path + actionId
                        val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                        queue.add(newNode)
                    }
                }
            }
        }

        // 未找到路径，尝试使用返回按钮策略
        return tryReturnPath(initialNode, targetPageIdx)
    }
    
    /**
     * 尝试使用返回按钮策略寻找路径
     * @param initialNode 初始节点
     * @param targetPageIdx 目标页面索引
     * @return 路径规划结果
     */
    private fun tryReturnPath(initialNode: PlannerNode, targetPageIdx: Int): PlanResult {
        // 检查是否已经在目标页面
        if (initialNode.pageIdx == targetPageIdx) {
            return PlanResult(
                success = true,
                actionPath = initialNode.path,
                reason = null
            )
        }
        
        // 获取当前页面的所有可执行action
        val actions = uiMap.transition[initialNode.pageIdx] ?: emptyMap()
        
        // 查找返回按钮
        for ((actionId, toPages) in actions) {
            // 检查是否是返回按钮（toPages为空数组）
            if (toPages.isEmpty()) {
                // 创建新的路径，包含返回按钮动作
                val newPath = initialNode.path + actionId
                
                // 尝试直接执行返回按钮动作，然后从主页面寻找路径
                // 这里我们假设返回按钮会返回到主页面
                val mainPageIdx = uiMap.page_index["MainActivity"] ?: return PlanResult(
                    success = false,
                    actionPath = emptyList(),
                    reason = REASON_NO_PATH_FOUND
                )
                
                // 检查是否有从主页面到目标页面的路径
                val result = findPathFromPage(mainPageIdx, targetPageIdx, newPath)
                if (result.success) {
                    return result
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
     * 从指定页面寻找路径到目标页面
     * @param startPageIdx 开始页面索引
     * @param targetPageIdx 目标页面索引
     * @param basePath 基础路径
     * @return 路径规划结果
     */
    private fun findPathFromPage(startPageIdx: Int, targetPageIdx: Int, basePath: List<Int>): PlanResult {
        // 检查是否已经在目标页面
        if (startPageIdx == targetPageIdx) {
            return PlanResult(
                success = true,
                actionPath = basePath,
                reason = null
            )
        }
        
        val queue = ArrayDeque<Pair<Int, List<Int>>>() // (pageIdx, path)
        val visited = mutableSetOf<Int>() // 已访问的页面
        
        queue.add(Pair(startPageIdx, basePath))
        visited.add(startPageIdx)
        
        while (queue.isNotEmpty()) {
            val (currentPageIdx, currentPath) = queue.removeFirst()
            
            // 获取当前页面的所有可执行action
            val actions = uiMap.transition[currentPageIdx] ?: emptyMap()
            
            for ((actionId, toPages) in actions) {
                // 检查是否到达目标页面
                if (toPages.contains(targetPageIdx)) {
                    return PlanResult(
                        success = true,
                        actionPath = currentPath + actionId,
                        reason = null
                    )
                }
                
                // 遍历所有可能的跳转页面
                for (toPageIdx in toPages) {
                    if (toPageIdx !in visited) {
                        visited.add(toPageIdx)
                        queue.add(Pair(toPageIdx, currentPath + actionId))
                    }
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

                // 处理返回按钮等特殊情况（toPages为空数组）
                if (toPages.isEmpty()) {
                    // 对于返回按钮等动作，假设它会返回到上一个页面
                    // 我们仍然将其纳入搜索路径，让执行器尝试执行这个动作
                    val newPath = currentNode.path + actionId
                    // 暂时保持在当前页面，因为我们不知道返回后会到哪个页面
                    val newNode = PlannerNode(pageIdx = currentNode.pageIdx, path = newPath)
                    stack.add(newNode)
                } else {
                    // 遍历所有可能的跳转页面
                    for (toPageIdx in toPages) {
                        val newPath = currentNode.path + actionId
                        val newNode = PlannerNode(pageIdx = toPageIdx, path = newPath)
                        stack.add(newNode)
                    }
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