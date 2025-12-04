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
import javax.inject.Inject

sealed class KeyboardUiState {
    data object Loading : KeyboardUiState()
    data class Ready(val language: String = "en", val isPasswordField: Boolean = false) : KeyboardUiState()
    data class Error(val message: String) : KeyboardUiState()
}

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<KeyboardUiState>(KeyboardUiState.Loading)
    val uiState: StateFlow<KeyboardUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            // Simulate loading settings
            _uiState.value = KeyboardUiState.Ready()
        }
    }

    fun onInputStarted(isPasswordField: Boolean) {
        val currentState = _uiState.value
        if (currentState is KeyboardUiState.Ready) {
            _uiState.value = currentState.copy(isPasswordField = isPasswordField)
        } else {
             _uiState.value = KeyboardUiState.Ready(isPasswordField = isPasswordField)
        }
    }

    fun handleKeyPress(key: Char) {
        viewModelScope.launch {
            // Logic for key press
            // e.g., clipboardRepository.copy(key.toString()) // Just an example, normally we don't copy every key
        }
    }
}
