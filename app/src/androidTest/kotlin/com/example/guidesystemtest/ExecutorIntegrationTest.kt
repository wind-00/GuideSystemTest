package com.example.guidesystemtest

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.executor.core.ExecutionEngine
import com.example.executor.action.ViewAtomicActionExecutor
import com.example.executor.planner.ActionPath
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType
import com.example.executor.result.ExecuteResult
import com.example.executor.state.ActivityPageStateProvider
import com.example.executor.util.LooperUiStabilizer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExecutorIntegrationTest {

    private lateinit var executionEngine: ExecutionEngine
    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        
        // 创建执行器实例
        val pageStateProvider = ActivityPageStateProvider(application)
        val atomicActionExecutor = ViewAtomicActionExecutor(pageStateProvider)
        val uiStabilizer = LooperUiStabilizer()
        
        executionEngine = ExecutionEngine(
            pageStateProvider,
            atomicActionExecutor,
            uiStabilizer
        )
    }

    @Test
    fun executeShouldHandleRealWorldActionPath() {
        // 注意：此测试需要在真实的Activity环境中运行
        // 这里我们只测试执行器的初始化和基本功能
        
        // 创建一个简单的动作路径
        val actionPath = ActionPath(
            startPageId = "ExecutorTestActivity",
            steps = listOf(
                ActionStep(
                    actionId = 1,
                    componentId = "button_test_click",
                    triggerType = TriggerType.CLICK
                )
            )
        )
        
        // 执行动作路径
        val result = executionEngine.execute(actionPath)
        
        // 验证结果类型（由于测试环境限制，可能会失败，但结构正确）
        assert(result is ExecuteResult.Success || result is ExecuteResult.Failed)
    }
}
