package com.example.pathplanner.locator

import com.example.pathplanner.models.ComponentTarget
import com.example.pathplanner.models.StateTarget
import com.example.pathplanner.models.TargetSpec
import com.example.pathplanner.models.UIMap

/**
 * 目标定位器，负责在UI地图中查找匹配目标规范的节点
 */
class TargetLocator {
    
    /**
     * 在UI地图中查找匹配目标规范的所有状态ID
     * @param uiMap UI地图
     * @param targetSpec 目标规范
     * @return 匹配的状态ID集合
     */
    fun locate(uiMap: UIMap, targetSpec: TargetSpec): Set<String> {
        return when (targetSpec) {
            is StateTarget -> locateStateTarget(uiMap, targetSpec)
            is ComponentTarget -> locateComponentTarget(uiMap, targetSpec)
        }
    }
    
    /**
     * 查找匹配StateTarget的状态ID
     */
    private fun locateStateTarget(uiMap: UIMap, targetSpec: StateTarget): Set<String> {
        return if (uiMap.states.containsKey(targetSpec.stateId)) {
            setOf(targetSpec.stateId)
        } else {
            emptySet()
        }
    }
    
    /**
     * 查找匹配ComponentTarget的所有状态ID
     */
    private fun locateComponentTarget(uiMap: UIMap, targetSpec: ComponentTarget): Set<String> {
        val matchingStates = mutableSetOf<String>()
        
        // 遍历所有状态，查找包含匹配组件的状态
        for ((stateId, state) in uiMap.states) {
            if (state.components.any { component -> 
                matchesComponent(component, targetSpec)
            }) {
                matchingStates.add(stateId)
            }
        }
        
        // 直接返回所有匹配的状态ID，不进行过滤
        // 让PathSearcher根据实际情况选择路径
        return matchingStates
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
                
                // 检查是否包含数字
                val targetNumbers = targetId.filter { it.isDigit() }
                val componentNumbers = componentId.filter { it.isDigit() }
                if (targetNumbers.isNotEmpty() && targetNumbers == componentNumbers) {
                    matchScore += 0.5
                }
                
                // 检查单词匹配
                val targetWords = targetId.split("_").filter { it.isNotBlank() }
                val componentWords = componentId.split("_").filter { it.isNotBlank() }
                val commonWords = targetWords.intersect(componentWords)
                if (commonWords.isNotEmpty()) {
                    matchScore += commonWords.size * 0.5
                }
                
                // 检查完整包含关系
                if (targetId.contains(componentId) || componentId.contains(targetId)) {
                    matchScore += 1
                }
                
                // 特殊处理：返回按钮的匹配
                if (targetId.contains("back") || targetId.contains("返回")) {
                    if (componentId.contains("back") || componentId.contains("返回") || componentId.contains("return")) {
                        matchScore += 1.5
                    }
                }
            }
        }
        
        // 检查组件类型（如果提供）
        if (targetSpec.componentType != null) {
            totalPossibleScore += 1
            val targetType = targetSpec.componentType.lowercase()
            val componentType = component.type.lowercase()
            
            // 更灵活的组件类型匹配
            val typeMatch = when {
                componentType == targetType -> 1.0
                targetType.contains(componentType) || componentType.contains(targetType) -> 0.75
                targetType == "menuitem" || targetType == "listitem" || targetType == "button" || targetType == "textview" -> {
                    // 常见组件类型的同义匹配
                    if (componentType in listOf("menuitem", "listitem", "button", "textview", "imagebutton", "view")) {
                        0.75
                    } else {
                        0.25
                    }
                }
                else -> 0.0
            }
            matchScore += typeMatch
        }
        
        // 检查组件文本（如果提供）
        if (targetSpec.componentText != null) {
            totalPossibleScore += 2.0 // 增加文本匹配的权重
            if (component.text != null) {
                val targetText = targetSpec.componentText.lowercase()
                val componentText = component.text.lowercase()
                
                if (componentText == targetText) {
                    matchScore += 2.0
                } else if (componentText.contains(targetText)) {
                    matchScore += 1.5
                } else if (targetText.contains(componentText)) {
                    matchScore += 1.25
                } else {
                    // 检查是否有共同的关键词
                    val targetWords = targetText.split(" ").filter { it.isNotBlank() }
                    val componentWords = componentText.split(" ").filter { it.isNotBlank() }
                    val commonWords = targetWords.intersect(componentWords)
                    if (commonWords.isNotEmpty()) {
                        matchScore += commonWords.size * 0.75
                    }
                    
                    // 检查是否包含核心关键词
                    val coreKeywords = listOf("第二", "third", "层级", "level", "返回", "back", "跳转到", "goto", "btn", "button", "action", "navigate")
                    val targetHasKeyword = coreKeywords.any { targetText.contains(it) }
                    val componentHasKeyword = coreKeywords.any { componentText.contains(it) }
                    if (targetHasKeyword && componentHasKeyword) {
                        matchScore += 1.0
                    }
                    
                    // 特殊处理：返回按钮的文本匹配
                    if (targetText.contains("返回") || targetText.contains("back")) {
                        if (componentText.contains("返回") || componentText.contains("back") || componentSemanticRole.contains("NAVIGATE")) {
                            matchScore += 1.5
                        }
                    }
                }
            }
        }
        
        // 特殊处理：如果组件文本包含目标文本的核心内容，增加匹配分数
        if (targetSpec.componentText != null && component.text != null) {
            val targetText = targetSpec.componentText.lowercase()
            val componentText = component.text.lowercase()
            
            // 移除常见前缀和后缀，如"跳转到"、"点击"等
            val processedTargetText = targetText
                .replace("跳转到", "")
                .replace("点击", "")
                .replace("进入", "")
                .trim()
            
            val processedComponentText = componentText
                .replace("跳转到", "")
                .replace("点击", "")
                .replace("进入", "")
                .trim()
            
            if (processedComponentText.contains(processedTargetText) || processedTargetText.contains(processedComponentText)) {
                matchScore += 1.5
                totalPossibleScore += 1.0
            }
        }
        
        // 检查语义角色匹配
        if (targetSpec.componentText != null) {
            totalPossibleScore += 1.5
            val targetText = targetSpec.componentText.lowercase()
            
            // 语义角色匹配
            if (targetText.contains("返回") || targetText.contains("back")) {
                if (componentSemanticRole.contains("NAVIGATE") || componentSemanticRole.contains("BACK")) {
                    matchScore += 1.5
                }
            } else if (targetText.contains("跳转到") || targetText.contains("goto")) {
                if (componentSemanticRole.contains("ACTION")) {
                    matchScore += 1.0
                }
            }
        }
        
        // 检查组件属性（如果提供）
        if (targetSpec.componentProperties.isNotEmpty()) {
            totalPossibleScore += targetSpec.componentProperties.size
            for ((key, value) in targetSpec.componentProperties) {
                if (component.properties.containsKey(key) && 
                    component.properties[key] == value) {
                    matchScore += 1
                }
            }
        }
        
        // 计算匹配度
        val matchPercentage = if (totalPossibleScore == 0.0) 0.0 else matchScore / totalPossibleScore.toDouble()
        
        // 打印调试信息
        println("[DEBUG] 组件匹配：")
        println("- 组件：${component.componentId} (${component.type}) - ${component.text} - 语义角色：$componentSemanticRole")
        println("- 目标：${targetSpec.componentId} (${targetSpec.componentType}) - ${targetSpec.componentText}")
        println("- 匹配分数：$matchScore / $totalPossibleScore = ${matchPercentage * 100}%")
        
        // 返回匹配度是否大于40%（提高匹配阈值，减少误匹配）
        return matchPercentage >= 0.4
    }
}