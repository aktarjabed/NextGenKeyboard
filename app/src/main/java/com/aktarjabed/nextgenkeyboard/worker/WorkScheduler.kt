package com.aktarjabed.nextgenkeyboard.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule periodic cleanup (runs daily)
     */
    fun schedulePeriodicCleanup() {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true) // Only run when battery is not low
                .build()

            val cleanupRequest = PeriodicWorkRequestBuilder<ClipboardCleanupWorker>(
                1, TimeUnit.DAYS // Run once per day
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                ClipboardCleanupWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                cleanupRequest
            )

            Timber.d("✅ Periodic cleanup scheduled")
        } catch (e: Exception) {
            Timber.e(e, "Error scheduling periodic cleanup")
        }
    }

    /**
     * Schedule one-time immediate cleanup
     */
    fun scheduleImmediateCleanup() {
        try {
            val cleanupRequest = OneTimeWorkRequestBuilder<ClipboardCleanupWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            workManager.enqueue(cleanupRequest)

            Timber.d("✅ Immediate cleanup scheduled")
        } catch (e: Exception) {
            Timber.e(e, "Error scheduling immediate cleanup")
        }
    }

    /**
     * Cancel all scheduled cleanup work
     */
    fun cancelPeriodicCleanup() {
        try {
            workManager.cancelUniqueWork(ClipboardCleanupWorker.WORK_NAME)
            Timber.d("✅ Periodic cleanup cancelled")
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling periodic cleanup")
        }
    }

    /**
     * Check if cleanup is scheduled
     */
    fun isCleanupScheduled(): Boolean {
        return try {
            val workInfos = workManager.getWorkInfosForUniqueWork(ClipboardCleanupWorker.WORK_NAME).get()
            workInfos.any { !it.state.isFinished }
        } catch (e: Exception) {
            Timber.e(e, "Error checking cleanup schedule")
            false
        }
    }
}