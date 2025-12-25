package com.aktarjabed.nextgenkeyboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aktarjabed.nextgenkeyboard.data.repository.ClipboardRepository
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import com.aktarjabed.nextgenkeyboard.feature.ai.AiContextManager
import com.aktarjabed.nextgenkeyboard.feature.ai.SmartPredictionUseCase
import com.aktarjabed.nextgenkeyboard.feature.suggestions.CompositeSuggestionProvider
import com.aktarjabed.nextgenkeyboard.feature.gif.GiphyManager
import com.giphy.sdk.core.models.Media
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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
    private val compositeSuggestionProvider: CompositeSuggestionProvider,
    private val aiContextManager: AiContextManager,
    private val giphyManager: GiphyManager
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

    private var gifSearchJob: Job? = null

    /**
     * Search for GIFs with debouncing and error handling
     */
    fun searchGifs(query: String) {
        // Cancel previous search
        gifSearchJob?.cancel()

        // Clear results if query is empty
        if (query.isBlank()) {
            _searchedGifs.value = emptyList()
            return
        }

        // Validate query length
        if (query.length < 2) {
            Timber.d("GIF search query too short: ${query.length} chars")
            return
        }

        gifSearchJob = viewModelScope.launch {
            try {
                // Debounce search
                delay(300)
                
                if (!giphyManager.isReady()) {
                    Timber.w("GiphyManager not initialized")
                    _searchedGifs.value = emptyList()
                    return@launch
                }

                // Perform search with callback
                giphyManager.searchGifs(query.trim()) { results ->
                    _searchedGifs.value = results
                    Timber.d("Found ${results.size} GIFs for query: $query")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching GIFs")
                _searchedGifs.value = emptyList()
            }
        }
    }

    /**
     * Load trending GIFs
     */
    fun loadTrendingGifs() {
        viewModelScope.launch {
            try {
                if (!giphyManager.isReady()) {
                    Timber.w("GiphyManager not initialized")
                    _trendingGifs.value = emptyList()
                    return@launch
                }

                giphyManager.trendingGifs { results ->
                    _trendingGifs.value = results
                    Timber.d("Loaded ${results.size} trending GIFs")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading trending GIFs")
                _trendingGifs.value = emptyList()
            }
        }
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
        // Using a 300ms delay (faster than before) as we now have hybrid engine
        predictionJob?.cancel()
        predictionJob = viewModelScope.launch {
            delay(300)

            // Extract current word for local engine
            val currentWord = textBeforeCursor.takeLastWhile { !it.isWhitespace() }
            val context = aiContextManager.getContext(textBeforeCursor)

            val predictions = compositeSuggestionProvider.getMergedSuggestions(
                currentWord = currentWord,
                contextText = context,
                languageCode = currentState.language
            )

            // Ensure we are still in a valid state to update UI
            val freshState = _uiState.value as? KeyboardUiState.Ready
            if (freshState != null && !freshState.isPasswordField) {
                 _uiState.value = freshState.copy(suggestions = predictions)
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
            try {
                if (emoji.isNotBlank()) {
                    preferencesRepository.addRecentEmoji(emoji)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error tracking emoji usage")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gifSearchJob?.cancel()
        predictionJob?.cancel()
    }
}
