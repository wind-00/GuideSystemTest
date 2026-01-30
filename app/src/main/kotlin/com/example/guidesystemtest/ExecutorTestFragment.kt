package com.example.guidesystemtest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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

class ExecutorTestFragment : Fragment() {

    private lateinit var executionEngine: ExecutionEngine

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_executor_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化Executor
        initExecutor()

        // 设置按钮点击事件
        setupButtonListeners()
    }

    private fun initExecutor() {
        val activity = requireActivity()
        val pageStateProvider = ActivityPageStateProvider(activity.application)
        val atomicActionExecutor = ViewAtomicActionExecutor(pageStateProvider)
        val uiStabilizer = LooperUiStabilizer()

        executionEngine = ExecutionEngine(
            pageStateProvider,
            atomicActionExecutor,
            uiStabilizer
        )
    }

    private fun setupButtonListeners() {
        val view = requireView()

        // 测试点击按钮
        view.findViewById<android.widget.Button>(R.id.button_fragment_click).setOnClickListener {
            testClickAction()
        }

        // 测试长按按钮
        view.findViewById<android.widget.Button>(R.id.button_fragment_long_click).setOnClickListener {
            testLongClickAction()
        }

        // 测试EditText
        view.findViewById<android.widget.EditText>(R.id.edit_text_fragment_test).setOnClickListener {
            testEditTextAction()
        }

        // 测试CheckBox
        view.findViewById<android.widget.CheckBox>(R.id.check_box_fragment_test).setOnClickListener {
            testCheckBoxAction()
        }

        // 测试ProgressBar
        view.findViewById<android.widget.ProgressBar>(R.id.progress_bar_fragment_test).setOnClickListener {
            testProgressBarAction()
        }

        // 测试导航
        view.findViewById<android.widget.Button>(R.id.button_fragment_navigate).setOnClickListener {
            testNavigationAction()
        }
    }

    private fun testClickAction() {
        val actionPath = ActionPath(
            startPageId = "ExecutorTestActivity", // Fragment的页面ID与Activity相同
            steps = listOf(
                ActionStep(
                    actionId = 10,
                    componentId = "button_fragment_click",
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
                    actionId = 11,
                    componentId = "button_fragment_long_click",
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
                    actionId = 12,
                    componentId = "edit_text_fragment_test",
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
                    actionId = 13,
                    componentId = "check_box_fragment_test",
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
                    actionId = 14,
                    componentId = "progress_bar_fragment_test",
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
                    actionId = 15,
                    componentId = "button_fragment_navigate",
                    triggerType = TriggerType.CLICK
                )
            )
        )

        executeActionPath(actionPath)
    }

    private fun executeActionPath(actionPath: ActionPath) {
        CoroutineScope(Dispatchers.Main).launch {
            // 显示加载提示
            Toast.makeText(requireContext(), "Executing action...", Toast.LENGTH_SHORT).show()

            // 在IO线程执行
            val result = withContext(Dispatchers.IO) {
                executionEngine.execute(actionPath)
            }

            // 处理执行结果
            handleExecutionResult(result)
        }
    }

    private fun handleExecutionResult(result: ExecuteResult) {
        val view = requireView()
        val resultDetails = view.findViewById<android.widget.TextView>(R.id.text_view_fragment_result_details)

        when (result) {
            is ExecuteResult.Success -> {
                resultDetails.text = "Success: All steps executed successfully!"
                Toast.makeText(requireContext(), "Execution successful!", Toast.LENGTH_SHORT).show()
            }
            is ExecuteResult.Failed -> {
                val errorMessage = "Failed at step ${result.stepIndex}: ${result.reason}"
                resultDetails.text = errorMessage
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
