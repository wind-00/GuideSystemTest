package com.example.orchestrator.control

import android.content.Context
import com.example.orchestrator.executor.ExecutorClient
import com.example.orchestrator.model.OrchestratorStatus
import com.example.orchestrator.model.PlanningResult
import com.example.orchestrator.model.UserRequest
import com.example.orchestrator.overlay.OverlayService
import com.example.orchestrator.planner.PlannerClient
import com.example.orchestrator.state.RuntimeStateProvider
import com.example.executor.planner.ActionPath
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType
import com.example.executor.result.ExecuteResult
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OrchestratorImplTest {
    
    private lateinit var context: Context
    private lateinit var runtimeStateProvider: RuntimeStateProvider
    private lateinit var plannerClient: PlannerClient
    private lateinit var executorClient: ExecutorClient
    private lateinit var overlayService: OverlayService
    private lateinit var orchestrator: OrchestratorImpl
    
    @Before
    fun setUp() {
        context = mockk()
        runtimeStateProvider = mockk()
        plannerClient = mockk()
        executorClient = mockk()
        overlayService = mockk()
        
        every { overlayService.setServiceListener(any()) } just Runs
        every { overlayService.updateStatus(any()) } just Runs
        
        orchestrator = OrchestratorImpl(
            context = context,
            runtimeStateProvider = runtimeStateProvider,
            plannerClient = plannerClient,
            executorClient = executorClient,
            overlayService = overlayService
        )
    }
    
    @Test
    fun `test startExecution with valid request`() {
        // 模拟获取当前页面 ID
        every { runtimeStateProvider.getCurrentPageId() } returns "page1"
        
        // 模拟规划结果
        val actionStep = ActionStep(
            actionId = 1,
            componentId = "component1",
            triggerType = TriggerType.CLICK
        )
        val actionPath = ActionPath(
            startPageId = "page1",
            steps = listOf(actionStep)
        )
        val planningResult = PlanningResult.Success(actionPath)
        every { plannerClient.plan(any()) } returns planningResult
        
        // 模拟执行结果
        every { executorClient.execute(any(), any()) } returns ExecuteResult.Success
        
        // 执行测试
        orchestrator.startExecution("test command")
        
        // 验证状态更新
        verifySequence {
            overlayService.updateStatus(OrchestratorStatus.PLANNING)
            overlayService.updateStatus(OrchestratorStatus.EXECUTING)
            overlayService.updateStatus(OrchestratorStatus.COMPLETED)
        }
    }
    
    @Test
    fun `test startExecution with planning failure`() {
        // 模拟获取当前页面 ID
        every { runtimeStateProvider.getCurrentPageId() } returns "page1"
        
        // 模拟规划失败
        val planningResult = PlanningResult.Failed("Planning failed")
        every { plannerClient.plan(any()) } returns planningResult
        
        // 执行测试
        orchestrator.startExecution("test command")
        
        // 验证状态更新
        verifySequence {
            overlayService.updateStatus(OrchestratorStatus.PLANNING)
            overlayService.updateStatus(OrchestratorStatus.FAILED)
        }
    }
    
    @Test
    fun `test startExecution with execution failure`() {
        // 模拟获取当前页面 ID
        every { runtimeStateProvider.getCurrentPageId() } returns "page1"
        
        // 模拟规划结果
        val actionStep = ActionStep(
            actionId = 1,
            componentId = "component1",
            triggerType = TriggerType.CLICK
        )
        val actionPath = ActionPath(
            startPageId = "page1",
            steps = listOf(actionStep)
        )
        val planningResult = PlanningResult.Success(actionPath)
        every { plannerClient.plan(any()) } returns planningResult
        
        // 模拟执行失败
        every { executorClient.execute(any(), any()) } returns ExecuteResult.Failed(
            stepIndex = 0,
            action = actionStep,
            reason = com.example.executor.result.ExecuteFailReason.COMPONENT_NOT_FOUND
        )
        
        // 执行测试
        orchestrator.startExecution("test command")
        
        // 验证状态更新
        verifySequence {
            overlayService.updateStatus(OrchestratorStatus.PLANNING)
            overlayService.updateStatus(OrchestratorStatus.EXECUTING)
            overlayService.updateStatus(OrchestratorStatus.FAILED)
        }
    }
    
    @Test
    fun `test stopExecution`() {
        // 执行测试
        orchestrator.stopExecution()
        
        // 验证状态更新
        verify { overlayService.updateStatus(OrchestratorStatus.IDLE) }
    }
    
    @Test
    fun `test getCurrentStatus`() {
        // 验证初始状态
        assert(orchestrator.getCurrentStatus() == OrchestratorStatus.IDLE)
    }
}
