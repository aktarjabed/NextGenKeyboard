package com.nextgen.keyboard.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DARK_MODE] ?: true }

    val selectedLayout: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SELECTED_LAYOUT] ?: "QWERTY" }

    val isHapticFeedbackEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true }

    val isSwipeTypingEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SWIPE_TYPING] ?: true }

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
}