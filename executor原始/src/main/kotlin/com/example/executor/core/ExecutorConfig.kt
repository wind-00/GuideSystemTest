package com.example.executor.core

/**
 * Configuration class for the Executor
 */
data class ExecutorConfig(
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000,
    val uiStabilizationDelayMs: Long = 500,
    val timeoutMs: Long = 10000
)
