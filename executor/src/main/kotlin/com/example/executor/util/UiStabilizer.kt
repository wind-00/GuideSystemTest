package com.example.executor.util

interface UiStabilizer {
    fun waitForIdle(timeoutMs: Long = 1500): Boolean
}