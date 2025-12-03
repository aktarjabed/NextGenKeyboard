package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.ClipboardManager
import android.content.Context
import com.aktarjabed.nextgenkeyboard.data.db.ClipboardDatabase
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
            }

            text
        } catch (e: Exception) {
            Timber.e(e, "Error accessing clipboard content")
            null
        }
    }

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
