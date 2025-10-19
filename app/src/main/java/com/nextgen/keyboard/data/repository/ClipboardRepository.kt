package com.nextgen.keyboard.data.repository

import com.nextgen.keyboard.data.local.ClipboardDao
import com.nextgen.keyboard.data.model.ClipboardEntity
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository @Inject constructor(
    private val clipboardDao: ClipboardDao
) {
    companion object {
        private const val MAX_UNPINNED_CLIPS = 500
        private const val AUTO_DELETE_DAYS = 30
    }

    fun getPinnedClips(): Flow<List<ClipboardEntity>> = clipboardDao.getPinnedClips()

    fun getRecentClips(): Flow<List<ClipboardEntity>> = clipboardDao.getRecentClips()

    suspend fun searchClips(query: String): Result<List<ClipboardEntity>> {
        return try {
            val clips = clipboardDao.searchClips(query)
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

            // ✅ Check if content looks sensitive (don't save)
            if (isSensitiveContent(content)) {
                Timber.d("🔒 Skipped saving sensitive content")
                return Result.failure(SecurityException("Sensitive content not saved"))
            }

            val clip = ClipboardEntity(content = content.trim())
            val id = clipboardDao.insert(clip)

            // ✅ Auto-cleanup after saving
            performAutoCleanup()

            Timber.d("Saved clip: $content")
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving clip")
            Result.failure(e)
        }
    }

    suspend fun updateClip(clip: ClipboardEntity): Result<Unit> {
        return try {
            clipboardDao.update(clip)
            Timber.d("Updated clip: ${clip.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating clip")
            Result.failure(e)
        }
    }

    suspend fun deleteClip(clip: ClipboardEntity): Result<Unit> {
        return try {
            clipboardDao.delete(clip)
            Timber.d("Deleted clip: ${clip.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting clip")
            Result.failure(e)
        }
    }

    suspend fun clearAllClips(): Result<Unit> {
        return try {
            clipboardDao.deleteAllClips()
            Timber.d("Cleared all clips")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing clips")
            Result.failure(e)
        }
    }

    suspend fun clearUnpinnedClips(): Result<Unit> {
        return try {
            clipboardDao.clearUnpinnedClips()
            Timber.d("Cleared unpinned clips")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing unpinned clips")
            Result.failure(e)
        }
    }

    // ✅ NEW: Auto-cleanup old clips
    private suspend fun performAutoCleanup() {
        try {
            // 1. Limit total unpinned clips to MAX_UNPINNED_CLIPS
            val unpinnedCount = clipboardDao.getUnpinnedCount()
            if (unpinnedCount > MAX_UNPINNED_CLIPS) {
                val toDelete = unpinnedCount - MAX_UNPINNED_CLIPS
                clipboardDao.deleteOldestUnpinned(toDelete)
                Timber.d("🧹 Deleted $toDelete old clips (limit: $MAX_UNPINNED_CLIPS)")
            }

            // 2. Delete clips older than AUTO_DELETE_DAYS
            val cutoffTimestamp = System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(AUTO_DELETE_DAYS.toLong())
            clipboardDao.deleteOlderThan(cutoffTimestamp)
            Timber.d("🧹 Deleted clips older than $AUTO_DELETE_DAYS days")

        } catch (e: Exception) {
            Timber.e(e, "Error during auto-cleanup")
        }
    }

    // ✅ NEW: Manual cleanup (can be called from settings)
    suspend fun performManualCleanup(): Result<Unit> {
        return try {
            performAutoCleanup()
            Timber.d("✅ Manual cleanup completed")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error during manual cleanup")
            Result.failure(e)
        }
    }

    // ✅ NEW: Detect sensitive content
    private fun isSensitiveContent(content: String): Boolean {
        val trimmed = content.trim()

        // Check for OTPs (4-8 digit numbers)
        if (trimmed.matches(Regex("^\\d{4,8}$"))) {
            return true
        }

        // Check for credit card numbers (13-19 digits with optional spaces/dashes)
        val cardPattern = Regex("^[0-9]{13,19}$|^[0-9\\s-]{15,23}$")
        if (trimmed.replace("[\\s-]".toRegex(), "").matches(cardPattern)) {
            return true
        }

        // Check for email + password patterns
        if (trimmed.contains("password", ignoreCase = true) && trimmed.length < 50) {
            return true
        }

        // Check for common password indicators
        val passwordKeywords = listOf("pass:", "pwd:", "password:", "pin:")
        if (passwordKeywords.any { trimmed.lowercase().contains(it) }) {
            return true
        }

        return false
    }
}