package com.aktarjabed.nextgenkeyboard.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import com.aktarjabed.nextgenkeyboard.BuildConfig
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.aktarjabed.nextgenkeyboard.feature.gif.GiphyManager
import com.aktarjabed.nextgenkeyboard.feature.voice.VoiceInputManager
import com.aktarjabed.nextgenkeyboard.ui.screens.MainActivity
import com.aktarjabed.nextgenkeyboard.ui.theme.NextGenKeyboardTheme
import com.aktarjabed.nextgenkeyboard.ui.view.MainKeyboardView
import com.aktarjabed.nextgenkeyboard.ui.viewmodel.KeyboardViewModel
import com.aktarjabed.nextgenkeyboard.util.logError
import com.aktarjabed.nextgenkeyboard.util.logInfo
import com.aktarjabed.nextgenkeyboard.util.logWarning
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NextGenKeyboardService : InputMethodService() {

    @Inject
    lateinit var autocorrectEngine: AdvancedAutocorrectEngine
    @Inject
    lateinit var voiceInputManager: VoiceInputManager
    @Inject
    lateinit var giphyManager: GiphyManager
    @Inject
    lateinit var viewModel: KeyboardViewModel

    private var serviceScope: CoroutineScope? = null
    private var composeView: ComposeView? = null
    private var useBasicKeyboard = false
    private var isPasswordMode = false

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
            val giphyApiKey = BuildConfig.GIPHY_API_KEY
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
            composeView?.disposeComposition()
            composeView = null

            if (::voiceInputManager.isInitialized) {
                voiceInputManager.destroy()
            }

            serviceScope?.cancel()
            serviceScope = null

            logInfo("Keyboard service destroyed cleanly")
        } catch (e: Exception) {
            logError("Error during onDestroy", e)
        } finally {
            super.onDestroy()
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)

        try {
            val inputType = attribute?.inputType ?: 0
            isPasswordMode = detectPasswordField(inputType, attribute)
            applySecurityMode(isPasswordMode)

            viewModel.onInputStarted(isPasswordMode)

            if (isPasswordMode) {
                Timber.d("🔒 PASSWORD FIELD DETECTED")
            } else {
                Timber.d("📝 Regular input field in ${attribute?.packageName}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onStartInput")
        }
    }

    override fun onCreateInputView(): View {
        return try {
            if (useBasicKeyboard) {
                return createFallbackView()
            }

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
        val uiState by viewModel.uiState.collectAsState()

        // Basic implementation based on state
        // In a real app, we would switch on uiState (Loading, Ready, Error)
        // Here we assume Ready for simplicity and use MainKeyboardView

        MainKeyboardView(
            language = "en", // Simplified: should come from uiState
            suggestions = emptyList(), // Simplified: should come from VM
            onSuggestionClick = { handleKeyPress(it) },
            onKeyClick = { handleKeyPress(it) },
            onVoiceInputClick = { /* Handle voice */ },
            onGifKeyboardClick = { /* Handle GIF */ },
            onSettingsClick = { openSettings() }
        )
    }

    private fun handleKeyPress(text: String) {
        if (text.isBlank()) return

        when (text) {
            "⌫" -> handleBackspace()
            "↵" -> handleEnter()
            "SPACE" -> commitText(" ")
            else -> {
                commitText(text)
                viewModel.handleKeyPress(text.firstOrNull() ?: ' ')
            }
        }
    }

    private fun handleBackspace() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun handleEnter() {
        currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
        currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun openSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun detectPasswordField(inputType: Int, editorInfo: EditorInfo?): Boolean {
         val inputClass = inputType and InputType.TYPE_MASK_CLASS
         val inputVariation = inputType and InputType.TYPE_MASK_VARIATION

         return inputClass == InputType.TYPE_CLASS_TEXT && (
            inputVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            inputVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            inputVariation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        ) || (inputClass == InputType.TYPE_CLASS_NUMBER && inputVariation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    }

    private fun applySecurityMode(isPassword: Boolean) {
        window?.window?.let { window ->
            if (isPassword) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    private fun createFallbackView(): View {
        return android.widget.TextView(this).apply {
            text = "Keyboard unavailable."
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
    }
}
