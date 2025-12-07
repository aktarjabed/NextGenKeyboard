package com.aktarjabed.nextgenkeyboard.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aktarjabed.nextgenkeyboard.data.repository.ClipboardRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class ClipboardCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val clipboardRepository: ClipboardRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("🧹 Starting scheduled clipboard cleanup")

            clipboardRepository.cleanup()

            Timber.d("✅ Scheduled cleanup completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during scheduled cleanup")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "clipboard_cleanup_worker"
    }
}