package com.nextgen.keyboard

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.nextgen.keyboard.worker.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
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

        Timber.d("NextGenKeyboard App Started")

        // ✅ Schedule periodic cleanup
        try {
            workScheduler.schedulePeriodicCleanup()
            Timber.d("✅ Background cleanup scheduled")
        } catch (e: Exception) {
            Timber.e(e, "Error scheduling background cleanup")
        }
    }

    // ✅ WorkManager configuration for Hilt
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
}