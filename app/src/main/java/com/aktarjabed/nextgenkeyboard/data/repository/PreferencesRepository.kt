package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing user preferences using DataStore
 * Stores: keyboard language, theme, clipboard settings, etc.
 */
@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore: DataStore<Preferences> = context.preferencesDataStore

    companion object {
        private const val KEYBOARD_LANGUAGE_KEY = "keyboard_language"
        private const val THEME_PREFERENCE_KEY = "theme_preference"
        private const val CLIPBOARD_AUTO_SAVE_KEY = "clipboard_auto_save"
        private const val IS_DARK_MODE_KEY = "is_dark_mode"
        private const val SELECTED_LAYOUT_KEY = "selected_layout"
        private const val HAPTIC_FEEDBACK_ENABLED_KEY = "haptic_feedback_enabled"
        private const val SWIPE_TYPING_ENABLED_KEY = "swipe_typing_enabled"

        private val KEYBOARD_LANGUAGE = stringPreferencesKey(KEYBOARD_LANGUAGE_KEY)
        private val THEME_PREFERENCE = stringPreferencesKey(THEME_PREFERENCE_KEY)
        private val CLIPBOARD_AUTO_SAVE = stringPreferencesKey(CLIPBOARD_AUTO_SAVE_KEY)
        private val IS_DARK_MODE = booleanPreferencesKey(IS_DARK_MODE_KEY)
        private val SELECTED_LAYOUT = stringPreferencesKey(SELECTED_LAYOUT_KEY)
        private val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey(HAPTIC_FEEDBACK_ENABLED_KEY)
        private val SWIPE_TYPING_ENABLED = booleanPreferencesKey(SWIPE_TYPING_ENABLED_KEY)

        // Defaults
        private const val DEFAULT_LANGUAGE = "en_US"
        private const val DEFAULT_LAYOUT = "qwerty"
        private const val DEFAULT_THEME = "auto"
    }

    // ================== GENERAL PREFERENCES ==================

    val isDarkMode: Flow<Boolean> = dataStore.data.map { it[IS_DARK_MODE] ?: true }
    val selectedLayout: Flow<String> = dataStore.data.map { it[SELECTED_LAYOUT] ?: DEFAULT_LAYOUT }
    val isHapticFeedbackEnabled: Flow<Boolean> = dataStore.data.map { it[HAPTIC_FEEDBACK_ENABLED] ?: true }
    val isSwipeTypingEnabled: Flow<Boolean> = dataStore.data.map { it[SWIPE_TYPING_ENABLED] ?: true }

    // ================== LANGUAGE PREFERENCES ==================

    /**
     * Get current keyboard language flow
     */
    val keyboardLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEYBOARD_LANGUAGE] ?: DEFAULT_LANGUAGE
    }

    // Using runBlocking to support legacy synchronous calls until full migration to Flow
    fun getKeyboardLanguage(): String {
        return try {
            runBlocking { keyboardLanguage.first() }
        } catch (e: Exception) {
            Timber.e(e, "Error reading keyboard language")
            DEFAULT_LANGUAGE
        }
    }

    /**
     * Set keyboard language
     */
    suspend fun setKeyboardLanguage(language: String) {
        safeEdit(KEYBOARD_LANGUAGE, language, "Language")
    }

    // ================== THEME PREFERENCES ==================

    val themePreference: Flow<String> = dataStore.data.map { prefs ->
        prefs[THEME_PREFERENCE] ?: DEFAULT_THEME
    }

    fun getThemePreference(): String {
        return try {
            runBlocking { themePreference.first() }
        } catch (e: Exception) {
            Timber.e(e, "Error reading theme preference")
            DEFAULT_THEME
        }
    }

    /**
     * Set theme preference
     */
    suspend fun setThemePreference(theme: String) {
        safeEdit(THEME_PREFERENCE, theme, "Theme")
    }

    suspend fun setDarkMode(enabled: Boolean) {
        safeEdit(IS_DARK_MODE, enabled, "Dark Mode")
    }

    suspend fun setSelectedLayout(layout: String) {
        safeEdit(SELECTED_LAYOUT, layout, "Layout")
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        safeEdit(HAPTIC_FEEDBACK_ENABLED, enabled, "Haptic Feedback")
    }

    suspend fun setSwipeTyping(enabled: Boolean) {
        safeEdit(SWIPE_TYPING_ENABLED, enabled, "Swipe Typing")
    }


    // ================== CLIPBOARD PREFERENCES ==================

    // Privacy Keys
    private val CLIPBOARD_ENABLED = booleanPreferencesKey("clipboard_enabled")
    private val BLOCK_SENSITIVE_CONTENT = booleanPreferencesKey("block_sensitive_content")
    private val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
    private val MAX_CLIPBOARD_ITEMS = intPreferencesKey("max_clipboard_items")
    private val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
    private val GIPHY_API_KEY = stringPreferencesKey("giphy_api_key")

    // Privacy Flows
    val isClipboardEnabled: Flow<Boolean> = dataStore.data.map { it[CLIPBOARD_ENABLED] ?: true }
    val isBlockSensitiveContent: Flow<Boolean> = dataStore.data.map { it[BLOCK_SENSITIVE_CONTENT] ?: true }
    val autoDeleteDays: Flow<Int> = dataStore.data.map { it[AUTO_DELETE_DAYS] ?: 30 }
    val maxClipboardItems: Flow<Int> = dataStore.data.map { it[MAX_CLIPBOARD_ITEMS] ?: 50 }
    val isCrashReportingEnabled: Flow<Boolean> = dataStore.data.map { it[CRASH_REPORTING_ENABLED] ?: false }
    val giphyApiKey: Flow<String> = dataStore.data.map { it[GIPHY_API_KEY] ?: "" }

    // Privacy Setters
     suspend fun setClipboardEnabled(enabled: Boolean) {
        safeEdit(CLIPBOARD_ENABLED, enabled, "Clipboard History")
    }

    suspend fun setBlockSensitiveContent(enabled: Boolean) {
        safeEdit(BLOCK_SENSITIVE_CONTENT, enabled, "Block Sensitive Content")
    }

    suspend fun setAutoDeleteDays(days: Int) {
        safeEdit(AUTO_DELETE_DAYS, days, "Auto Delete Days")
    }

    suspend fun setMaxClipboardItems(count: Int) {
        safeEdit(MAX_CLIPBOARD_ITEMS, count, "Max Clipboard Items")
    }

    suspend fun setCrashReportingEnabled(enabled: Boolean) {
        safeEdit(CRASH_REPORTING_ENABLED, enabled, "Crash Reporting")
    }

    suspend fun setGiphyApiKey(key: String) {
        safeEdit(GIPHY_API_KEY, key, "Giphy API Key")
    }

    // Helper
    private suspend fun <T> safeEdit(key: Preferences.Key<T>, value: T, name: String) {
        try {
            dataStore.edit { prefs ->
                prefs[key] = value
            }
            Timber.d("Updated $name to: $value")
        } catch (e: Exception) {
            Timber.e(e, "Error updating $name")
        }
    }
}

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "nextgen_preferences"
)
