package com.nextgen.keyboard.service

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.View
import androidx.compose.ui.platform.ComposeView
import com.nextgen.keyboard.ui.theme.NextGenKeyboardTheme
import android.view.View
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import com.nextgen.keyboard.data.repository.PreferencesRepository
import android.view.View
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import com.nextgen.keyboard.data.repository.PreferencesRepository
import com.nextgen.keyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.nextgen.keyboard.feature.voice.VoiceInputManager
import com.nextgen.keyboard.ui.theme.NextGenKeyboardTheme
import com.nextgen.keyboard.ui.view.GifKeyboard
import com.nextgen.keyboard.ui.view.MainKeyboardView
import android.content.Intent
import android.view.View
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
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

    override fun onCreate() {
        super.onCreate()
        viewModel = KeyboardViewModel(autocorrectEngine, preferencesRepository, giphyManager)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceInputManager.destroy()
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        // Reset to main state whenever the keyboard is shown
        _keyboardState.value = KeyboardState.Main
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setContent {
                NextGenKeyboardTheme {
                    val currentKeyboardState by keyboardState.collectAsState()
                    val suggestions by viewModel.suggestions.collectAsState()
                    val voiceState by voiceInputManager.voiceState.collectAsState()
                    val voiceVolume by voiceInputManager.volume.collectAsState()

                    // Handle voice input results
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
                                onSuggestionClick = { suggestion ->
                                    commitText(suggestion)
                                },
                                onKeyClick = { key ->
                                    commitText(key)
                                },
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
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        val composingText = currentInputConnection?.getTextBeforeCursor(100, 0)?.toString() ?: ""
        viewModel.onTextUpdated(composingText)
    }
}