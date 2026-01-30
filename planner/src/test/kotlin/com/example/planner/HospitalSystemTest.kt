package com.example.planner

import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

class HospitalSystemTest {
    @Test
    fun `test can find 查看挂号 action`() {
        // 读取 fsm_transition.json 文件
        val jsonFile = File("c:\\Users\\13210\\AndroidStudioProjects\\GuideSystemTest\\fsm_transition.json")
        val jsonContent = jsonFile.readText()
        
        // 解析为 UiMapModel
        val uiMap = Json.decodeFromString<UiMapModel>(jsonContent)
        
        // 检查 visible_text_index 是否包含 "查看挂号"
        val hasViewAppointment = uiMap.visible_text_index.containsKey("查看挂号")
        assertTrue(hasViewAppointment, "visible_text_index should contain \"查看挂号\"")
        
        // 检查 "查看挂号" 对应的 actionId
        val viewAppointmentActions = uiMap.visible_text_index["查看挂号"]
        assertTrue(viewAppointmentActions != null && viewAppointmentActions.isNotEmpty(), "\"查看挂号\" should have at least one actionId")
        
        // 打印 "查看挂号" 对应的 actionId
        println("查看挂号 对应的 actionId: $viewAppointmentActions")
        
        // 检查 action_metadata 是否包含对应的 actionId
        for (actionId in viewAppointmentActions!!) {
            val actionMeta = uiMap.action_metadata[actionId]
            assertTrue(actionMeta != null, "action_metadata should contain actionId $actionId")
            println("ActionId $actionId: ${actionMeta.componentId}, ${actionMeta.visibleText}, ${actionMeta.page}")
        }
    }
    
    @Test
    fun `test planner can plan for 查看挂号`() {
        // 读取 fsm_transition.json 文件
        val jsonFile = File("c:\\Users\\13210\\AndroidStudioProjects\\GuideSystemTest\\fsm_transition.json")
        val jsonContent = jsonFile.readText()
        
        // 解析为 UiMapModel
        val uiMap = Json.decodeFromString<UiMapModel>(jsonContent)
        
        // 打印 uiMap 的基本信息
        println("\n=== UI Map Information ===")
        println("Page count: ${uiMap.page_index.size}")
        println("Action count: ${uiMap.action_index.size}")
        println("Action metadata count: ${uiMap.action_metadata.size}")
        println("Visible text count: ${uiMap.visible_text_index.size}")
        println("Transition count: ${uiMap.transition.size}")
        
        // 检查 SecondActivity1 的 pageIndex
        val secondActivity1Index = uiMap.page_index["SecondActivity1"]
        println("\n=== SecondActivity1 Information ===")
        println("SecondActivity1 pageIndex: $secondActivity1Index")
        
        // 检查 SecondActivity1 的 transition
        if (secondActivity1Index != null) {
            val secondActivity1Transitions = uiMap.transition[secondActivity1Index]
            println("SecondActivity1 transitions: $secondActivity1Transitions")
        }
        
        // 创建 Planner 实例
        val planner = Planner(uiMap)
        
        // 创建 UserGoal
        val userGoal = UserGoal(
            targetVisibleText = "查看挂号",
            startPage = "SecondActivity1",
            searchStrategy = SearchStrategy.BFS
        )
        
        // 执行规划
        val result = planner.plan(userGoal)
        
        // 检查规划是否成功
        assertTrue(result.success, "Planner should find path for \"查看挂号\"")
        assertTrue(result.actionPath.isNotEmpty(), "Action path should not be empty")
        
        // 打印规划结果
        println("\n=== Plan Result ===")
        println("Plan result: ${result.success}")
        println("Action path: ${result.actionPath}")
        println("Reason: ${result.reason}")
        
        // 打印路径中的每个 action 的详细信息
        println("\n=== Path Details ===")
        for ((index, actionId) in result.actionPath.withIndex()) {
            val actionMeta = uiMap.action_metadata[actionId]
            println("Step $index: ActionId $actionId, ComponentId: ${actionMeta?.componentId}, VisibleText: ${actionMeta?.visibleText}, Page: ${actionMeta?.page}")
        }
    }
    
    @Test
    fun `test planner can plan for 预约挂号`() {
        // 读取 fsm_transition.json 文件
        val jsonFile = File("c:\\Users\\13210\\AndroidStudioProjects\\GuideSystemTest\\fsm_transition.json")
        val jsonContent = jsonFile.readText()
        
        // 解析为 UiMapModel
        val uiMap = Json.decodeFromString<UiMapModel>(jsonContent)
        
        // 创建 Planner 实例
        val planner = Planner(uiMap)
        
        // 创建 UserGoal
        val userGoal = UserGoal(
            targetVisibleText = "预约挂号",
            startPage = "SecondActivity1",
            searchStrategy = SearchStrategy.BFS
        )
        
        // 执行规划
        val result = planner.plan(userGoal)
        
        // 检查规划是否成功
        assertTrue(result.success, "Planner should find path for \"预约挂号\"")
        assertTrue(result.actionPath.isNotEmpty(), "Action path should not be empty")
        
        // 打印规划结果
        println("Plan result: ${result.success}")
        println("Action path: ${result.actionPath}")
        println("Reason: ${result.reason}")
    }
}