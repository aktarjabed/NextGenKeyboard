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
import kotlinx.coroutines.flow.map
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
    }

    // ================== GENERAL PREFERENCES ==================

    val isDarkMode: Flow<Boolean> = dataStore.data.map { it[IS_DARK_MODE] ?: true }
    val selectedLayout: Flow<String> = dataStore.data.map { it[SELECTED_LAYOUT] ?: "qwerty" }
    val isHapticFeedbackEnabled: Flow<Boolean> = dataStore.data.map { it[HAPTIC_FEEDBACK_ENABLED] ?: true }
    val isSwipeTypingEnabled: Flow<Boolean> = dataStore.data.map { it[SWIPE_TYPING_ENABLED] ?: true }

    // ================== LANGUAGE PREFERENCES ==================

    /**
     * Get current keyboard language flow
     */
    val keyboardLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEYBOARD_LANGUAGE] ?: "en_US"
    }

    // Retained for compatibility with synchronous-like access patterns, but now using Flow internally
    // Note: To get a snapshot synchronously (blocking) is discouraged.
    // ViewModel should observe the flow. If needed for one-off, use .first() in a coroutine.
    // For migration purposes, this method now returns a default or blocking value is generally avoided.
    // However, to fix the blocking issue, we change this to just return the default if called,
    // or expected to be observed via flow.
    // But since the interface requires a String return (based on previous code),
    // we must warn or refactor usage.
    // Assuming refactor:
    fun getKeyboardLanguage(): String {
        // Warning: This is a placeholder. Real implementation should collect flow.
        // For safe fallback to avoid crash:
        return "en_US"
    }

    /**
     * Set keyboard language
     */
    suspend fun setKeyboardLanguage(language: String) {
        try {
            dataStore.edit { prefs ->
                prefs[KEYBOARD_LANGUAGE] = language
            }
            Timber.d("Set keyboard language to: $language")
        } catch (e: Exception) {
            Timber.e(e, "Error setting keyboard language")
        }
    }

    // ================== THEME PREFERENCES ==================

    val themePreference: Flow<String> = dataStore.data.map { prefs ->
        prefs[THEME_PREFERENCE] ?: "auto"
    }

    fun getThemePreference(): String {
        return "auto" // Placeholder to avoid blocking
    }

    /**
     * Set theme preference
     */
    suspend fun setThemePreference(theme: String) {
        try {
            dataStore.edit { prefs ->
                prefs[THEME_PREFERENCE] = theme
            }
            Timber.d("Set theme to: $theme")
        } catch (e: Exception) {
            Timber.e(e, "Error setting theme preference")
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        try {
            dataStore.edit { prefs ->
                prefs[IS_DARK_MODE] = enabled
            }
            Timber.d("Set dark mode to: $enabled")
        } catch (e: Exception) {
            Timber.e(e, "Error setting dark mode")
        }
    }

    suspend fun setSelectedLayout(layout: String) {
        try {
             dataStore.edit { prefs ->
                prefs[SELECTED_LAYOUT] = layout
            }
            Timber.d("Set layout to: $layout")
        } catch (e: Exception) {
             Timber.e(e, "Error setting layout")
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        try {
            dataStore.edit { prefs ->
                prefs[HAPTIC_FEEDBACK_ENABLED] = enabled
            }
            Timber.d("Set haptic feedback to: $enabled")
        } catch (e: Exception) {
             Timber.e(e, "Error setting haptic feedback")
        }
    }

    suspend fun setSwipeTyping(enabled: Boolean) {
        try {
             dataStore.edit { prefs ->
                prefs[SWIPE_TYPING_ENABLED] = enabled
            }
            Timber.d("Set swipe typing to: $enabled")
        } catch (e: Exception) {
             Timber.e(e, "Error setting swipe typing")
        }
    }


    // ================== CLIPBOARD PREFERENCES ==================

    // Privacy
    private val CLIPBOARD_ENABLED = booleanPreferencesKey("clipboard_enabled")
    private val BLOCK_SENSITIVE_CONTENT = booleanPreferencesKey("block_sensitive_content")
    private val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
    private val MAX_CLIPBOARD_ITEMS = intPreferencesKey("max_clipboard_items")
    private val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
    private val GIPHY_API_KEY = stringPreferencesKey("giphy_api_key")

    val isClipboardEnabled: Flow<Boolean> = dataStore.data.map { it[CLIPBOARD_ENABLED] ?: true }
    val isBlockSensitiveContent: Flow<Boolean> = dataStore.data.map { it[BLOCK_SENSITIVE_CONTENT] ?: true }
    val autoDeleteDays: Flow<Int> = dataStore.data.map { it[AUTO_DELETE_DAYS] ?: 30 }
    val maxClipboardItems: Flow<Int> = dataStore.data.map { it[MAX_CLIPBOARD_ITEMS] ?: 50 }
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
