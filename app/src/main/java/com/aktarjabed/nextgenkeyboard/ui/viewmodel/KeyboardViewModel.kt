package com.aktarjabed.nextgenkeyboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aktarjabed.nextgenkeyboard.data.repository.ClipboardRepository
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import com.aktarjabed.nextgenkeyboard.feature.ai.AiContextManager
import com.aktarjabed.nextgenkeyboard.feature.ai.SmartPredictionUseCase
import com.giphy.sdk.core.models.Media
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class KeyboardUiState {
    data object Loading : KeyboardUiState()
    data class Ready(
        val language: String = "en",
        val isPasswordField: Boolean = false,
        val suggestions: List<String> = emptyList()
    ) : KeyboardUiState()
    data class Error(val message: String) : KeyboardUiState()
}

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val clipboardRepository: ClipboardRepository,
    private val preferencesRepository: PreferencesRepository,
    private val smartPredictionUseCase: SmartPredictionUseCase,
    private val aiContextManager: AiContextManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<KeyboardUiState>(KeyboardUiState.Loading)
    val uiState: StateFlow<KeyboardUiState> = _uiState.asStateFlow()

    // Emoji support
    val recentEmojis: Flow<List<String>> = preferencesRepository.recentEmojis

    // GIF Support
    private val _trendingGifs = MutableStateFlow<List<Media>>(emptyList())
    val trendingGifs: StateFlow<List<Media>> = _trendingGifs.asStateFlow()

    private val _searchedGifs = MutableStateFlow<List<Media>>(emptyList())
    val searchedGifs: StateFlow<List<Media>> = _searchedGifs.asStateFlow()

    fun searchGifs(query: String) {
        // Stub
    }

    // Prediction Debounce
    private var predictionJob: Job? = null

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
        // Clear history on new input session start if needed,
        // but for now we keep it for context unless it's a password field.
        if (isPasswordField) {
            aiContextManager.clearHistory()
        }

        if (currentState is KeyboardUiState.Ready) {
            _uiState.value = currentState.copy(isPasswordField = isPasswordField, suggestions = emptyList())
        } else {
             _uiState.value = KeyboardUiState.Ready(isPasswordField = isPasswordField)
        }
    }

    fun handleKeyPress(key: Char) {
        viewModelScope.launch {
            // Logic for key press tracking if needed
        }
    }

    fun onTextChanged(textBeforeCursor: String) {
        val currentState = _uiState.value as? KeyboardUiState.Ready ?: return
        if (currentState.isPasswordField) return

        // Debounce predictions to avoid spamming API on every keystroke
        predictionJob?.cancel()
        predictionJob = viewModelScope.launch {
            delay(500) // Wait 500ms after last keystroke

            val context = aiContextManager.getContext(textBeforeCursor)
            val predictions = smartPredictionUseCase.getPredictions(context)

            if (predictions.isNotEmpty()) {
                _uiState.value = currentState.copy(suggestions = predictions)
            }
        }
    }

    fun onTextCommitted(text: String) {
        val currentState = _uiState.value as? KeyboardUiState.Ready ?: return
        if (!currentState.isPasswordField) {
            aiContextManager.addCommittedText(text)
        }
    }

    fun trackEmojiUsage(emoji: String) {
        viewModelScope.launch {
            preferencesRepository.addRecentEmoji(emoji)
        }
    }
}
