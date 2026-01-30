package com.example.guidesystemtest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.executor.core.ExecutionEngine
import com.example.executor.planner.ActionPath
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType
import com.example.executor.result.ExecuteResult
import com.example.executor.state.ActivityPageStateProvider
import com.example.executor.action.ViewAtomicActionExecutor
import com.example.executor.util.LooperUiStabilizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExecutorTestActivity : AppCompatActivity() {

    private lateinit var executionEngine: ExecutionEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_executor_test)

        // 初始化Executor
        initExecutor()

        // 设置按钮点击事件
        setupButtonListeners()
    }

    private fun initExecutor() {
        // 初始化页面状态提供者
        val pageStateProvider = ActivityPageStateProvider(application)

        // 初始化原子动作执行器
        val atomicActionExecutor = ViewAtomicActionExecutor(pageStateProvider)

        // 初始化UI稳定器
        val uiStabilizer = LooperUiStabilizer()

        // 初始化执行引擎
        executionEngine = ExecutionEngine(
            pageStateProvider,
            atomicActionExecutor,
            uiStabilizer
        )
    }

    private fun setupButtonListeners() {
        // 测试点击按钮
        findViewById<android.widget.Button>(R.id.button_test_click).setOnClickListener {
            testClickAction()
        }

        // 测试长按按钮
        findViewById<android.widget.Button>(R.id.button_test_long_click).setOnClickListener {
            testLongClickAction()
        }

        // 测试EditText
        findViewById<android.widget.EditText>(R.id.edit_text_test).setOnClickListener {
            testEditTextAction()
        }

        // 测试CheckBox
        findViewById<android.widget.CheckBox>(R.id.check_box_test).setOnClickListener {
            testCheckBoxAction()
        }

        // 测试ProgressBar
        findViewById<android.widget.ProgressBar>(R.id.progress_bar_test).setOnClickListener {
            testProgressBarAction()
        }

        // 测试页面导航
        findViewById<android.widget.Button>(R.id.button_navigate_to_second).setOnClickListener {
            testNavigationAction()
        }

        // 加载测试Fragment
        findViewById<android.widget.Button>(R.id.button_load_fragment).setOnClickListener {
            loadTestFragment()
        }
    }

    private fun loadTestFragment() {
        val fragment = ExecutorTestFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun testClickAction() {
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

        executeActionPath(actionPath)
    }

    private fun testLongClickAction() {
        val actionPath = ActionPath(
            startPageId = "ExecutorTestActivity",
            steps = listOf(
                ActionStep(
                    actionId = 2,
                    componentId = "button_test_long_click",
                    triggerType = TriggerType.LONG_CLICK
                )
            )
        )

        executeActionPath(actionPath)
    }

    private fun testEditTextAction() {
        val actionPath = ActionPath(
            startPageId = "ExecutorTestActivity",
            steps = listOf(
                ActionStep(
                    actionId = 3,
                    componentId = "edit_text_test",
                    triggerType = TriggerType.TOUCH
                )
            )
        )

        executeActionPath(actionPath)
    }

    private fun testCheckBoxAction() {
        val actionPath = ActionPath(
            startPageId = "ExecutorTestActivity",
            steps = listOf(
                ActionStep(
                    actionId = 4,
                    componentId = "check_box_test",
                    triggerType = TriggerType.CHECKED_CHANGE
                )
            )
        )

        executeActionPath(actionPath)
    }

    private fun testProgressBarAction() {
        val actionPath = ActionPath(
            startPageId = "ExecutorTestActivity",
            steps = listOf(
                ActionStep(
                    actionId = 5,
                    componentId = "progress_bar_test",
                    triggerType = TriggerType.PROGRESS_CHANGE
                )
            )
        )

        executeActionPath(actionPath)
    }

    private fun testNavigationAction() {
        val actionPath = ActionPath(
            startPageId = "ExecutorTestActivity",
            steps = listOf(
                ActionStep(
                    actionId = 6,
                    componentId = "button_navigate_to_second",
                    triggerType = TriggerType.CLICK
                )
            )
        )

        executeActionPath(actionPath)
    }

    private fun executeActionPath(actionPath: ActionPath) {
        CoroutineScope(Dispatchers.Main).launch {
            // 显示加载提示
            Toast.makeText(this@ExecutorTestActivity, "Executing action...", Toast.LENGTH_SHORT).show()

            // 在IO线程执行
            val result = withContext(Dispatchers.IO) {
                executionEngine.execute(actionPath)
            }

            // 处理执行结果
            handleExecutionResult(result)
        }
    }

    private fun handleExecutionResult(result: ExecuteResult) {
        val resultDetails = findViewById<android.widget.TextView>(R.id.text_view_result_details)

        when (result) {
            is ExecuteResult.Success -> {
                resultDetails.text = "Success: All steps executed successfully!"
                Toast.makeText(this, "Execution successful!", Toast.LENGTH_SHORT).show()
            }
            is ExecuteResult.Failed -> {
                val errorMessage = "Failed at step ${result.stepIndex}: ${result.reason}"
                resultDetails.text = errorMessage
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun navigateToSecondActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
    }

}
