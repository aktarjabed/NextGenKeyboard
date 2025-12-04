package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.aktarjabed.nextgenkeyboard.data.db.ClipboardDatabase
import com.aktarjabed.nextgenkeyboard.data.model.Clip
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Repository for managing clipboard operations with security filters
 * - Saves clips to local database
 * - Detects and blocks sensitive data (OTP, credit card, tokens)
 * - Safe clipboard read/write operations
 */
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
    // ================== FLOW OPERATIONS ==================

    fun getPinnedClips(): Flow<List<Clip>> = database.clipboardDao().getPinnedClips()

    fun getRecentClips(): Flow<List<Clip>> = database.clipboardDao().getRecentClips()

    // ================== SEARCH ==================

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

    // ================== SAVE OPERATIONS ==================

    /**
     * Saves clipboard content to database
     * Blocks sensitive data (OTP, credit cards, tokens, passwords)
     */
    suspend fun saveClip(content: String): Result<Long> {
        return try {
            if (content.isBlank()) {
                return Result.failure(IllegalArgumentException("Clip content cannot be blank"))
            }

            // Sensitive data checks
            if (isSensitiveContent(content)) {
            // Check for sensitive data
            if (isSensitiveContent(content)) {
            // Sensitive data checks
            if (isSensitiveContent(content)) {
            if (isSensitiveContent(content)) {
                Timber.w("Blocked save: Detected sensitive data in clipboard")
                return Result.failure(IllegalArgumentException("Potential sensitive data detected"))
            }

            val clip = Clip(content = content.trim())
            val id = database.clipboardDao().insertClip(clip)

            // Trigger auto-cleanup, but don't fail if it errors
            // ✅ FIXED ISSUE 2: Trigger cleanup after save, but don't fail if it errors
            try {
                performAutoCleanup()
            } catch (cleanupError: Exception) {
                Timber.w(cleanupError, "Cleanup failed after save, but save was successful")
            }

            Timber.d("Saved clip with ID: $id")
            Result.success(id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving clip")
            Result.failure(e)
        }
    }

    // ================== UPDATE/DELETE OPERATIONS ==================

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
        } catch (e: Exception) {
            Timber.e(e, "Error deleting clip")
            Result.failure(e)
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
        }
    }

    suspend fun clearUnpinnedClips(): Result<Unit> {
        return try {
            database.clipboardDao().clearUnpinnedClips()
            Timber.d("Cleared unpinned clips")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error clearing unpinned clips")
            Result.failure(e)
        }
    }

    // ================== CLIPBOARD READ/WRITE ==================

    /**
     * Safely reads content from system clipboard
     * Returns null if no content available or if sensitive data detected
     */
    // Renamed from cleanup() to performAutoCleanup() to match test expectations and internal calls
    private suspend fun performAutoCleanup() = withContext(Dispatchers.IO) {
        try {
            // 1. Limit total unpinned clips to MAX_UNPINNED_CLIPS
    suspend fun getClipboardContent(): String? = withContext(Dispatchers.IO) {
        try {
            val manager = clipboardManager ?: run {
                Timber.w("ClipboardManager not available")
                return@withContext null
            }

            val primaryClip = manager.primaryClip
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

    /**
     * Safely copies text to system clipboard
     */
    suspend fun copyToClipboard(text: String, label: String = "Copied"): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val manager = clipboardManager ?: return@withContext false
                val clip = ClipData.newPlainText(label, text)
                manager.setPrimaryClip(clip)
                Timber.d("Copied to clipboard: $label (${text.length} chars)")
                true
            } catch (e: Exception) {
                Timber.e(e, "Failed to copy to clipboard")
                false
            }
        }

    /**
     * Safely pastes text from system clipboard
     * Blocks sensitive data from being pasted
     */
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

    // ================== CLEANUP OPERATIONS ==================

    /**
     * Automatic cleanup: removes old clips and enforces size limits
     */
    private suspend fun performAutoCleanup() = withContext(Dispatchers.IO) {
        try {
            val unpinnedCount = database.clipboardDao().getUnpinnedCount()
            if (unpinnedCount > MAX_UNPINNED_CLIPS) {
                val toDelete = unpinnedCount - MAX_UNPINNED_CLIPS
                database.clipboardDao().deleteOldestUnpinned(toDelete)
                Timber.d("Deleted $toDelete old clips (limit: $MAX_UNPINNED_CLIPS)")
            }

            val cutoffTimestamp = System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(AUTO_DELETE_DAYS.toLong())
            database.clipboardDao().deleteOlderThan(cutoffTimestamp)
            Timber.d("Deleted clips older than $AUTO_DELETE_DAYS days")

        } catch (e: Exception) {
            Timber.e(e, "Error during auto-cleanup")
            throw e
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
    /**
     * Manual cleanup operation (called by tests/UI)
     */
    suspend fun cleanup() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting manual clipboard cleanup...")

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
                }
            }

            Timber.i("Cleanup complete: deleted $deletedCount items")

        } catch (e: Exception) {
            Timber.e(e, "Critical error during clipboard cleanup")
        }
    }

    // ================== SECURITY: SENSITIVE DATA DETECTION ==================

    /**
     * Detects sensitive data patterns:
     * - OTP (6 digits only)
     * - Credit cards (13-19 digits)
     * - Keywords: password, token, secret, pin, ssn, etc.
     * - High entropy + length (encrypted/API keys)
     */
    private fun isSensitiveContent(text: String): Boolean {
        return when {
            // OTP: exactly 6 digits only
            text.matches(Regex("^\\d{6}$")) -> {
                Timber.d("Detected OTP pattern")
                true
            }

            // Credit card: 13-19 digits (with possible spaces)
            text.replace(" ", "").matches(Regex("^\\d{13,19}$")) -> {
                Timber.d("Detected credit card pattern")
                true
            }

            // Sensitive keywords
            text.contains("password", ignoreCase = true) -> {
                Timber.d("Detected 'password' keyword")
                true
            }
            text.contains("token", ignoreCase = true) -> {
                Timber.d("Detected 'token' keyword")
                true
            }
            text.contains("secret", ignoreCase = true) -> {
                Timber.d("Detected 'secret' keyword")
                true
            }
            text.contains("pin", ignoreCase = true) -> {
                Timber.d("Detected 'pin' keyword")
                true
            }
            text.contains("ssn", ignoreCase = true) -> {
                Timber.d("Detected 'ssn' keyword")
                true
            }
            text.contains("api_key", ignoreCase = true) -> {
                Timber.d("Detected 'api_key' keyword")
                true
            }
            text.contains("private_key", ignoreCase = true) -> {
                Timber.d("Detected 'private_key' keyword")
                true
            }

            // High entropy: 20+ chars with mixed digits/special chars (suggests encrypted/token)
            text.length >= 20 &&
            text.any { it.isDigit() } &&
            text.any { !it.isLetterOrDigit() && it != ' ' } -> {
                Timber.d("Detected high entropy pattern (possible encrypted data)")
                true
            }

            Timber.d("Pasted from clipboard: ${text.take(20)}...")
            text
        } catch (e: Exception) {
            Timber.e(e, "Error pasting from clipboard")
            null
    suspend fun cleanup() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting manual clipboard cleanup...")

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
                }
            }

            Timber.i("Cleanup complete: deleted $deletedCount items")

        } catch (e: Exception) {
            Timber.e(e, "Critical error during clipboard cleanup")
        }
    }

    private fun isSensitiveContent(text: String): Boolean {
        return when {
            text.matches(Regex("^\\d{6}$")) -> {
                Timber.d("Detected OTP pattern")
                true
            }

            text.replace(" ", "").matches(Regex("^\\d{13,19}$")) -> {
                Timber.d("Detected credit card pattern")
                true
            }

            text.contains("password", ignoreCase = true) -> {
                Timber.d("Detected 'password' keyword")
                true
            }
            text.contains("token", ignoreCase = true) -> {
                Timber.d("Detected 'token' keyword")
                true
            }
            text.contains("secret", ignoreCase = true) -> {
                Timber.d("Detected 'secret' keyword")
                true
            }
            text.contains("pin", ignoreCase = true) -> {
                Timber.d("Detected 'pin' keyword")
                true
            }
            text.contains("ssn", ignoreCase = true) -> {
                Timber.d("Detected 'ssn' keyword")
                true
            }
            text.contains("api_key", ignoreCase = true) -> {
                Timber.d("Detected 'api_key' keyword")
                true
            }
            text.contains("private_key", ignoreCase = true) -> {
                Timber.d("Detected 'private_key' keyword")
                true
            }

            text.length >= 20 &&
            text.any { it.isDigit() } &&
            text.any { !it.isLetterOrDigit() && it != ' ' } -> {
                Timber.d("Detected high entropy pattern (possible encrypted data)")
                true
            }

            else -> false
        }
    }
}

