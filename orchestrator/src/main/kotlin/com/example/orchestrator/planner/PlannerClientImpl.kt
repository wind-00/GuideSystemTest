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
import android.util.Log

class PlannerClientImpl(private val planner: Planner) : PlannerClient {
    
    companion object {
        private const val TAG = "PlannerClientImpl"
    }
    
    // 缓存的actionId到componentId的映射
    private val actionIdToComponentIdMap = mutableMapOf<Int, String>()
    
    // 上下文，用于读取assets目录中的文件
    private var context: android.content.Context? = null
    
    // 初始化时加载映射
    init {
        loadActionIdToComponentIdMap()
    }
    
    override fun setContext(context: android.content.Context) {
        this.context = context
        // 重新加载映射
        loadActionIdToComponentIdMap()
    }
    
    override fun plan(request: UserRequest): PlanningResult {
        Log.d(TAG, "=== PlannerClientImpl.plan() called ===")
        Log.d(TAG, "Request: rawText='${request.rawText}', startPageId='${request.startPageId}'")
        
        val userGoal = UserGoal(
            targetVisibleText = request.rawText,
            startPage = request.startPageId,
            searchStrategy = SearchStrategy.BFS
        )
        
        Log.d(TAG, "Created UserGoal: targetVisibleText='${userGoal.targetVisibleText}', startPage='${userGoal.startPage}'")
        
        val planResult = planner.plan(userGoal)
        
        Log.d(TAG, "PlanResult: success=${planResult.success}, reason=${planResult.reason}")
        Log.d(TAG, "Action path: ${planResult.actionPath}")
        
        return if (planResult.success) {
            val actionSteps = planResult.actionPath.map {
                // 根据 actionId 获取对应的 ActionStep 信息
                // 从缓存的映射中获取 componentId
                val componentId = getComponentIdForActionId(it)
                Log.d(TAG, "Mapping actionId=$it to componentId='$componentId'")
                
                ActionStep(
                    actionId = it,
                    componentId = componentId,
                    triggerType = TriggerType.CLICK
                )
            }
            
            val actionPath = ActionPath(
                startPageId = request.startPageId,
                steps = actionSteps
            )
            
            Log.d(TAG, "Created ActionPath with ${actionSteps.size} steps")
            PlanningResult.Success(actionPath)
        } else {
            Log.d(TAG, "Planning failed: ${planResult.reason}")
            PlanningResult.Failed(planResult.reason ?: "Unknown error")
        }
    }
    
    /**
     * 加载fsm_transition.json文件，构建actionId到componentId的映射
     */
    private fun loadActionIdToComponentIdMap() {
        try {
            // 清空现有映射
            actionIdToComponentIdMap.clear()
            Log.d(TAG, "开始加载actionId到componentId的映射")
            
            // 尝试从assets目录读取fsm_transition.json文件
            if (context != null) {
                try {
                    Log.d(TAG, "尝试从assets目录读取fsm_transition.json文件")
                    val inputStream = context!!.assets.open("fsm_transition.json")
                    val jsonContent = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()
                    Log.d(TAG, "成功从assets目录读取fsm_transition.json文件，大小: ${jsonContent.length} 字符")
                    parseFsmTransitionJson(jsonContent)
                    return
                } catch (assetError: Exception) {
                    Log.w(TAG, "从assets目录读取fsm_transition.json文件失败: ${assetError.message}")
                }
            } else {
                Log.w(TAG, "上下文为空，无法从assets目录读取fsm_transition.json文件")
            }
            
            // 尝试多个路径读取fsm_transition.json文件
            val possiblePaths = listOf(
                "c:\\Users\\13210\\AndroidStudioProjects\\GuideSystemTest\\fsm_transition.json",
                "fsm_transition.json"
            )
            
            var jsonContent: String? = null
            
            for (path in possiblePaths) {
                val jsonFile = File(path)
                if (jsonFile.exists()) {
                    Log.d(TAG, "从路径加载fsm_transition.json: ${jsonFile.absolutePath}")
                    jsonContent = jsonFile.readText()
                    Log.d(TAG, "成功从文件读取fsm_transition.json，大小: ${jsonContent.length} 字符")
                    break
                }
            }
            
            if (jsonContent != null) {
                parseFsmTransitionJson(jsonContent)
            } else {
                Log.w(TAG, "fsm_transition.json文件不存在，使用默认映射")
                loadDefaultMap()
            }
        } catch (e: IOException) {
            Log.e(TAG, "读取fsm_transition.json文件失败: ${e.message}", e)
            loadDefaultMap()
        } catch (e: Exception) {
            Log.e(TAG, "解析fsm_transition.json文件失败: ${e.message}", e)
            loadDefaultMap()
        }
    }
    
    /**
     * 解析fsm_transition.json文件，构建actionId到componentId的映射
     */
    private fun parseFsmTransitionJson(jsonContent: String) {
        val jsonObject = JSONObject(jsonContent)
        
        // 解析action_metadata部分
        if (jsonObject.has("action_metadata")) {
            val actionMetadata = jsonObject.getJSONObject("action_metadata")
            val keys = actionMetadata.keys()
            var mappedCount = 0
            
            while (keys.hasNext()) {
                val key = keys.next()
                try {
                    val actionId = key.toInt()
                    val metadata = actionMetadata.getJSONObject(key)
                    if (metadata.has("componentId")) {
                        val componentId = metadata.getString("componentId")
                        actionIdToComponentIdMap[actionId] = componentId
                        Log.d(TAG, "添加映射: actionId=$actionId -> componentId=$componentId")
                        mappedCount++
                    }
                } catch (e: NumberFormatException) {
                    // 忽略非数字的key
                }
            }
            
            Log.d(TAG, "成功加载actionId到componentId映射，共${actionIdToComponentIdMap.size}个映射")
            
            // 检查是否加载了关键的映射
            if (actionIdToComponentIdMap.containsKey(35)) {
                Log.d(TAG, "✓ 找到 actionId=35 的映射: ${actionIdToComponentIdMap[35]}")
            } else {
                Log.w(TAG, "✗ 未找到 actionId=35 的映射")
            }
            
            if (actionIdToComponentIdMap.containsKey(36)) {
                Log.d(TAG, "✓ 找到 actionId=36 的映射: ${actionIdToComponentIdMap[36]}")
            } else {
                Log.w(TAG, "✗ 未找到 actionId=36 的映射")
            }
        } else {
            Log.w(TAG, "fsm_transition.json文件中没有action_metadata部分，使用默认映射")
            loadDefaultMap()
        }
    }
    
    /**
     * 加载默认映射作为后备
     */
    private fun loadDefaultMap() {
        Log.d(TAG, "使用默认映射作为后备")
        // 添加所有关键的映射，确保执行器能找到所有必要的组件
        val basicMap = mapOf(
            34 to "btnToSecond1",  // 医院就医按钮
            35 to "btnToSecond2",  // 跳转到第二层级2按钮
            36 to "btnViewAppointment",  // 查看挂号按钮
            37 to "buttonNavigateBack"  // 返回按钮
        )
        
        actionIdToComponentIdMap.putAll(basicMap)
        Log.d(TAG, "添加基本映射，共${basicMap.size}个映射")
        // 打印所有添加的映射
        basicMap.forEach { (actionId, componentId) ->
            Log.d(TAG, "默认映射: actionId=$actionId -> componentId=$componentId")
        }
    }
    
    /**
     * 根据actionId获取对应的componentId
     */
    private fun getComponentIdForActionId(actionId: Int): String {
        val componentId = actionIdToComponentIdMap[actionId]
        if (componentId == null) {
            Log.w(TAG, "未找到 actionId=$actionId 的映射，返回空字符串")
            return ""
        }
        Log.d(TAG, "获取映射: actionId=$actionId -> componentId=$componentId")
        return componentId
    }
}