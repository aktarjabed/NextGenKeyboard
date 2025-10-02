package com.nextgen.keyboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgen.keyboard.data.repository.ClipboardRepository
import com.nextgen.keyboard.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val clipboardRepository: ClipboardRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
    val selectedLayout: StateFlow<String> = preferencesRepository.selectedLayout
    val isHapticEnabled: StateFlow<Boolean> = preferencesRepository.isHapticFeedbackEnabled
    val isSwipeEnabled: StateFlow<Boolean> = preferencesRepository.isSwipeTypingEnabled

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
            }
        }
    }
}