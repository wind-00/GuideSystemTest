package com.example.executor.core

/**
 * ExecutionResult sealed class representing the result of an execution
 */
sealed class ExecutionResult {
    object Success : ExecutionResult()
    data class Failure(val reason: String) : ExecutionResult()
    object Timeout : ExecutionResult()
}
