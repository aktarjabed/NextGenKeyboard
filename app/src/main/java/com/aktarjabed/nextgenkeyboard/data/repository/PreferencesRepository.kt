package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.SharedPreferencesMigration
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

// DataStore extension at top level to ensure singleton behavior
private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "nextgen_preferences",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "com.aktarjabed.nextgenkeyboard_preferences"))
    }
)

/**
 * Repository for managing user preferences using DataStore.
 * Stores: keyboard language, theme, clipboard settings, privacy options, etc.
 *
 * This class has been repaired to remove duplicate code blocks and ensure consistent state management.
 */
@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Using the extension property for access
    private val dataStore: DataStore<Preferences> = context.preferencesDataStore

    companion object {
        // Preference Keys
        private val KEYBOARD_LANGUAGE = stringPreferencesKey("keyboard_language")
        private val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val SELECTED_LAYOUT = stringPreferencesKey("selected_layout")
        private val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        private val SWIPE_TYPING_ENABLED = booleanPreferencesKey("swipe_typing_enabled")
        private val AUTOCORRECT_ENABLED = booleanPreferencesKey("autocorrect_enabled")
        private val RECENT_EMOJIS = stringPreferencesKey("recent_emojis")

        // Privacy & Clipboard Keys
        private val CLIPBOARD_ENABLED = booleanPreferencesKey("clipboard_enabled")
        private val BLOCK_SENSITIVE_CONTENT = booleanPreferencesKey("block_sensitive_content")
        private val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
        private val MAX_CLIPBOARD_ITEMS = intPreferencesKey("max_clipboard_items")
        private val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
        private val GIPHY_API_KEY = stringPreferencesKey("giphy_api_key")

        // Defaults
        private const val DEFAULT_LANGUAGE = "en_US"
        private const val DEFAULT_LAYOUT = "qwerty"
        private const val DEFAULT_THEME = "light" // Changed default to ID
        private const val DEFAULT_AUTO_DELETE_DAYS = 7 // 1 week default
        private const val DEFAULT_MAX_CLIPBOARD_ITEMS = 500 // 500 clips
    }

    // ================== GENERAL PREFERENCES ==================

    val isDarkMode: Flow<Boolean> = dataStore.data.map { it[IS_DARK_MODE] ?: true }
    val selectedLayout: Flow<String> = dataStore.data.map { it[SELECTED_LAYOUT] ?: DEFAULT_LAYOUT }
    val isHapticFeedbackEnabled: Flow<Boolean> = dataStore.data.map { it[HAPTIC_FEEDBACK_ENABLED] ?: true }
    val isSwipeTypingEnabled: Flow<Boolean> = dataStore.data.map { it[SWIPE_TYPING_ENABLED] ?: true }
    val isAutocorrectEnabled: Flow<Boolean> = dataStore.data.map { it[AUTOCORRECT_ENABLED] ?: true }

    // ================== EMOJI PREFERENCES ==================
    val recentEmojis: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[RECENT_EMOJIS]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    suspend fun addRecentEmoji(emoji: String) {
        try {
            dataStore.edit { prefs ->
                val current = prefs[RECENT_EMOJIS]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                val updated = (listOf(emoji) + current).distinct().take(50)
                prefs[RECENT_EMOJIS] = updated.joinToString(",")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating recent emojis")
        }
    }

    // ================== LANGUAGE PREFERENCES ==================

    /**
     * Get current keyboard language flow
     */
    val keyboardLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEYBOARD_LANGUAGE] ?: DEFAULT_LANGUAGE
    }

    /**
     * Blocking getter for legacy compatibility.
     * Use flow collection in new code.
     */
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

    suspend fun setThemePreference(themeId: String) {
        safeEdit(THEME_PREFERENCE, themeId, "Theme")
    }

    suspend fun setDarkMode(enabled: Boolean) {
        safeEdit(IS_DARK_MODE, enabled, "Dark Mode")
    }

    // ================== SETTINGS UPDATERS ==================

    suspend fun setSelectedLayout(layout: String) {
        safeEdit(SELECTED_LAYOUT, layout, "Layout")
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        safeEdit(HAPTIC_FEEDBACK_ENABLED, enabled, "Haptic Feedback")
    }

    suspend fun setSwipeTyping(enabled: Boolean) {
        safeEdit(SWIPE_TYPING_ENABLED, enabled, "Swipe Typing")
    }

    suspend fun setAutocorrectEnabled(enabled: Boolean) {
        safeEdit(AUTOCORRECT_ENABLED, enabled, "Autocorrect")
    }

    // ================== CLIPBOARD & PRIVACY PREFERENCES ==================

    val isClipboardEnabled: Flow<Boolean> = dataStore.data.map { it[CLIPBOARD_ENABLED] ?: true }
    val isBlockSensitiveContent: Flow<Boolean> = dataStore.data.map { it[BLOCK_SENSITIVE_CONTENT] ?: true }
    val autoDeleteDays: Flow<Int> = dataStore.data.map { it[AUTO_DELETE_DAYS] ?: DEFAULT_AUTO_DELETE_DAYS }
    val maxClipboardItems: Flow<Int> = dataStore.data.map { it[MAX_CLIPBOARD_ITEMS] ?: DEFAULT_MAX_CLIPBOARD_ITEMS }
    val isCrashReportingEnabled: Flow<Boolean> = dataStore.data.map { it[CRASH_REPORTING_ENABLED] ?: false }
    val giphyApiKey: Flow<String> = dataStore.data.map { it[GIPHY_API_KEY] ?: "" }

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

    // ================== HELPER FUNCTIONS ==================

    /**
     * Safely edits a preference with error handling and logging
     */
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
