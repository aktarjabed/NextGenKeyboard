package com.aktarjabed.nextgenkeyboard.feature.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.aktarjabed.nextgenkeyboard.data.model.LanguagesPro
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class KeyboardBackup(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val settings: BackupSettings
)

@Serializable
data class BackupSettings(
    val darkMode: Boolean,
    val selectedLayout: String,
    val hapticFeedback: Boolean,
    val swipeTyping: Boolean,
    val clipboardEnabled: Boolean,
    val blockSensitiveContent: Boolean,
    val autoDeleteDays: Int,
    val maxClipboardItems: Int,
    val currentLanguage: String,
    val enabledLanguages: List<String>,
    val currentTheme: String
)

@Singleton
class SettingsBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Export settings to JSON file
     */
    suspend fun exportSettings(uri: Uri): Result<String> {
        return try {
            val backup = KeyboardBackup(
                settings = BackupSettings(
                    darkMode = preferencesRepository.isDarkMode.first(),
                    selectedLayout = preferencesRepository.selectedLayout.first(),
                    hapticFeedback = preferencesRepository.isHapticFeedbackEnabled.first(),
                    swipeTyping = preferencesRepository.isSwipeTypingEnabled.first(),
                    clipboardEnabled = preferencesRepository.isClipboardEnabled.first(),
                    blockSensitiveContent = preferencesRepository.isBlockSensitiveContent.first(),
                    autoDeleteDays = preferencesRepository.autoDeleteDays.first(),
                    maxClipboardItems = preferencesRepository.maxClipboardItems.first(),
                    currentLanguage = preferencesRepository.keyboardLanguage.first(),
                    enabledLanguages = emptyList(), // Not supported yet
                    currentTheme = preferencesRepository.themePreference.first()
                )
            )

            val jsonString = json.encodeToString(KeyboardBackup.serializer(), backup)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            }

            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val filename = "NextGenKeyboard_${dateFormat.format(Date())}.json"

            Timber.d("✅ Settings exported successfully")
            Result.success(filename)
        } catch (e: Exception) {
            Timber.e(e, "Error exporting settings")
            Result.failure(e)
        }
    }

    /**
     * Import settings from JSON file
     */
    suspend fun importSettings(uri: Uri): Result<BackupSettings> {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return Result.failure(Exception("Failed to read file"))

            val backup = json.decodeFromString(KeyboardBackup.serializer(), jsonString)

            // Apply settings
            with(backup.settings) {
                preferencesRepository.setDarkMode(darkMode)
                preferencesRepository.setSelectedLayout(selectedLayout)
                preferencesRepository.setHapticFeedback(hapticFeedback)
                preferencesRepository.setSwipeTyping(swipeTyping)
                preferencesRepository.setClipboardEnabled(clipboardEnabled)
                preferencesRepository.setBlockSensitiveContent(blockSensitiveContent)
                preferencesRepository.setAutoDeleteDays(autoDeleteDays)
                preferencesRepository.setMaxClipboardItems(maxClipboardItems)
                // Apply language and theme settings
                preferencesRepository.setKeyboardLanguage(currentLanguage)
                // preferencesRepository.setEnabledLanguages(enabledLanguages) // Not supported yet
                preferencesRepository.setThemePreference(currentTheme)
            }

            Timber.d("✅ Settings imported successfully")
            Result.success(backup.settings)
        } catch (e: Exception) {
            Timber.e(e, "Error importing settings")
            Result.failure(e)
        }
    }
}