package com.nextgen.keyboard.util

import timber.log.Timber

object PerformanceUtils {

    fun measureExecutionTime(operation: String, block: () -> Unit) {
        val startTime = System.currentTimeMillis()
        block()
        val executionTime = System.currentTimeMillis() - startTime
        if (executionTime > 100) { // Log if > 100ms
            Timber.w("Slow operation '$operation' took ${executionTime}ms")
        }
    }

    fun <T> withMemoryCheck(operation: String, block: () -> T): T {
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        val result = block()

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = (finalMemory - initialMemory) / 1024 / 1024 // MB

        if (memoryUsed > 10) { // Log if > 10MB
            Timber.w("Memory intensive operation '$operation' used ${memoryUsed}MB")
        }

        return result
    }
}