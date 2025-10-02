package com.nextgen.keyboard

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nextgen.keyboard.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltAndroidApp
class NextGenKeyboardApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workScheduler: WorkScheduler

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Firebase Crashlytics
        initializeCrashlytics()

        // Set custom exception handler
        setupCustomExceptionHandler()

        Timber.d("NextGenKeyboard App Started")

        // ✅ Schedule periodic cleanup
        try {
            workScheduler.schedulePeriodicCleanup()
            Timber.d("✅ Background cleanup scheduled")
        } catch (e: Exception) {
            Timber.e(e, "Error scheduling background cleanup")
        }
    }

    private fun initializeCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance().apply {
                // Set custom keys for debugging
                setCustomKey("app_version", BuildConfig.VERSION_NAME)
                setCustomKey("app_version_code", BuildConfig.VERSION_CODE)
                setCustomKey("build_type", BuildConfig.BUILD_TYPE)

                // Set user identifier (anonymized)
                setUserId(getAnonymousUserId())

                // Enable/disable based on build type
                setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

                log("Crashlytics initialized successfully")
            }
        } catch (e: Exception) {
            Log.e("NextGenKeyboardApp", "Failed to initialize Crashlytics", e)
        }
    }

    private fun setupCustomExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Log additional context
                val runtime = Runtime.getRuntime()
                val maxMemory = runtime.maxMemory() / (1024 * 1024)
                val freeMemory = runtime.freeMemory() / (1024 * 1024)
                val totalMemory = runtime.totalMemory() / (1024 * 1024)

                FirebaseCrashlytics.getInstance().apply {
                    setCustomKey("max_memory_mb", maxMemory)
                    setCustomKey("free_memory_mb", freeMemory)
                    setCustomKey("total_memory_mb", totalMemory)
                    setCustomKey("thread_name", thread.name)

                    log("Uncaught exception in thread: ${thread.name}")
                    recordException(throwable)
                }
            } catch (e: Exception) {
                Log.e("NextGenKeyboardApp", "Error in custom exception handler", e)
            } finally {
                // Pass to default handler
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun getAnonymousUserId(): String {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_id", null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("user_id", newId).apply()
            newId
        }
    }

    // ✅ WorkManager configuration for Hilt
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
}