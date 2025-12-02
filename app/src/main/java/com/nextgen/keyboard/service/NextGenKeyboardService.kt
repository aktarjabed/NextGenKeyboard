package com.nextgen.keyboard.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import com.nextgen.keyboard.BuildConfig
import com.nextgen.keyboard.repository.PreferencesRepository
import com.nextgen.keyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.nextgen.keyboard.managers.GiphyManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NextGenKeyboardService : InputMethodService() {
    @Inject lateinit var autocorrectEngine: AdvancedAutocorrectEngine
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var voiceInputManager: VoiceInputManager
    @Inject lateinit var giphyManager: GiphyManager

    private lateinit var viewModel: KeyboardViewModel
    private var serviceScope: CoroutineScope? = null
    private var composeView: ComposeView? = null

    private val _keyboardState = MutableStateFlow(KeyboardState.Main)
    private val keyboardState = _keyboardState.asStateFlow()

    private var useBasicKeyboard = false

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        try {
            initializeComponents()
        } catch (e: Exception) {
            logError("Failed to initialize keyboard components", e)
            useBasicKeyboard = true
        }
    }

    private fun initializeComponents() {
        try {
            viewModel = KeyboardViewModel(
                autocorrectEngine,
                preferencesRepository,
                giphyManager,
                serviceScope!! // Pass scope for proper lifecycle
            )

            // Initialize Giphy with proper error handling
            val giphyApiKey = getGiphyApiKey()
            if (giphyApiKey.isNotEmpty()) {
                giphyManager.initialize(giphyApiKey)
            } else {
                logWarning("Giphy API key not configured")
            }

            logInfo("Keyboard components initialized successfully")
        } catch (e: Exception) {
            logError("Error during initialization", e)
            throw e
        }
    }

    override fun onDestroy() {
        try {
            // Proper cleanup sequence
            composeView?.disposeComposition()
            composeView = null

            voiceInputManager.destroy()
            // giphyManager.cleanup() - Removed as cleanup() doesn't exist in new GiphyManager

            // Cancel all coroutines
            serviceScope?.cancel()
            serviceScope = null

            logInfo("Keyboard service destroyed cleanly")
        } catch (e: Exception) {
            logError("Error during onDestroy", e)
        } finally {
            super.onDestroy()
        }
    }

    override fun onCreateInputView(): View {
        return try {
            if (useBasicKeyboard) {
                return createFallbackView()
            }

            // Dispose previous compose view to prevent memory leaks
            composeView?.disposeComposition()

            composeView = ComposeView(this).apply {
                setContent {
                    NextGenKeyboardTheme {
                        KeyboardContent()
                    }
                }
            }
            composeView!!
        } catch (e: Exception) {
            logError("Error creating input view", e)
            createFallbackView()
        }
    }

    @Composable
    private fun KeyboardContent() {
        val currentKeyboardState by keyboardState.collectAsState()
        val suggestions by viewModel.suggestions.collectAsState()
        val voiceState by voiceInputManager.voiceState.collectAsState()
        val voiceVolume by voiceInputManager.volume.collectAsState()

        // Handle voice input results with proper error handling
        LaunchedEffect(voiceState) {
            try {
                if (voiceState is VoiceInputState.Result) {
                    val result = voiceState as VoiceInputState.Result
                    if (result.text.isNotBlank()) {
                        commitText(result.text)
                    }
                    _keyboardState.value = KeyboardState.Main
                }
            } catch (e: Exception) {
                logError("Error handling voice input result", e)
                _keyboardState.value = KeyboardState.Main
            }
        }

        when (currentKeyboardState) {
            is KeyboardState.Main -> {
                val language by viewModel.currentLanguage.collectAsState()
                MainKeyboardView(
                    language = language,
                    suggestions = suggestions.map { it.suggestion },
                    onSuggestionClick = { suggestion ->
                        handleKeyPress(suggestion)
                    },
                    onKeyClick = { key ->
                        handleKeyPress(key)
                    },
                    onVoiceInputClick = {
                        _keyboardState.value = KeyboardState.Voice
                    },
                    onGifKeyboardClick = {
                        _keyboardState.value = KeyboardState.Gif
                    },
                    onSettingsClick = {
                        openSettings()
                    }
                )
            }

            is KeyboardState.Voice -> {
                VoiceInputSheet(
                    state = voiceState,
                    volume = voiceVolume,
                    onStartListening = {
                        serviceScope?.launch {
                            try {
                                voiceInputManager.startListening(viewModel.currentLanguage.value.code)
                            } catch (e: Exception) {
                                logError("Error starting voice input", e)
                                _keyboardState.value = KeyboardState.Main
                            }
                        }
                    },
                    onStopListening = {
                        voiceInputManager.stopListening()
                    },
                    onCancel = {
                        _keyboardState.value = KeyboardState.Main
                    }
                )
            }

            is KeyboardState.Gif -> {
                GifKeyboard(
                    viewModel = viewModel,
                    onGifSelected = { media ->
                        try {
                            val url = media.images.original?.gifUrl
                            if (!url.isNullOrBlank()) {
                                commitText(url)
                            } else {
                                logWarning("Selected GIF has no URL")
                            }
                        } catch (e: Exception) {
                            logError("Error selecting GIF", e)
                        } finally {
                            _keyboardState.value = KeyboardState.Main
                        }
                    },
                    onClose = { _keyboardState.value = KeyboardState.Main }
                )
            }
        }
    }

    private fun handleKeyPress(text: String) {
        if (text.isBlank()) return

        // Handle special keys
        when (text) {
            "⌫" -> {
                handleBackspace()
                return
            }
            "↵" -> {
                handleEnter()
                return
            }
            "SPACE" -> {
                commitText(" ")
                return
            }
        }

        serviceScope?.launch {
            try {
                // Process through autocorrect if enabled
                val processedText = if (viewModel.isAutocorrectEnabled()) {
                    autocorrectEngine.processInput(text)
                } else {
                    text
                }

                commitText(processedText)

                // Learn from user input
                autocorrectEngine.learnWord(processedText)
            } catch (e: Exception) {
                logError("Error processing key press", e)
                // Fallback to original text
                commitText(text)
            }
        }
    }

    private fun handleBackspace() {
        try {
            currentInputConnection?.deleteSurroundingText(1, 0)
        } catch (e: Exception) {
            logError("Error handling backspace", e)
        }
    }

    private fun handleEnter() {
        try {
            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
        } catch (e: Exception) {
            logError("Error handling enter", e)
        }
    }

    private fun commitText(text: String) {
        try {
            currentInputConnection?.commitText(text, 1)
        } catch (e: Exception) {
            logError("Error committing text", e)
        }
    }

    private fun openSettings() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        } catch (e: Exception) {
            logError("Error opening settings", e)
        }
    }

    private fun createFallbackView(): View {
        return TextView(this).apply {
            text = "Keyboard temporarily unavailable. Please restart the app."
            setPadding(32, 32, 32, 32)
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
    }

    private fun getGiphyApiKey(): String {
        // In production, get from secure config or build config
        return BuildConfig.GIPHY_API_KEY.takeIf { it.isNotEmpty() } ?: ""
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        try {
            super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)

            if (!useBasicKeyboard) {
                val composingText = currentInputConnection?.getTextBeforeCursor(100, 0)?.toString() ?: ""
                serviceScope?.launch {
                    try {
                        viewModel.onTextUpdated(composingText)
                    } catch (e: Exception) {
                        logError("Error updating text context", e)
                    }
                }
            }
        } catch (e: Exception) {
            logError("Error in onUpdateSelection", e)
        }
    }
}