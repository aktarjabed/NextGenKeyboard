package com.nextgen.keyboard.data.repository

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
    @ApplicationContext private val context: Context
) {
    // Placeholder implementation - replace with Room database later
    private val clipboardCache = mutableListOf<ClipboardItem>()

    fun getClipboardHistory(): Flow<List<ClipboardItem>> = flowOf(clipboardCache)

    fun saveClip(text: String, isSensitive: Boolean = false) {
        if (text.isBlank()) return

        val item = ClipboardItem(
            id = UUID.randomUUID().toString(),
            text = text,
            timestamp = System.currentTimeMillis(),
            isSensitive = isSensitive
        )

        clipboardCache.add(0, item)

        // Limit cache size
        if (clipboardCache.size > 500) {
            clipboardCache.subList(500, clipboardCache.size).clear()
        }

        Timber.d("✅ Saved clipboard item: ${text.take(50)}...")
    }

    fun clearAllClips() {
        clipboardCache.clear()
        Timber.d("✅ Clipboard history cleared")
    }

    fun performManualCleanup() {
        // Remove items older than 30 days by default
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        clipboardCache.removeAll { it.timestamp < thirtyDaysAgo }
        Timber.d("✅ Manual cleanup completed")
    }
}

data class ClipboardItem(
    val id: String,
    val text: String,
    val timestamp: Long,
    val isSensitive: Boolean
)