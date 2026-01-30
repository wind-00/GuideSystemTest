package com.example.executor.core

import com.example.executor.action.AtomicActionExecutor
import com.example.executor.planner.ActionPath
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType
import com.example.executor.result.ExecuteFailReason
import com.example.executor.result.ExecuteResult
import com.example.executor.state.PageStateProvider
import com.example.executor.util.UiStabilizer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class ExecutionEngineTest {

    private val pageStateProvider = mockk<PageStateProvider>()
    private val atomicActionExecutor = mockk<AtomicActionExecutor>()
    private val uiStabilizer = mockk<UiStabilizer>()

    private val executionEngine = ExecutionEngine(
        stateProvider = pageStateProvider,
        atomicActionExecutor = atomicActionExecutor,
        uiStabilizer = uiStabilizer
    )

    @Test
    fun `execute should return success when all steps are executed successfully`() {
        // Given
        val startPageId = "MainActivity"
        val steps = listOf(
            ActionStep(1, "button1", TriggerType.CLICK),
            ActionStep(2, "button2", TriggerType.CLICK)
        )
        val actionPath = ActionPath(startPageId, steps)

        every { pageStateProvider.getCurrentPageId() } returns startPageId andThen "SecondActivity" andThen "SecondActivity"
        every { atomicActionExecutor.execute(any()) } returns com.example.executor.action.AtomicExecuteResult.Success
        every { uiStabilizer.waitForIdle() } returns true

        // When
        val result = executionEngine.execute(actionPath)

        // Then
        assertEquals(ExecuteResult.Success, result)
    }

    @Test
    fun `execute should return page mismatch when current page is different from start page`() {
        // Given
        val startPageId = "MainActivity"
        val currentPageId = "OtherActivity"
        val steps = listOf(ActionStep(1, "button1", TriggerType.CLICK))
        val actionPath = ActionPath(startPageId, steps)

        every { pageStateProvider.getCurrentPageId() } returns currentPageId

        // When
        val result = executionEngine.execute(actionPath)

        // Then
        assertEquals(
            ExecuteResult.Failed(0, steps[0], ExecuteFailReason.PAGE_MISMATCH),
            result
        )
    }

    @Test
    fun `execute should return failed when atomic action execution fails`() {
        // Given
        val startPageId = "MainActivity"
        val steps = listOf(ActionStep(1, "button1", TriggerType.CLICK))
        val actionPath = ActionPath(startPageId, steps)

        every { pageStateProvider.getCurrentPageId() } returns startPageId
        every { atomicActionExecutor.execute(any()) } returns 
            com.example.executor.action.AtomicExecuteResult.Fail(ExecuteFailReason.COMPONENT_NOT_FOUND)

        // When
        val result = executionEngine.execute(actionPath)

        // Then
        assertEquals(
            ExecuteResult.Failed(0, steps[0], ExecuteFailReason.COMPONENT_NOT_FOUND),
            result
        )
    }

    @Test
    fun `execute should return timeout when ui stabilization times out`() {
        // Given
        val startPageId = "MainActivity"
        val steps = listOf(ActionStep(1, "button1", TriggerType.CLICK))
        val actionPath = ActionPath(startPageId, steps)

        every { pageStateProvider.getCurrentPageId() } returns startPageId
        every { atomicActionExecutor.execute(any()) } returns com.example.executor.action.AtomicExecuteResult.Success
        every { uiStabilizer.waitForIdle() } returns false

        // When
        val result = executionEngine.execute(actionPath)

        // Then
        assertEquals(
            ExecuteResult.Failed(0, steps[0], ExecuteFailReason.TIMEOUT),
            result
        )
    }

    @Test
    fun `execute should return page not changed when page remains the same after action`() {
        // Given
        val startPageId = "MainActivity"
        val steps = listOf(
            ActionStep(1, "button1", TriggerType.CLICK),
            ActionStep(2, "button2", TriggerType.CLICK)
        )
        val actionPath = ActionPath(startPageId, steps)

        every { pageStateProvider.getCurrentPageId() } returns startPageId andThen startPageId andThen startPageId
        every { atomicActionExecutor.execute(any()) } returns com.example.executor.action.AtomicExecuteResult.Success
        every { uiStabilizer.waitForIdle() } returns true

        // When
        val result = executionEngine.execute(actionPath)

        // Then
        assertEquals(
            ExecuteResult.Failed(0, steps[0], ExecuteFailReason.PAGE_NOT_CHANGED),
            result
        )
    }
}
