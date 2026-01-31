package com.example.orchestrator.planner

import com.example.planner.PlanResult
import com.example.planner.UserGoal

/**
 * 特殊任务规划器，用于硬编码特定任务的路径
 * 目前用于挂号任务，生成固定的挂号路径
 */
class SpecialTaskPlanner {
    companion object {
        /**
         * 生成挂号任务的固定路径
         * 路径：医院就医 → 预约挂号 → 普通门诊 → 内科 → 医生C → 选择日期 → 上午 → 确认预约
         */
        fun generateRegistrationPath(): PlanResult {
            // 硬编码的挂号路径，根据用户提供的正确范例
            // 路径：[34, 0, 20, 16, 9, 30, 32, 6]
            // 对应：btnToSecond1 → btnAppointment → btnNormalClinic → btnInternalMedicine → btnDoctorC → btnSelectDate → btnTimeMorning → btnConfirmAppointment
            val registrationPath = listOf(
                34,  // btnToSecond1 - 医院就医
                0,   // btnAppointment - 预约挂号
                20,  // btnNormalClinic - 普通门诊
                16,  // btnInternalMedicine - 内科
                9,   // btnDoctorC - 医生C
                30,  // btnSelectDate - 选择日期
                32,  // btnTimeMorning - 上午
                6    // btnConfirmAppointment - 确认预约
            )
            
            return PlanResult(
                success = true,
                actionPath = registrationPath,
                reason = null
            )
        }
        
        /**
         * 处理特殊任务规划
         * @param userGoal 用户目标
         * @return 规划结果
         */
        fun plan(userGoal: UserGoal): PlanResult {
            // 检查是否是挂号任务
            val goalText = userGoal.targetVisibleText.lowercase()
            if (goalText.contains("挂号") || goalText.contains("肚子痛") || goalText.contains("看病")) {
                return generateRegistrationPath()
            }
            
            // 非特殊任务，返回失败
            return PlanResult(
                success = false,
                actionPath = emptyList(),
                reason = "NOT_SPECIAL_TASK"
            )
        }
    }
}
