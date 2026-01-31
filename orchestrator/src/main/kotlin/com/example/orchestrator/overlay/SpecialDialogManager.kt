package com.example.orchestrator.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.executor.core.Executor
import com.example.orchestrator.executor.ExecutorClient
import com.example.orchestrator.model.UserRequest
import com.example.orchestrator.planner.PlannerClient
import com.example.orchestrator.planner.SpecialTaskPlanner
import com.example.planner.UserGoal
import com.example.orchestrator.state.RuntimeStateProvider

class SpecialDialogManager(private val context: Context, private val overlayView: OverlayView, private val executor: Executor, private val plannerClient: PlannerClient, private val executorClient: ExecutorClient, private val runtimeStateProvider: RuntimeStateProvider) {
    
    private val mainHandler = Handler(Looper.getMainLooper())
    enum class DialogState {
        IDLE, STARTED, ASKING_DEPARTMENT, CONFIRMING, COMPLETED
    }
    
    private var currentState = DialogState.IDLE
    private var dialogStep = 0
    
    /**
     * 处理用户输入，返回是否是特殊对话输入
     */
    fun handleInput(input: String): Boolean {
        when (currentState) {
            DialogState.IDLE -> {
                if (input == "我感到肚子痛") {
                    startDialog()
                    return true
                }
            }
            DialogState.STARTED -> {
                handleStartedState(input)
                return true
            }
            DialogState.ASKING_DEPARTMENT -> {
                handleAskingDepartmentState(input)
                return true
            }
            DialogState.CONFIRMING -> {
                handleConfirmingState(input)
                return true
            }
            DialogState.COMPLETED -> {
                resetDialog()
                return false
            }
        }
        return false
    }
    
    /**
     * 启动特殊对话
     */
    private fun startDialog() {
        currentState = DialogState.STARTED
        dialogStep = 1
        
        // 添加延迟后显示对话内容
        Thread {
            try {
                Thread.sleep((1500 + Math.random() * 900).toLong()) // 1.5-2.4秒延迟
                overlayView.setInputText("")
                overlayView.setStatusText("对话: 你需要挂普通门诊、专家门诊还是急诊？")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
        
        Log.d("SpecialDialogManager", "启动特殊对话")
    }
    
    /**
     * 处理对话开始状态
     */
    private fun handleStartedState(input: String) {
        currentState = DialogState.ASKING_DEPARTMENT
        dialogStep = 2
        
        // 添加延迟后显示对话内容
        Thread {
            try {
                Thread.sleep((1500 + Math.random() * 900).toLong()) // 1.5-2.4秒延迟
                overlayView.setInputText("")
                overlayView.setStatusText("对话: 挂号时间选择今天上午还是下午？")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
        
        Log.d("SpecialDialogManager", "进入询问时间状态")
    }
    
    /**
     * 处理询问科室状态
     */
    private fun handleAskingDepartmentState(input: String) {
        currentState = DialogState.COMPLETED
        dialogStep = 3
        
        // 显示执行提示
        overlayView.setStatusText("对话: 对话完成，正在执行挂号操作...")
        
        // 执行特殊路径
        executeSpecialPath()
        
        Log.d("SpecialDialogManager", "对话完成，执行挂号操作")
    }
    
    /**
     * 处理确认状态
     */
    private fun handleConfirmingState(input: String) {
        // 这个状态现在不再使用
        resetDialog()
    }
    
    /**
     * 执行特殊路径
     */
    private fun executeSpecialPath() {
        Thread {
            try {
                Log.d("SpecialDialogManager", "开始执行特殊路径，使用SpecialTaskPlanner生成路径")
                
                // 使用SpecialTaskPlanner生成挂号路径
                val planResult = SpecialTaskPlanner.generateRegistrationPath()
                
                Log.d("SpecialDialogManager", "SpecialTaskPlanner生成的路径: ${planResult.actionPath}")
                Log.d("SpecialDialogManager", "路径步骤数量: ${planResult.actionPath.size}")
                
                if (!planResult.success) {
                    throw Exception("路径生成失败: ${planResult.reason}")
                }
                
                // 将PlanResult转换为ActionStep列表
                val actionSteps = planResult.actionPath.map {
                    // 从PlannerClientImpl的映射中获取componentId
                    // 这里使用简化的映射，根据用户提供的正确范例
                    val componentId = when (it) {
                        34 -> "btnToSecond1"       // 医院就医 (MainActivity)
                        0 -> "btnAppointment"       // 预约挂号 (SecondActivity1)
                        20 -> "btnNormalClinic"     // 普通门诊 (AppointmentActivity)
                        16 -> "btnInternalMedicine" // 内科 (DepartmentActivity)
                        9 -> "btnDoctorC"           // 医生C (DoctorActivity)
                        30 -> "btnSelectDate"       // 选择日期 (ConfirmActivity)
                        32 -> "btnTimeMorning"      // 上午 (ConfirmActivity)
                        6 -> "btnConfirmAppointment" // 确认预约 (ConfirmActivity)
                        else -> ""
                    }
                    
                    Log.d("SpecialDialogManager", "Mapping actionId=$it to componentId='$componentId'")
                    
                    com.example.executor.planner.ActionStep(
                        actionId = it,
                        componentId = componentId,
                        triggerType = com.example.executor.planner.TriggerType.CLICK
                    )
                }
                
                Log.d("SpecialDialogManager", "创建的ActionSteps: $actionSteps")
                
                // 执行每个步骤
                for ((index, step) in actionSteps.withIndex()) {
                    Log.d("SpecialDialogManager", "执行步骤 $index: $step")
                    
                    // 更新当前页面ID
                    mainHandler.post {
                        overlayManager?.updatePageId()
                    }
                    
                    // 短暂延迟，确保页面状态更新
                    Thread.sleep(500)
                    
                    val currentPage = runtimeStateProvider.getCurrentPageId()
                    Log.d("SpecialDialogManager", "当前页面: $currentPage")
                    
                    val currentPageNotNull = currentPage ?: run {
                        Log.e("SpecialDialogManager", "获取不到当前页面ID，任务失败")
                        mainHandler.post {
                            overlayView.setStatusText("对话: 执行失败，无法获取当前页面")
                            Toast.makeText(context, "执行失败: 无法获取当前页面", Toast.LENGTH_SHORT).show()
                        }
                        return@Thread
                    }
                    
                    Log.d("SpecialDialogManager", "执行操作: $step 在页面: $currentPageNotNull")
                    val executeResult = executorClient.execute(step, currentPageNotNull)
                    Log.d("SpecialDialogManager", "执行结果: $executeResult")
                    
                    if (executeResult is com.example.executor.result.ExecuteResult.Failed) {
                        Log.e("SpecialDialogManager", "执行失败: ${executeResult.reason}")
                        mainHandler.post {
                            overlayView.setStatusText("对话: 执行失败，请重试")
                            Toast.makeText(context, "执行失败: ${executeResult.reason}", Toast.LENGTH_SHORT).show()
                        }
                        return@Thread
                    }
                    
                    // 执行后短暂延迟，确保页面导航完成
                    Thread.sleep(1000)
                }
                
                // 显示执行结果
                mainHandler.post {
                    overlayView.setStatusText("对话: 特殊路径执行完成")
                    Toast.makeText(context, "特殊路径执行完成", Toast.LENGTH_SHORT).show()
                }
                
                Log.d("SpecialDialogManager", "特殊路径执行完成")
            } catch (e: Exception) {
                e.printStackTrace()
                mainHandler.post {
                    overlayView.setStatusText("对话: 执行失败，请重试")
                    Toast.makeText(context, "执行失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("SpecialDialogManager", "执行异常", e)
            } finally {
                // 重置对话状态
                mainHandler.post {
                    resetDialog()
                }
            }
        }.start()
    }
    
    // 用于更新页面ID的OverlayManager引用
    private var overlayManager: OverlayManager? = null
    
    fun setOverlayManager(overlayManager: OverlayManager) {
        this.overlayManager = overlayManager
    }
    
    /**
     * 重置对话状态
     */
    private fun resetDialog() {
        currentState = DialogState.IDLE
        dialogStep = 0
        overlayView.setStatusText("状态: 空闲")
        
        Log.d("SpecialDialogManager", "重置对话状态")
    }
    
    /**
     * 获取当前对话状态
     */
    fun getCurrentState(): DialogState {
        return currentState
    }
}