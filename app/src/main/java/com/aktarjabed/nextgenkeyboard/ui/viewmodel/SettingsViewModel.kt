package com.aktarjabed.nextgenkeyboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aktarjabed.nextgenkeyboard.data.repository.ClipboardRepository
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Settings Screen
 * Manages clipboard history, preferences, and multi-language settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _clearSuccess = MutableStateFlow(false)
    val clearSuccess: StateFlow<Boolean> = _clearSuccess.asStateFlow()

    // Settings State
    val pinnedClips = clipboardRepository.getPinnedClips()
    val recentClips = clipboardRepository.getRecentClips()

    // ================== CLIPBOARD MANAGEMENT ==================

    /**
     * Clear all clipboard history
     * Called from Settings UI when user confirms full clear
     */
    fun clearClipboardHistory() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Timber.d("Clearing all clipboard history")

                val result = clipboardRepository.clearAllClips()
                result.onSuccess {
                    Timber.d("Successfully cleared all clips")
                    _clearSuccess.value = true
                    // Auto-reset success flag after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    _clearSuccess.value = false
                }
                result.onFailure { exception ->
                    Timber.e(exception, "Failed to clear clipboard history")
                    _errorMessage.value = "Failed to clear clipboard: ${exception.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear only unpinned clips
     * Preserves pinned/favorite clips
     */
    fun clearUnpinnedClips() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Timber.d("Clearing unpinned clipboard items")

                val result = clipboardRepository.clearUnpinnedClips()
                result.onSuccess {
                    Timber.d("Successfully cleared unpinned clips")
                    _clearSuccess.value = true
                    kotlinx.coroutines.delay(2000)
                    _clearSuccess.value = false
                }
                result.onFailure { exception ->
                    Timber.e(exception, "Failed to clear unpinned clips")
                    _errorMessage.value = "Failed to clear: ${exception.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a specific clipboard item
     */
    fun deleteClipboardItem(clipId: Long) {
        viewModelScope.launch {
            try {
                Timber.d("Deleting clipboard item: $clipId")
                // Note: Actual Clip object needed; stub for now
                // In real impl, fetch clip by ID first, then delete
            } catch (e: Exception) {
                Timber.e(e, "Error deleting clipboard item")
                _errorMessage.value = "Failed to delete item: ${e.message}"
            }
        }
    }

    // ================== PREFERENCES MANAGEMENT ==================

    /**
     * Get current keyboard language preference
     */
    fun getKeyboardLanguage(): String {
        return preferencesRepository.getKeyboardLanguage()
    }

    /**
     * Set keyboard language preference
     */
    fun setKeyboardLanguage(language: String) {
        viewModelScope.launch {
            try {
                Timber.d("Setting keyboard language to: $language")
                preferencesRepository.setKeyboardLanguage(language)
            } catch (e: Exception) {
                Timber.e(e, "Error setting keyboard language")
                _errorMessage.value = "Failed to change language: ${e.message}"
            }
        }
    }

    /**
     * Get current theme preference
     */
    fun getThemePreference(): String {
        return preferencesRepository.getThemePreference()
    }

    /**
     * Set theme preference (light/dark/auto)
     */
    fun setThemePreference(theme: String) {
        viewModelScope.launch {
            try {
                Timber.d("Setting theme to: $theme")
                preferencesRepository.setThemePreference(theme)
            } catch (e: Exception) {
                Timber.e(e, "Error setting theme")
                _errorMessage.value = "Failed to change theme: ${e.message}"
            }
        }
    }

    // ================== ERROR HANDLING ==================

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
