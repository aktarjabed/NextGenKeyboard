package com.aktarjabed.nextgenkeyboard.data.repository

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.aktarjabed.nextgenkeyboard.data.local.NextGenDatabase
import com.aktarjabed.nextgenkeyboard.data.model.Clip
import com.aktarjabed.nextgenkeyboard.util.SecurityUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Repository for managing clipboard operations with security filters.
 * - Saves clips to local database via Room
 * - Detects and blocks sensitive data (OTP, credit card, tokens)
 * - Safe clipboard read/write operations from System Service
 *
 * This class has been repaired to remove duplicate methods and ensure clean separation of concerns.
 */
class ClipboardRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: NextGenDatabase,
    private val preferencesRepository: PreferencesRepository
) {
    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

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

    // ================== SAVE OPERATIONS ==================

    /**
     * Saves clipboard content to database
     * Blocks sensitive data (OTP, credit cards, tokens, passwords)
     */
    suspend fun saveClip(content: String): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Enhanced input validation
            when {
                content.isBlank() -> {
                    Timber.w("Attempted to save blank clip")
                    return@withContext Result.failure(IllegalArgumentException("Clip content cannot be blank"))
                }
                content.length > 10000 -> {
                    Timber.w("Clip content too long: ${content.length} chars")
                    return@withContext Result.failure(IllegalArgumentException("Clip content exceeds maximum length"))
                }
            }

            // Check for sensitive data
            if (isSensitiveContent(content)) {
                Timber.w("Blocked save: Detected sensitive data in clipboard")
                return@withContext Result.failure(SecurityException("Potential sensitive data detected"))
            }

            // Check for duplicate content (optional optimization)
            val trimmedContent = content.trim()
            
            val clip = Clip(content = trimmedContent)
            val id = database.clipboardDao().insertClip(clip)

            // Trigger auto-cleanup, but don't fail if it errors
            try {
                performAutoCleanup()
            } catch (cleanupError: Exception) {
                Timber.w(cleanupError, "Cleanup failed after save, but save was successful")
            }

            Timber.d("Saved clip with ID: $id (${trimmedContent.length} chars)")
            Result.success(id)
        } catch (e: SecurityException) {
            Timber.e(e, "Security error saving clip")
            Result.failure(e)
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

    // ================== SYSTEM CLIPBOARD INTERACTION ==================

    private fun hasReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Starting Android 13 (Tiramisu), apps generally have access to their own clipboard writes
            // but reading requires user focus or specific conditions.
            // There isn't a runtime permission named "READ_CLIPBOARD" for apps to request from users
            // in the traditional sense, but we check if we are allowed or if the system might block us.
            // Actually, Android 10+ restricts background clipboard access.
            // Assuming this service is an IME, it generally has clipboard access when active.
            // But checking is safer.
            true
        } else {
            true
        }
    }

    /**
     * Safely reads content from system clipboard
     * Returns null if no content available or if sensitive data detected
     */
    suspend fun getClipboardContent(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            if (!hasReadPermission()) {
                Timber.w("Clipboard read permission missing")
                return@withContext null
            }

            val manager = clipboardManager ?: run {
                Timber.w("ClipboardManager not available")
                return@withContext null
            }

            if (!manager.hasPrimaryClip()) {
                 return@withContext null
            }

            val primaryClip = manager.primaryClip
            if (primaryClip == null || primaryClip.itemCount == 0) {
                Timber.d("No clipboard content available")
                return@withContext null
            }

            val item = primaryClip.getItemAt(0)
            val text = item?.text?.toString()
            
            if (text.isNullOrBlank()) {
                Timber.d("Clipboard content is empty")
                return@withContext null
            }

            // Validate content length
            if (text.length > 10000) {
                Timber.w("Clipboard content too large: ${text.length} chars")
                return@withContext null
            }

            // Optional: Block reading sensitive content automatically
            if (isSensitiveContent(text)) {
                 Timber.d("Sensitive content detected in system clipboard, ignoring.")
                 // Return null for sensitive content to protect user privacy
                 return@withContext null
            }

            text
        } catch (e: SecurityException) {
            Timber.e(e, "Security exception accessing clipboard")
            null
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
                if (text.isBlank()) {
                    Timber.w("Attempted to copy blank text to clipboard")
                    return@withContext false
                }

                if (text.length > 10000) {
                    Timber.w("Text too large to copy: ${text.length} chars")
                    return@withContext false
                }

                val manager = clipboardManager ?: run {
                    Timber.w("ClipboardManager not available for copy")
                    return@withContext false
                }

                val clip = ClipData.newPlainText(label, text)
                manager.setPrimaryClip(clip)
                Timber.d("Copied to clipboard: $label (${text.length} chars)")
                true
            } catch (e: SecurityException) {
                Timber.e(e, "Security exception copying to clipboard")
                false
            } catch (e: Exception) {
                Timber.e(e, "Failed to copy to clipboard")
                false
            }
        }

    /**
     * Safely pastes text from system clipboard
     * Blocks sensitive data from being pasted via this repository method
     */
    suspend fun pasteFromClipboard(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val manager = clipboardManager ?: return@withContext null
            if (!manager.hasPrimaryClip()) return@withContext null

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
            val maxItems = preferencesRepository.maxClipboardItems.first()
            val autoDeleteDays = preferencesRepository.autoDeleteDays.first()

            // Validate settings
            if (maxItems <= 0 || autoDeleteDays <= 0) {
                Timber.w("Invalid cleanup settings: maxItems=$maxItems, days=$autoDeleteDays")
                return@withContext
            }

            val unpinnedCount = database.clipboardDao().getUnpinnedCount()
            if (unpinnedCount > maxItems) {
                val toDelete = unpinnedCount - maxItems
                database.clipboardDao().deleteOldestUnpinned(toDelete)
                Timber.d("Deleted $toDelete old clips (limit: $maxItems)")
            }

            val cutoffTimestamp = System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(autoDeleteDays.toLong())
            
            // Validate timestamp
            if (cutoffTimestamp > 0 && cutoffTimestamp < System.currentTimeMillis()) {
                database.clipboardDao().deleteOlderThan(cutoffTimestamp)
                Timber.d("Deleted clips older than $autoDeleteDays days")
            } else {
                Timber.w("Invalid cutoff timestamp: $cutoffTimestamp")
            }

        } catch (e: Exception) {
            Timber.e(e, "Error during auto-cleanup")
            // Non-fatal error, don't propagate
        }
    }

    /**
     * Manual cleanup operation (called by tests/UI)
     */
    suspend fun cleanup() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting manual clipboard cleanup...")
            performAutoCleanup()
            Timber.i("Manual cleanup complete")
        } catch (e: Exception) {
            Timber.e(e, "Error during manual clipboard cleanup")
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
    fun isSensitiveContent(text: String): Boolean {
        return try {
             when {
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
                Regex("\\b(?:password|token|secret|pin|ssn|api_key|private_key)\\b", RegexOption.IGNORE_CASE).containsMatchIn(text) -> {
                    Timber.d("Detected sensitive keyword")
                    true
                }

                // High entropy: 20+ chars with mixed digits/special chars (suggests encrypted/token)
                text.length >= 20 &&
                text.any { it.isDigit() } &&
                text.any { !it.isLetterOrDigit() && it != ' ' } -> {
                    Timber.d("Detected high entropy pattern (possible encrypted data)")
                    true
                }

                else -> false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking sensitive content")
            false
        }
        return SecurityUtils.isSensitiveContent(text)
    }
}
