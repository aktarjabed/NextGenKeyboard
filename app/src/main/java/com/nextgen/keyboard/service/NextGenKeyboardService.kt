package com.nextgen.keyboard.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.InflateException
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import com.nextgen.keyboard.BuildConfig
import com.nextgen.keyboard.data.repository.PreferencesRepository
import com.nextgen.keyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.nextgen.keyboard.feature.voice.VoiceInputManager
import com.nextgen.keyboard.feature.voice.VoiceInputState
import com.nextgen.keyboard.ui.screens.MainActivity
import com.nextgen.keyboard.ui.theme.NextGenKeyboardTheme
import com.nextgen.keyboard.ui.view.GifKeyboard
import com.nextgen.keyboard.ui.view.MainKeyboardView
import com.nextgen.keyboard.ui.view.VoiceInputSheet
import com.nextgen.keyboard.ui.viewmodel.KeyboardViewModel
import com.nextgen.keyboard.util.logError
import com.nextgen.keyboard.util.logInfo
import com.nextgen.keyboard.util.logWarning
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class NextGenKeyboardService : InputMethodService() {

    @Inject
    lateinit var autocorrectEngine: AdvancedAutocorrectEngine
    @Inject
    lateinit var preferencesRepository: PreferencesRepository
    @Inject
    lateinit var voiceInputManager: VoiceInputManager
    @Inject
    lateinit var giphyManager: GiphyManager

    private lateinit var viewModel: KeyboardViewModel

    private val _keyboardState = MutableStateFlow<KeyboardState>(KeyboardState.Main)
    private val keyboardState = _keyboardState.asStateFlow()

    private var useBasicKeyboard = false

    override fun onCreate() {
        super.onCreate()
        try {
            initializeComponents()
        } catch (e: Exception) {
            logError("Failed to initialize keyboard components", e)
            useBasicKeyboard = true
        }
    }

    private fun initializeComponents() {
        try {
            viewModel = KeyboardViewModel(autocorrectEngine, preferencesRepository, giphyManager)
            if (BuildConfig.GIPHY_API_KEY != "YOUR_KEY_HERE") {
                giphyManager.initialize(BuildConfig.GIPHY_API_KEY)
            }
            logInfo("Keyboard components initialized successfully.")
        } catch (oom: OutOfMemoryError) {
            logError("Out of memory during initialization", oom)
            throw oom
        } catch (e: Exception) {
            logError("Unexpected error during initialization", e)
            throw e
        }
    }

    override fun onDestroy() {
        try {
            voiceInputManager.destroy()
        } catch (e: Exception) {
            logError("Error during onDestroy", e)
        } finally {
            super.onDestroy()
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        _keyboardState.value = KeyboardState.Main
    }

    override fun onCreateInputView(): View {
        return try {
            if (useBasicKeyboard) {
                return createFallbackView()
            }
            ComposeView(this).apply {
                setContent {
                    NextGenKeyboardTheme {
                        val currentKeyboardState by keyboardState.collectAsState()
                        val suggestions by viewModel.suggestions.collectAsState()
                        val voiceState by voiceInputManager.voiceState.collectAsState()
                        val voiceVolume by voiceInputManager.volume.collectAsState()

                        LaunchedEffect(voiceState) {
                            if (voiceState is VoiceInputState.Result) {
                                commitText((voiceState as VoiceInputState.Result).text)
                                _keyboardState.value = KeyboardState.Main
                            }
                        }

                        when (currentKeyboardState) {
                            is KeyboardState.Main -> {
                                val language by viewModel.currentLanguage.collectAsState()
                                MainKeyboardView(
                                    language = language,
                                    suggestions = suggestions.map { it.suggestion },
                                    onSuggestionClick = { suggestion -> handleKeyPress(suggestion) },
                                    onKeyClick = { key -> handleKeyPress(key) },
                                    onVoiceInputClick = { _keyboardState.value = KeyboardState.Voice },
                                    onGifKeyboardClick = { _keyboardState.value = KeyboardState.Gif },
                                    onSettingsClick = {
                                        val intent = Intent(this@NextGenKeyboardService, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        startActivity(intent)
                                    }
                                )
                            }
                            is KeyboardState.Voice -> {
                                VoiceInputSheet(
                                    state = voiceState,
                                    volume = voiceVolume,
                                    onStartListening = { voiceInputManager.startListening(viewModel.currentLanguage.value.code) },
                                    onStopListening = { voiceInputManager.stopListening() },
                                    onCancel = { _keyboardState.value = KeyboardState.Main }
                                )
                            }
                            is KeyboardState.Gif -> {
                                GifKeyboard(
                                    viewModel = viewModel,
                                    onGifSelected = { media ->
                                        commitText(media.images.original?.gifUrl ?: "")
                                        _keyboardState.value = KeyboardState.Main
                                    },
                                    onClose = { _keyboardState.value = KeyboardState.Main }
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: InflateException) {
            logError("Failed to inflate keyboard layout", e)
            createFallbackView()
        } catch (e: Exception) {
            logError("Unexpected error creating input view", e)
            createFallbackView()
        }
    }

    private fun handleKeyPress(text: String) {
        try {
            if (text.isBlank()) {
                logWarning("Attempted to commit empty text.")
                return
            }
            commitText(text)
        } catch (e: Exception) {
            logError("Error handling key press for text: $text", e)
        }
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun createFallbackView(): View {
        return TextView(this).apply {
            this.text = "Keyboard failed to load. Please try again."
            setPadding(32, 32, 32, 32)
        }
    }

    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        try {
            super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
            val composingText = currentInputConnection?.getTextBeforeCursor(100, 0)?.toString() ?: ""
            if (!useBasicKeyboard) {
                viewModel.onTextUpdated(composingText)
            }
        } catch (e: Exception) {
            logError("Error in onUpdateSelection", e)
        }
    }
}