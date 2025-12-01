package com.nextgen.keyboard.data.repository

import com.nextgen.keyboard.data.local.ClipboardDao
import com.nextgen.keyboard.data.model.ClipboardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class ClipboardRepository @Inject constructor(
    private val clipboardDao: ClipboardDao
) {
    // Expose clipboard history as a Flow of domain items
    fun getClipboardHistory(): Flow<List<ClipboardItem>> {
        return clipboardDao.getRecentClips().map { entities ->
            entities.map { entity ->
                ClipboardItem(
                    id = entity.id.toString(),
                    text = entity.content,
                    timestamp = entity.timestamp,
                    isSensitive = entity.isEncrypted
                )
            }
        }
    }

    // Save a new clip (runs on IO thread via DAO)
    suspend fun saveClip(text: String, isSensitive: Boolean = false) {
        if (text.isBlank()) return

        try {
            val entity = ClipboardEntity(
                content = text,
                timestamp = System.currentTimeMillis(),
                isEncrypted = isSensitive,
                category = if (isSensitive) "sensitive" else "general"
            )
            clipboardDao.insert(entity)
            Timber.d("✅ Saved clipboard item to DB")
        } catch (e: Exception) {
            Timber.e(e, "Error saving clip to DB")
        }
    }

    // Clear all clips
    suspend fun clearAllClips() {
        try {
            clipboardDao.deleteAllClips()
            Timber.d("✅ Clipboard history cleared from DB")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing clipboard DB")
        }
    }

    // Manual cleanup of old items
    suspend fun performManualCleanup(): Result<Unit> {
        return try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            clipboardDao.deleteOlderThan(thirtyDaysAgo)
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