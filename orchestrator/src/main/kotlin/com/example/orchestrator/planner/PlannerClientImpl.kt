package com.example.orchestrator.planner

import com.example.orchestrator.model.PlanningResult
import com.example.orchestrator.model.UserRequest
import com.example.planner.PlanResult
import com.example.planner.Planner
import com.example.planner.SearchStrategy
import com.example.planner.UserGoal
import com.example.executor.planner.ActionPath
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType
import org.json.JSONObject
import java.io.File
import java.io.IOException

class PlannerClientImpl(private val planner: Planner) : PlannerClient {
    
    // 缓存的actionId到componentId的映射
    private val actionIdToComponentIdMap = mutableMapOf<Int, String>()
    
    // 初始化时加载映射
    init {
        loadActionIdToComponentIdMap()
    }
    
    override fun plan(request: UserRequest): PlanningResult {
        val userGoal = UserGoal(
            targetVisibleText = request.rawText,
            startPage = request.startPageId,
            searchStrategy = SearchStrategy.BFS
        )
        
        val planResult = planner.plan(userGoal)
        
        return if (planResult.success) {
            val actionSteps = planResult.actionPath.map {
                // 根据 actionId 获取对应的 ActionStep 信息
                // 从缓存的映射中获取 componentId
                val componentId = getComponentIdForActionId(it)
                
                ActionStep(
                    actionId = it,
                    componentId = componentId,
                    triggerType = TriggerType.CLICK
                )
            }
            
            PlanningResult.Success(
                ActionPath(
                    startPageId = request.startPageId,
                    steps = actionSteps
                )
            )
        } else {
            PlanningResult.Failed(planResult.reason ?: "Unknown error")
        }
    }
    
    /**
     * 加载fsm_transition.json文件，构建actionId到componentId的映射
     */
    private fun loadActionIdToComponentIdMap() {
        try {
            // 尝试从项目根目录读取fsm_transition.json文件
            val jsonFile = File("fsm_transition.json")
            if (jsonFile.exists()) {
                val jsonContent = jsonFile.readText()
                val jsonObject = JSONObject(jsonContent)
                
                // 解析action_metadata部分
                if (jsonObject.has("action_metadata")) {
                    val actionMetadata = jsonObject.getJSONObject("action_metadata")
                    val keys = actionMetadata.keys()
                    
                    while (keys.hasNext()) {
                        val key = keys.next()
                        try {
                            val actionId = key.toInt()
                            val metadata = actionMetadata.getJSONObject(key)
                            if (metadata.has("componentId")) {
                                val componentId = metadata.getString("componentId")
                                actionIdToComponentIdMap[actionId] = componentId
                            }
                        } catch (e: NumberFormatException) {
                            // 忽略非数字的key
                        }
                    }
                    
                    println("成功加载actionId到componentId映射，共${actionIdToComponentIdMap.size}个映射")
                }
            } else {
                println("fsm_transition.json文件不存在，使用默认映射")
                loadDefaultMap()
            }
        } catch (e: IOException) {
            println("读取fsm_transition.json文件失败: ${e.message}")
            loadDefaultMap()
        } catch (e: Exception) {
            println("解析fsm_transition.json文件失败: ${e.message}")
            loadDefaultMap()
        }
    }
    
    /**
     * 加载默认映射作为后备
     */
    private fun loadDefaultMap() {
        // 加载默认映射，与原来的硬编码映射一致
        val defaultMap = mapOf(
            0 to "auto_back_btn",
            1 to "btnBack",
            2 to "btnIcon",
            3 to "btnLongClick",
            4 to "btnNormal",
            5 to "btnOption1",
            6 to "btnOption2",
            7 to "btnOption3",
            8 to "btnOption4",
            9 to "btnToSecond1",
            10 to "btnToSecond2",
            11 to "checkbox1",
            12 to "checkbox2",
            13 to "longClickArea",
            14 to "radioGroup",
            15 to "seekBarHorizontal",
            16 to "seekBarVertical",
            17 to "slider",
            18 to "switch1",
            19 to "switch2",
            20 to "switch3",
            21 to "switchButton",
            22 to "taskBtnBack",
            23 to "taskBtnCompleted",
            24 to "taskBtnCreate",
            25 to "taskBtnDelete",
            26 to "taskBtnEdit",
            27 to "taskBtnPending",
            28 to "taskBtnSearch",
            29 to "taskCardStats",
            30 to "taskFilterAll",
            31 to "taskFilterPersonal",
            32 to "taskFilterUrgent",
            33 to "taskFilterWork"
        )
        
        actionIdToComponentIdMap.putAll(defaultMap)
    }
    
    /**
     * 根据actionId获取对应的componentId
     */
    private fun getComponentIdForActionId(actionId: Int): String {
        return actionIdToComponentIdMap[actionId] ?: ""
    }
}