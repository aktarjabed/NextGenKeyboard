package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.ClipboardManager
import android.content.Context
import com.aktarjabed.nextgenkeyboard.data.db.ClipboardDatabase
import com.aktarjabed.nextgenkeyboard.data.model.Clip
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ClipboardRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: ClipboardDatabase
) {
    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    companion object {
        private const val MAX_UNPINNED_CLIPS = 500
        private const val AUTO_DELETE_DAYS = 30
    }

    fun getPinnedClips(): Flow<List<Clip>> = database.clipboardDao().getPinnedClips()

    fun getRecentClips(): Flow<List<Clip>> = database.clipboardDao().getRecentClips()

    suspend fun searchClips(query: String): Result<List<Clip>> {
        return try {
            val clips = database.clipboardDao().searchClips(query)
            Result.success(clips)
        } catch (e: Exception) {
            Timber.e(e, "Error searching clips")
            Result.failure(e)
        }
    }

    // ✅ FIXED ISSUE 1: Smarter sensitivity filter used by saveClip
    private fun isSensitiveContent(text: String): Boolean {
        val sensitivePatterns = listOf(
            "password", "token", "secret", "api_key", "private_key",
            "credit_card", "ssn", "pin", "cvv", "bearer", "authorization"
        )

        // ✅ Check for sensitive keywords (primary filter)
        val hasSensitiveKeyword = sensitivePatterns.any {
            text.contains(it, ignoreCase = true)
        }

        // ✅ Secondary filter: very high entropy + length suggests encrypted/token
        // Only reject if: contains 20+ chars AND mix of numbers/special chars
        val isHighEntropy = text.length >= 20 &&
                           text.any { it.isDigit() } &&
                           text.any { !it.isLetterOrDigit() && it != ' ' }

        return hasSensitiveKeyword || isHighEntropy
    }

    suspend fun saveClip(content: String): Result<Long> {
        return try {
            if (content.isBlank()) {
                return Result.failure(IllegalArgumentException("Clip content cannot be blank"))
            }

            // Sensitive data checks
    // ✅ FIXED ISSUE 1: Smarter sensitivity filter
    private fun isSensitiveContent(text: String): Boolean {
        val sensitivePatterns = listOf(
            "password", "token", "secret", "api_key", "private_key",
            "credit_card", "ssn", "pin", "cvv", "bearer", "authorization"
        )

        // ✅ Check for sensitive keywords (primary filter)
        val hasSensitiveKeyword = sensitivePatterns.any {
            text.contains(it, ignoreCase = true)
        }

        // ✅ Secondary filter: very high entropy + length suggests encrypted/token
        // Only reject if: contains 20+ chars AND mix of numbers/special chars
        val isHighEntropy = text.length >= 20 &&
                           text.any { it.isDigit() } &&
                           text.any { !it.isLetterOrDigit() && it != ' ' }

        return hasSensitiveKeyword || isHighEntropy
    }

    // ✅ FIXED ISSUE 3: Safe null handling
    suspend fun getClipboardContent(): String? = withContext(Dispatchers.IO) {
        try {
            val clipboardManager = clipboardManager ?: run {
                Timber.w("ClipboardManager not available")
                return@withContext null
            }

            val primaryClip = clipboardManager.primaryClip
            if (primaryClip == null || primaryClip.itemCount == 0) {
                Timber.d("No clipboard content available")
                return@withContext null
            }

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) {
                Timber.d("Clipboard content is empty")
                return@withContext null
            // Sensitive data checks based on updated requirements
            if (isSensitiveContent(content)) {
                return Result.failure(IllegalArgumentException("Potential sensitive data detected"))
            }

            val clip = Clip(content = content.trim())
            val id = database.clipboardDao().insertClip(clip)

            // ✅ FIXED ISSUE 2: Trigger cleanup after save, but don't fail if it errors
            // Trigger cleanup after save, but don't fail if it errors
            try {
                performAutoCleanup()
            } catch (cleanupError: Exception) {
                Timber.w(cleanupError, "Cleanup failed after save, but save was successful")
            }

            text
        } catch (e: Exception) {
            Timber.e(e, "Error accessing clipboard content")
            null
        }
    }

    suspend fun updateClip(clip: Clip): Result<Unit> {
        return try {
            database.clipboardDao().updateClip(clip)
            Timber.d("Updated clip: ${clip.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating clip")
            Result.failure(e)
        }
    }

    suspend fun deleteClip(clip: Clip): Result<Unit> {
        return try {
            database.clipboardDao().deleteClip(clip)
            Timber.d("Deleted clip: ${clip.id}")
            Result.success(Unit)
    // ✅ FIXED ISSUE 2: Proper error handling + logging
    suspend fun cleanup() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting clipboard cleanup...")

            val allItems = database.clipboardDao().getAllClipboard()
            if (allItems.isEmpty()) {
                Timber.d("No items to cleanup")
                return@withContext
            }

            var deletedCount = 0
            allItems.forEach { item ->
                try {
                    database.clipboardDao().delete(item)
                    deletedCount++
                } catch (e: Exception) {
                    Timber.e(e, "Failed to delete clipboard item: ${item.id}")
                    // Continue with next item instead of crashing
                }
            }

            Timber.i("Cleanup complete: deleted $deletedCount items")

        } catch (e: Exception) {
            Timber.e(e, "Critical error during clipboard cleanup")
            // Don't rethrow - allow app to continue
        }
    }

    suspend fun clearAllClips(): Result<Unit> {
        return try {
            database.clipboardDao().deleteAllClips()
            Timber.d("Cleared all clips")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing clips")
            Result.failure(e)
    // ✅ NEW: Safe copy to clipboard
    suspend fun copyToClipboard(text: String, label: String = "Copied"): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val manager = clipboardManager ?: return@withContext false
                val clip = android.content.ClipData.newPlainText(label, text)
                manager.setPrimaryClip(clip)
                Timber.d("Copied to clipboard: $label")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to copy to clipboard")
                false
            }
        }

    suspend fun clearUnpinnedClips(): Result<Unit> {
        return try {
            database.clipboardDao().clearUnpinnedClips()
            Timber.d("Cleared unpinned clips")
            Result.success(Unit)
    // ✅ NEW: Safe paste from clipboard
    suspend fun pasteFromClipboard(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val manager = clipboardManager ?: return@withContext null
            val primaryClip = manager.primaryClip ?: return@withContext null

            if (primaryClip.itemCount == 0) return@withContext null

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) return@withContext null

            if (isSensitiveContent(text)) {
                Timber.w("Detected sensitive content in clipboard - blocking paste")
                return@withContext null
            }

            Timber.d("Pasted from clipboard: ${text.take(20)}...")
            text
        } catch (e: Exception) {
            Timber.e(e, "Error pasting from clipboard")
            null
        }
    }

    // Renamed from cleanup() to performAutoCleanup() to match test expectations and internal calls
    private suspend fun performAutoCleanup() = withContext(Dispatchers.IO) {
        try {
            // 1. Limit total unpinned clips to MAX_UNPINNED_CLIPS
            val unpinnedCount = database.clipboardDao().getUnpinnedCount()
            if (unpinnedCount > MAX_UNPINNED_CLIPS) {
                val toDelete = unpinnedCount - MAX_UNPINNED_CLIPS
                database.clipboardDao().deleteOldestUnpinned(toDelete)
                Timber.d("Deleted $toDelete old clips (limit: $MAX_UNPINNED_CLIPS)")
            }

            // 2. Delete clips older than AUTO_DELETE_DAYS
            val cutoffTimestamp = System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(AUTO_DELETE_DAYS.toLong())
            database.clipboardDao().deleteOlderThan(cutoffTimestamp)
            Timber.d("Deleted clips older than $AUTO_DELETE_DAYS days")
    private fun isSensitiveContent(content: String): Boolean {
        return when {
            // OTP detection: exactly 6 digits, only
            content.matches(Regex("^\\d{6}$")) -> true

            // Credit card: 13-19 digits with possible spaces
            content.replace(" ", "").matches(Regex("^\\d{13,19}$")) -> true

            // Keyword detection
            content.contains("password", ignoreCase = true) -> true
            content.contains("token", ignoreCase = true) -> true
            content.contains("secret", ignoreCase = true) -> true
            content.contains("pin", ignoreCase = true) -> true
            content.contains("ssn", ignoreCase = true) -> true

            else -> false
        }
    }

    // ✅ FIXED ISSUE 3: Safe null handling
    suspend fun getClipboardContent(): String? = withContext(Dispatchers.IO) {
        try {
            val clipboardManager = clipboardManager ?: run {
                Timber.w("ClipboardManager not available")
                return@withContext null
            }

            val primaryClip = clipboardManager.primaryClip
            if (primaryClip == null || primaryClip.itemCount == 0) {
                Timber.d("No clipboard content available")
                return@withContext null
            }

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) {
                Timber.d("Clipboard content is empty")
                return@withContext null
            }

            text
        } catch (e: Exception) {
            Timber.e(e, "Error accessing clipboard content")
            null
        }
    }

    // ✅ NEW: Safe copy to clipboard
    suspend fun copyToClipboard(text: String, label: String = "Copied"): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val manager = clipboardManager ?: return@withContext false
                val clip = android.content.ClipData.newPlainText(label, text)
                manager.setPrimaryClip(clip)
                Timber.d("Copied to clipboard: $label")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to copy to clipboard")
                false
            }
        }

    // ✅ NEW: Safe paste from clipboard
    suspend fun pasteFromClipboard(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val manager = clipboardManager ?: return@withContext null
            val primaryClip = manager.primaryClip ?: return@withContext null

            if (primaryClip.itemCount == 0) return@withContext null

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) return@withContext null

            if (isSensitiveContent(text)) {
                Timber.w("Detected sensitive content in clipboard - blocking paste")
                return@withContext null
            }

            Timber.d("Pasted from clipboard: ${text.take(20)}...")
            text
        } catch (e: Exception) {
            Timber.e(e, "Error pasting from clipboard")
            null
        }
    }

    // ✅ FIXED ISSUE 3: Safe null handling
    suspend fun getClipboardContent(): String? = withContext(Dispatchers.IO) {
        try {
            val clipboardManager = clipboardManager ?: run {
                Timber.w("ClipboardManager not available")
                return@withContext null
            }

            val primaryClip = clipboardManager.primaryClip
            if (primaryClip == null || primaryClip.itemCount == 0) {
                Timber.d("No clipboard content available")
                return@withContext null
            }

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) {
                Timber.d("Clipboard content is empty")
                return@withContext null
            }

            text
        } catch (e: Exception) {
            Timber.e(e, "Error accessing clipboard content")
            null
        }
    }

    // ✅ NEW: Safe copy to clipboard
    suspend fun copyToClipboard(text: String, label: String = "Copied"): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val manager = clipboardManager ?: return@withContext false
                val clip = android.content.ClipData.newPlainText(label, text)
                manager.setPrimaryClip(clip)
                Timber.d("Copied to clipboard: $label")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to copy to clipboard")
                false
            }
        }

    // ✅ NEW: Safe paste from clipboard
    suspend fun pasteFromClipboard(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val manager = clipboardManager ?: return@withContext null
            val primaryClip = manager.primaryClip ?: return@withContext null

            if (primaryClip.itemCount == 0) return@withContext null

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) return@withContext null

            if (isSensitiveContent(text)) {
                Timber.w("Detected sensitive content in clipboard - blocking paste")
                return@withContext null
            }

            Timber.d("Pasted from clipboard: ${text.take(20)}...")
            text
        } catch (e: Exception) {
            Timber.e(e, "Error pasting from clipboard")
            null
        }
    }

    // ✅ FIXED ISSUE 3: Safe null handling
    suspend fun getClipboardContent(): String? = withContext(Dispatchers.IO) {
        try {
            val clipboardManager = clipboardManager ?: run {
                Timber.w("ClipboardManager not available")
                return@withContext null
            }

            val primaryClip = clipboardManager.primaryClip
            if (primaryClip == null || primaryClip.itemCount == 0) {
                Timber.d("No clipboard content available")
                return@withContext null
            }

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) {
                Timber.d("Clipboard content is empty")
                return@withContext null
            }

            text
        } catch (e: Exception) {
            Timber.e(e, "Error accessing clipboard content")
            null
        }
    }

    // ✅ NEW: Safe copy to clipboard
    suspend fun copyToClipboard(text: String, label: String = "Copied"): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val manager = clipboardManager ?: return@withContext false
                val clip = android.content.ClipData.newPlainText(label, text)
                manager.setPrimaryClip(clip)
                Timber.d("Copied to clipboard: $label")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to copy to clipboard")
                false
            }
        }

    // ✅ NEW: Safe paste from clipboard
    suspend fun pasteFromClipboard(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val manager = clipboardManager ?: return@withContext null
            val primaryClip = manager.primaryClip ?: return@withContext null

            if (primaryClip.itemCount == 0) return@withContext null

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) return@withContext null

            if (isSensitiveContent(text)) {
                Timber.w("Detected sensitive content in clipboard - blocking paste")
                return@withContext null
            }

            Timber.d("Pasted from clipboard: ${text.take(20)}...")
            text
        } catch (e: Exception) {
            Timber.e(e, "Error pasting from clipboard")
            null
        }
    }

    // ✅ FIXED ISSUE 3: Safe null handling
    suspend fun getClipboardContent(): String? = withContext(Dispatchers.IO) {
        try {
            val clipboardManager = clipboardManager ?: run {
                Timber.w("ClipboardManager not available")
                return@withContext null
            }

            val primaryClip = clipboardManager.primaryClip
            if (primaryClip == null || primaryClip.itemCount == 0) {
                Timber.d("No clipboard content available")
                return@withContext null
            }

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) {
                Timber.d("Clipboard content is empty")
                return@withContext null
            }

            text
        } catch (e: Exception) {
            Timber.e(e, "Error accessing clipboard content")
            null
        }
    }

    // ✅ NEW: Safe copy to clipboard
    suspend fun copyToClipboard(text: String, label: String = "Copied"): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val manager = clipboardManager ?: return@withContext false
                val clip = android.content.ClipData.newPlainText(label, text)
                manager.setPrimaryClip(clip)
                Timber.d("Copied to clipboard: $label")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to copy to clipboard")
                false
            }
        }

    // ✅ NEW: Safe paste from clipboard
    suspend fun pasteFromClipboard(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val manager = clipboardManager ?: return@withContext null
            val primaryClip = manager.primaryClip ?: return@withContext null

            if (primaryClip.itemCount == 0) return@withContext null

            val text = primaryClip.getItemAt(0)?.text?.toString()
            if (text.isNullOrBlank()) return@withContext null

            if (isSensitiveContent(text)) {
                Timber.w("Detected sensitive content in clipboard - blocking paste")
                return@withContext null
            }

            Timber.d("Pasted from clipboard: ${text.take(20)}...")
            text
        } catch (e: Exception) {
            Timber.e(e, "Error pasting from clipboard")
            null
        }
    }
}
