package com.nextgen.keyboard.data.repository

import com.nextgen.keyboard.data.local.ClipDao
import com.nextgen.keyboard.data.model.Clip
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import android.content.Context
import androidx.room.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository @Inject constructor(
    private val clipDao: ClipDao
) {
    companion object {
        private const val MAX_UNPINNED_CLIPS = 500
        private const val AUTO_DELETE_DAYS = 30
    }

    fun getPinnedClips(): Flow<List<Clip>> = clipDao.getPinnedClips()

    fun getRecentClips(): Flow<List<Clip>> = clipDao.getRecentClips()

    suspend fun searchClips(query: String): Result<List<Clip>> {
        return try {
            val clips = clipDao.searchClips(query)
            Result.success(clips)
        } catch (e: Exception) {
            Timber.e(e, "Error searching clips")
            Result.failure(e)
        }
    }

    suspend fun saveClip(content: String): Result<Long> {
        return try {
            if (content.isBlank()) {
                return Result.failure(IllegalArgumentException("Clip content cannot be blank"))
            }

            val clip = Clip(content = content.trim())
            val id = clipDao.insertClip(clip)
    @ApplicationContext private val context: Context
) {
    // Placeholder implementation - replace with Room database later
    private val clipboardCache = mutableListOf<ClipboardItem>()

    fun getClipboardHistory(): Flow<List<ClipboardItem>> = flowOf(clipboardCache)

            val clip = Clip(content = content.trim())
            val id = clipDao.insertClip(clip)
    fun saveClip(text: String, isSensitive: Boolean = false) {
        if (text.isBlank()) return

        val item = ClipboardItem(
            id = UUID.randomUUID().toString(),
            text = text,
            timestamp = System.currentTimeMillis(),
            isSensitive = isSensitive
        )

            Timber.d("Saved clip: $content")
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving clip")
            Result.failure(e)
        }
    }

    suspend fun updateClip(clip: Clip): Result<Unit> {
        return try {
            clipDao.updateClip(clip)
            Timber.d("Updated clip: ${clip.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating clip")
            Result.failure(e)
        }
    }

    suspend fun deleteClip(clip: Clip): Result<Unit> {
        return try {
            clipDao.deleteClip(clip)
            Timber.d("Deleted clip: ${clip.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting clip")
            Result.failure(e)
        }
    }

    suspend fun clearAllClips(): Result<Unit> {
        return try {
            clipDao.deleteAllClips()
            Timber.d("Cleared all clips")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing clips")
            Result.failure(e)
        }
    }
        clipboardCache.add(0, item)

        // Limit cache size
        if (clipboardCache.size > 500) {
            clipboardCache.subList(500, clipboardCache.size).clear()
        }

    suspend fun clearUnpinnedClips(): Result<Unit> {
        return try {
            clipDao.clearUnpinnedClips()
            Timber.d("Cleared unpinned clips")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing unpinned clips")
            Result.failure(e)
        }
    }
}

    // ✅ NEW: Auto-cleanup old clips
    private suspend fun performAutoCleanup() {
        try {
            // 1. Limit total unpinned clips to MAX_UNPINNED_CLIPS
            val unpinnedCount = clipDao.getUnpinnedCount()
            if (unpinnedCount > MAX_UNPINNED_CLIPS) {
                val toDelete = unpinnedCount - MAX_UNPINNED_CLIPS
                clipDao.deleteOldestUnpinned(toDelete)
                Timber.d("🧹 Deleted $toDelete old clips (limit: $MAX_UNPINNED_CLIPS)")
            }

            // 2. Delete clips older than AUTO_DELETE_DAYS
            val cutoffTimestamp = System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(AUTO_DELETE_DAYS.toLong())
            clipDao.deleteOlderThan(cutoffTimestamp)
            Timber.d("🧹 Deleted clips older than $AUTO_DELETE_DAYS days")

        } catch (e: Exception) {
            Timber.e(e, "Error during auto-cleanup")
        }
        Timber.d("✅ Saved clipboard item: ${text.take(50)}...")
    }

    fun clearAllClips() {
        clipboardCache.clear()
        Timber.d("✅ Clipboard history cleared")
    }

    fun performManualCleanup(): Result<Unit> {
        return try {
            // Remove items older than 30 days by default
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            clipboardCache.removeAll { it.timestamp < thirtyDaysAgo }
            Timber.d("✅ Manual cleanup completed")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error performing manual cleanup")
            Result.failure(e)
        }
    }
}

data class ClipboardItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isSensitive: Boolean
)
