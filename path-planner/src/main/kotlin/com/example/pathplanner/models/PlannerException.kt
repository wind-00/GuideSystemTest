package com.example.pathplanner.models

/**
 * 规划器异常基类，包含结构化错误信息
 */
abstract class PlannerException(message: String) : Exception(message) {
    abstract val errorCode: String
    abstract val details: Map<String, Any>
}

/**
 * 找不到目标节点异常
 */
data class TargetNotFoundException(
    override val errorCode: String = "TARGET_NOT_FOUND",
    override val details: Map<String, Any>
) : PlannerException("找不到匹配的目标节点")

/**
 * 找不到路径异常
 */
data class PathNotFoundException(
    override val errorCode: String = "PATH_NOT_FOUND",
    override val details: Map<String, Any>
) : PlannerException("找不到从起始状态到目标状态的路径")

/**
 * 多条路径无优先级可选异常
 */
data class MultiplePathsException(
    override val errorCode: String = "MULTIPLE_PATHS",
    override val details: Map<String, Any>
) : PlannerException("存在多条路径，无法确定优先级")

/**
 * TargetSpec与UIMap不匹配异常
 */
data class TargetSpecMismatchException(
    override val errorCode: String = "TARGET_SPEC_MISMATCH",
    override val details: Map<String, Any>
) : PlannerException("TargetSpec与UIMap不匹配")

/**
 * 未知错误异常
 */
data class UnknownPlannerException(
    override val errorCode: String = "UNKNOWN_ERROR",
    override val details: Map<String, Any>
) : PlannerException("规划过程中发生未知错误")
