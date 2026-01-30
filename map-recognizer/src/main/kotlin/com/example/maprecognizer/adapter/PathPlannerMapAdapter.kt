package com.example.maprecognizer.adapter

import com.example.maprecognizer.data.*

/**
 * 将AppAutomationMap转换为path-planner期望的UIMap格式
 */
class PathPlannerMapAdapter {
    
    /**
     * 将AppAutomationMap转换为path-planner期望的JSON字符串
     */
    fun convertToPathPlannerJson(appMap: AppAutomationMap): String {
        // 创建path-planner格式的地图
        val pathPlannerMap = mutableMapOf<String, Any>()
        
        // 创建状态映射
        val statesMap = mutableMapOf<String, Any>()
        
        // 遍历所有状态
        for (state in appMap.stateModel.states) {
            // 查找对应的页面
            val page = appMap.uiModel.pages.find { it.pageId == state.stateId }
            if (page != null) {
                // 创建状态对象
                val stateObj = mutableMapOf<String, Any>()
                stateObj["stateId"] = state.stateId
                stateObj["description"] = state.description
                
                // 创建组件列表
                val componentsList = mutableListOf<Map<String, Any>>()
                
                // 遍历页面组件
                for (component in page.components) {
                    // 创建组件对象
                    val componentObj = mutableMapOf<String, Any>()
                    componentObj["componentId"] = component.componentId
                    componentObj["type"] = component.viewType.name
                    componentObj["text"] = component.text ?: ""
                    
                    // 添加组件属性
                    val properties = mutableMapOf<String, String>()
                    properties["enabled"] = component.enabled.toString()
                    componentObj["properties"] = properties
                    
                    // 查找该组件的所有意图
                    val componentIntents = appMap.intentModel.intents
                        .filter { intent -> 
                            intent.fromStateId == state.stateId &&
                            intent.uiBindings.any { binding -> binding.componentId == component.componentId }
                        }
                    
                    // 创建组件意图列表
                    val intentsList = mutableListOf<Map<String, Any>>()
                    for (intent in componentIntents) {
                        val binding = intent.uiBindings.first { it.componentId == component.componentId }
                        val intentObj = mutableMapOf<String, Any>()
                        intentObj["intentId"] = intent.intentId
                        intentObj["type"] = binding.trigger.name
                        intentObj["targetStateId"] = intent.toStateId
                        intentObj["description"] = intent.description
                        intentObj["parameters"] = binding.parameters
                        intentsList.add(intentObj)
                    }
                    componentObj["intents"] = intentsList
                    
                    componentsList.add(componentObj)
                }
                stateObj["components"] = componentsList
                
                // 创建状态意图列表
                val stateIntents = appMap.intentModel.intents
                    .filter { it.fromStateId == state.stateId }
                    .map { intent ->
                        val binding = intent.uiBindings.firstOrNull()
                        mapOf(
                            "intentId" to intent.intentId,
                            "type" to intent.type.name,
                            "targetStateId" to intent.toStateId,
                            "description" to intent.description,
                            "parameters" to (binding?.parameters ?: emptyMap<String, String>())
                        )
                    }
                stateObj["intents"] = stateIntents
                
                statesMap[state.stateId] = stateObj
            }
        }
        
        pathPlannerMap["states"] = statesMap
        pathPlannerMap["version"] = "1.0"
        
        // 将map转换为JSON字符串
        return jsonStringify(pathPlannerMap)
    }
    
    /**
     * 将Map转换为JSON字符串
     */
    private fun jsonStringify(map: Map<String, Any>): String {
        return buildString { 
            append("{")
            var first = true
            for ((key, value) in map) {
                if (!first) append(",")
                first = false
                append("\"$key\":")
                append(jsonValueToString(value))
            }
            append("}")
        }
    }
    
    /**
     * 将Any转换为JSON字符串
     */
    private fun jsonValueToString(value: Any): String {
        return when (value) {
            is String -> "\"$value\""
            is Number -> value.toString()
            is Boolean -> value.toString()
            is Map<*, *> -> {
                buildString { 
                    append("{")
                    var first = true
                    for ((key, v) in value) {
                        if (!first) append(",")
                        first = false
                        append("\"$key\":")
                        append(jsonValueToString(v!!))
                    }
                    append("}")
                }
            }
            is List<*> -> {
                buildString { 
                    append("[")
                    var first = true
                    for (v in value) {
                        if (!first) append(",")
                        first = false
                        append(jsonValueToString(v!!))
                    }
                    append("]")
                }
            }
            else -> "\"$value\""
        }
    }
}