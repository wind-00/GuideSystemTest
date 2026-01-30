package com.example.pathplanner.resolver

import com.example.pathplanner.models.TargetSpec

/**
 * 目标解析器接口，负责将用户意图转换为结构化的目标规范
 */
interface TargetResolver {
    /**
     * 将用户的自然语言指令转换为结构化的目标规范
     * @param userIntent 用户的自然语言指令
     * @return 结构化的目标规范
     */
    suspend fun resolve(userIntent: String): TargetSpec
}