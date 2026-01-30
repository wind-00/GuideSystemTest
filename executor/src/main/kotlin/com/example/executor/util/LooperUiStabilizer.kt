package com.example.executor.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LooperUiStabilizer : UiStabilizer {

    override fun waitForIdle(timeoutMs: Long): Boolean {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Already on main thread, no need to wait
            return true
        }

        val latch = CountDownLatch(1)
        val handler = Handler(Looper.getMainLooper())
        
        handler.post {
            // This will be executed when the UI thread is idle
            latch.countDown()
        }

        return try {
            latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
    }
}