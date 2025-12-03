package com.aktarjabed.nextgenkeyboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aktarjabed.nextgenkeyboard.data.repository.ClipboardRepository
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.google.firebase.crashlytics.FirebaseCrashlytics

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val clipboardRepository: ClipboardRepository
) : ViewModel() {

    // Existing preferences
    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
    val selectedLayout: StateFlow<String> = preferencesRepository.selectedLayout
    val isHapticEnabled: StateFlow<Boolean> = preferencesRepository.isHapticFeedbackEnabled
    val isSwipeEnabled: StateFlow<Boolean> = preferencesRepository.isSwipeTypingEnabled

    // ✅ NEW: Privacy preferences
    val isClipboardEnabled: StateFlow<Boolean> = preferencesRepository.isClipboardEnabled
    val isBlockSensitive: StateFlow<Boolean> = preferencesRepository.isBlockSensitiveContent
    val autoDeleteDays: StateFlow<Int> = preferencesRepository.autoDeleteDays
    val maxClipboardItems: StateFlow<Int> = preferencesRepository.maxClipboardItems
    val isCrashReportingEnabled: StateFlow<Boolean> = preferencesRepository.isCrashReportingEnabled

    // ✅ NEW: Giphy API Key
    val giphyApiKey: StateFlow<String> = preferencesRepository.giphyApiKey

    // ✅ NEW: UI state for operations
    private val _cleanupInProgress = MutableStateFlow(false)
    val cleanupInProgress = _cleanupInProgress.asStateFlow()

    private val _cleanupResult = MutableStateFlow<String?>(null)
    val cleanupResult = _cleanupResult.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setDarkMode(enabled)
                Timber.d("✅ Dark mode ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Timber.e(e, "Error setting dark mode")
            }
        }
    }

    fun setLayout(layoutName: String) {
        viewModelScope.launch {
            try {
                preferencesRepository.setSelectedLayout(layoutName)
                Timber.d("✅ Layout changed to: $layoutName")
            } catch (e: Exception) {
                Timber.e(e, "Error setting layout")
            }
        }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setHapticFeedback(enabled)
                Timber.d("✅ Haptic feedback ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Timber.e(e, "Error setting haptic feedback")
            }
        }
    }

    fun setSwipeTyping(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setSwipeTyping(enabled)
                Timber.d("✅ Swipe typing ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Timber.e(e, "Error setting swipe typing")
            }
        }
    }

    fun clearClipboardHistory() {
        viewModelScope.launch {
            try {
                clipboardRepository.clearAllClips()
                Timber.d("✅ Clipboard history cleared")
            } catch (e: Exception) {
                Timber.e(e, "Error clearing clipboard")
    // ✅ NEW: Privacy setters
    fun setClipboardEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setClipboardEnabled(enabled)
                Timber.d("✅ Clipboard ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Timber.e(e, "Error setting clipboard enabled")
            }
        }
    }

    fun setBlockSensitiveContent(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setBlockSensitiveContent(enabled)
                Timber.d("✅ Block sensitive content ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Timber.e(e, "Error setting block sensitive content")
            }
        }
    }

    fun setAutoDeleteDays(days: Int) {
        viewModelScope.launch {
            try {
                preferencesRepository.setAutoDeleteDays(days)
                Timber.d("✅ Auto-delete set to $days days")
            } catch (e: Exception) {
                Timber.e(e, "Error setting auto-delete days")
            }
        }
    }

    fun setMaxClipboardItems(count: Int) {
        viewModelScope.launch {
            try {
                preferencesRepository.setMaxClipboardItems(count)
                Timber.d("✅ Max clipboard items set to $count")
            } catch (e: Exception) {
                Timber.e(e, "Error setting max clipboard items")
            }
        }
    }

    fun setCrashReportingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.setCrashReportingEnabled(enabled)
                // Apply immediately
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
                Timber.d("✅ Crash reporting ${if (enabled) "enabled" else "disabled"}")
            } catch (e: Exception) {
                Timber.e(e, "Error setting crash reporting")
            }
        }
    }

    fun clearClipboardHistory() {
        viewModelScope.launch {
            try {
                _cleanupInProgress.value = true
                clipboardRepository.clearAllClips()
                _cleanupResult.value = "Clipboard history cleared successfully"
                Timber.d("✅ Clipboard history cleared")
            } catch (e: Exception) {
                Timber.e(e, "Error clearing clipboard")
                _cleanupResult.value = "Error clearing clipboard: ${e.message}"
            } finally {
                _cleanupInProgress.value = false
            }
        }
    }

    // ✅ NEW: Manual cleanup
    fun performManualCleanup() {
        viewModelScope.launch {
            try {
                _cleanupInProgress.value = true
                clipboardRepository.performManualCleanup()
                _cleanupResult.value = "Cleanup completed successfully"
                Timber.d("✅ Manual cleanup completed")
            } catch (e: Exception) {
                Timber.e(e, "Error performing cleanup")
                _cleanupResult.value = "Error during cleanup: ${e.message}"
            } finally {
                _cleanupInProgress.value = false
            }
        }
    }

    fun clearCleanupResult() {
        _cleanupResult.value = null
    }

    fun setGiphyApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                preferencesRepository.setGiphyApiKey(apiKey)
                Timber.d("✅ Giphy API key updated")
            } catch (e: Exception) {
                Timber.e(e, "Error setting Giphy API key")
            }
        }
    }
}