package com.nextgen.keyboard.data.repository

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
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keyboard_preferences")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SELECTED_LAYOUT = stringPreferencesKey("selected_layout")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SWIPE_TYPING = booleanPreferencesKey("swipe_typing")

        // ✅ NEW: Privacy settings
        val CLIPBOARD_ENABLED = booleanPreferencesKey("clipboard_enabled")
        val BLOCK_SENSITIVE_CONTENT = booleanPreferencesKey("block_sensitive_content")
        val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
        val MAX_CLIPBOARD_ITEMS = intPreferencesKey("max_clipboard_items")
        val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled") // Opt-in

        // ✅ NEW: Language and Theme settings
        val CURRENT_LANGUAGE = stringPreferencesKey("current_language")
        val ENABLED_LANGUAGES = stringPreferencesKey("enabled_languages")
        val CURRENT_THEME = stringPreferencesKey("current_theme")

        // ✅ NEW: Giphy API Key
        val GIPHY_API_KEY = stringPreferencesKey("giphy_api_key")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DARK_MODE] ?: true }

    val selectedLayout: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SELECTED_LAYOUT] ?: "QWERTY" }

    val isHapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true }

    val isSwipeTypingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SWIPE_TYPING] ?: true }

    // ✅ NEW: Privacy preferences
    val isClipboardEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CLIPBOARD_ENABLED] ?: true }

    val isBlockSensitiveContent: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.BLOCK_SENSITIVE_CONTENT] ?: true }

    val autoDeleteDays: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.AUTO_DELETE_DAYS] ?: 30 }

    val maxClipboardItems: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.MAX_CLIPBOARD_ITEMS] ?: 500 }

    val isCrashReportingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CRASH_REPORTING_ENABLED] ?: false }

    // ✅ NEW: Language and Theme flows
    val currentLanguage: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_LANGUAGE] ?: "en_US" }

    val enabledLanguages: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.ENABLED_LANGUAGES] ?: "en_US")
                .split(",")
                .filter { it.isNotBlank() }
        }

    val currentTheme: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_THEME] ?: "dark_blue" }

    // ✅ NEW: Giphy API Key flow
    val giphyApiKey: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.GIPHY_API_KEY] ?: "" }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun setSelectedLayout(layoutName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LAYOUT] = layoutName
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabled
        }
    }

    suspend fun setSwipeTyping(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SWIPE_TYPING] = enabled
        }
    }

    // ✅ NEW: Privacy setters
    suspend fun setClipboardEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLIPBOARD_ENABLED] = enabled
        }
    }

    suspend fun setBlockSensitiveContent(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLOCK_SENSITIVE_CONTENT] = enabled
        }
    }

    suspend fun setAutoDeleteDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_DELETE_DAYS] = days.coerceIn(1, 365)
        }
    }

    suspend fun setMaxClipboardItems(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_CLIPBOARD_ITEMS] = count.coerceIn(50, 2000)
        }
    }

    suspend fun setCrashReportingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CRASH_REPORTING_ENABLED] = enabled
        }
    }

    // ✅ NEW: Language and Theme setters
    suspend fun setCurrentLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_LANGUAGE] = languageCode
        }
    }

    suspend fun setEnabledLanguages(languageCodes: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLED_LANGUAGES] = languageCodes.joinToString(",")
        }
    }

    suspend fun setCurrentTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_THEME] = themeName
        }
    }

    // ✅ NEW: Giphy API Key setter
    suspend fun setGiphyApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GIPHY_API_KEY] = apiKey
        }
    }
}