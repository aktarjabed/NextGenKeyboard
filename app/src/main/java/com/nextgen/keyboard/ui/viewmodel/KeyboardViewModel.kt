package com.nextgen.keyboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgen.keyboard.data.models.LanguagesPro
import com.nextgen.keyboard.repository.PreferencesRepository
import com.giphy.sdk.core.models.Media
import com.nextgen.keyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.nextgen.keyboard.feature.autocorrect.AdvancedSuggestion
import com.nextgen.keyboard.feature.autocorrect.WordContext
import com.nextgen.keyboard.managers.GiphyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val autocorrectEngine: AdvancedAutocorrectEngine,
    private val preferencesRepository: PreferencesRepository,
    private val giphyManager: GiphyManager
) : ViewModel() {

    private val _suggestions = MutableStateFlow<List<AdvancedSuggestion>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _currentLanguage = MutableStateFlow(LanguagesPro.getLanguageByCode("en_US"))
    val currentLanguage = _currentLanguage.asStateFlow()

    private val _trendingGifs = MutableStateFlow<List<Media>>(emptyList())
    val trendingGifs = _trendingGifs.asStateFlow()

    private val _searchedGifs = MutableStateFlow<List<Media>>(emptyList())
    val searchedGifs = _searchedGifs.asStateFlow()

    private var _isAutocorrectEnabled = true // Cache for synchronous access

    init {
        viewModelScope.launch {
            preferencesRepository.currentLanguage.collect { langCode ->
                _currentLanguage.value = LanguagesPro.getLanguageByCode(langCode)
            }
        }

        viewModelScope.launch {
            preferencesRepository.isAutocorrectEnabled.collect { enabled ->
                _isAutocorrectEnabled = enabled
            }
        }

        // TODO: Initialize GiphyManager with an API key from preferences
        fetchTrendingGifs()
    }

    // Synchronous check for the Service to avoid blocking
    fun isAutocorrectEnabled(): Boolean = _isAutocorrectEnabled

    fun onTextUpdated(text: String) {
        viewModelScope.launch {
            val words = text.split(" ").filter { it.isNotBlank() }
            if (words.isEmpty()) {
                _suggestions.value = emptyList()
                return@launch
            }

            val currentWord = words.last()
            val previousWord = if (words.size > 1) words[words.size - 2] else ""

            val context = WordContext(
                previousWord = previousWord,
                isStartOfSentence = text.isEmpty() || text.endsWith(". ") || text.endsWith("? ") || text.endsWith("! "),
                isAfterPunctuation = previousWord.endsWith(".") || previousWord.endsWith(",")
            )

            val newSuggestions = autocorrectEngine.getAdvancedSuggestions(
                word = currentWord,
                context = context,
                language = _currentLanguage.value
            )
            _suggestions.value = newSuggestions
        }
    }

    fun fetchTrendingGifs() {
        giphyManager.trendingGifs { gifs ->
            _trendingGifs.value = gifs
        }
    }

    fun searchGifs(query: String) {
        if (query.isNotBlank()) {
            giphyManager.searchGifs(query) { gifs ->
                _searchedGifs.value = gifs
            }
        } else {
            _searchedGifs.value = emptyList()
        }
    }
}