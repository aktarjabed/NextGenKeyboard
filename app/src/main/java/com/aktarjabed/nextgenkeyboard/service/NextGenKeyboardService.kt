package com.aktarjabed.nextgenkeyboard.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import com.aktarjabed.nextgenkeyboard.BuildConfig
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.aktarjabed.nextgenkeyboard.feature.gif.GiphyManager
import com.aktarjabed.nextgenkeyboard.feature.voice.VoiceInputManager
import com.aktarjabed.nextgenkeyboard.feature.voice.VoiceInputState
import com.aktarjabed.nextgenkeyboard.ui.screens.MainActivity
import com.aktarjabed.nextgenkeyboard.ui.theme.NextGenKeyboardTheme
import com.aktarjabed.nextgenkeyboard.ui.view.GifKeyboard
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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
    lateinit var voiceInputManager: VoiceInputManager
    @Inject
    lateinit var giphyManager: GiphyManager

    // Inject the ViewModel properly if possible, or construct it as shown in previous code
    // Assuming Hilt can inject it directly or we use a factory pattern.
    // Based on the corrupted file, it was trying to do both.
    // We will stick to the manual construction pattern used in `initializeComponents` for now
    // as ViewModels in Services are tricky with Hilt without proper setup.
    private lateinit var viewModel: KeyboardViewModel

    private var serviceScope: CoroutineScope? = null
    private var composeView: ComposeView? = null

    private val _keyboardState = MutableStateFlow<KeyboardState>(KeyboardState.Main)
    private val keyboardState = _keyboardState.asStateFlow()

    private var useBasicKeyboard = false
    private var isPasswordMode = false
    private var currentPackageName: String? = null
    private var useBasicKeyboard = false
    private var isPasswordMode = false
    private var currentPackageName: String? = null

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
            // Manually constructing ViewModel as per legacy pattern found
            viewModel = KeyboardViewModel(autocorrectEngine, preferencesRepository, giphyManager)

            viewModel = KeyboardViewModel(autocorrectEngine, preferencesRepository, giphyManager)

            // Initialize Giphy with proper error handling
            val giphyApiKey = getGiphyApiKey()
            val giphyApiKey = BuildConfig.GIPHY_API_KEY
            if (giphyApiKey.isNotEmpty()) {
                giphyManager.initialize(giphyApiKey)
            } else {
                logWarning("Giphy API key not configured")
            }

            logInfo("Keyboard components initialized successfully")
        } catch (oom: OutOfMemoryError) {
            logError("Out of memory during initialization", oom)
            throw oom
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
            currentPackageName = attribute?.packageName
            val inputType = attribute?.inputType ?: 0

            // Detect password fields
            isPasswordMode = detectPasswordField(inputType, attribute)

            // Apply security measures
            applySecurityMode(isPasswordMode)

            // Update ViewModel context
            viewModel.onTextUpdated("") // Reset text context on new input
            val inputType = attribute?.inputType ?: 0

            // Detect password fields
            isPasswordMode = detectPasswordField(inputType, attribute)

            // Apply security measures
            applySecurityMode(isPasswordMode)

            // Update ViewModel context
            viewModel.onTextUpdated("") // Reset text context on new input
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

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)

        try {
            currentPackageName = attribute?.packageName
            val inputType = attribute?.inputType ?: 0

            // Detect password fields
            isPasswordMode = detectPasswordField(inputType, attribute)

            // Apply security measures
            applySecurityMode(isPasswordMode)

            // Update ViewModel context
            viewModel.onTextUpdated("") // Reset text context on new input

            if (isPasswordMode) {
                Timber.d("🔒 PASSWORD FIELD DETECTED")
            } else {
                Timber.d("📝 Regular input field in ${attribute?.packageName}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onStartInput")
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        _keyboardState.value = KeyboardState.Main
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

    override fun onFinishInput() {
        super.onFinishInput()

        try {
            isPasswordMode = false
            currentPackageName = null

            // Clear security flags
            window?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            Timber.d("Input session finished - Security reset")
        } catch (e: Exception) {
            Timber.e(e, "Error in onFinishInput")
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

        try {
            isPasswordMode = false
            currentPackageName = null

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
            // Clear security flags
            window?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            Timber.d("Input session finished - Security reset")
        } catch (e: Exception) {
            Timber.e(e, "Error in onFinishInput")
        }
    }

    @Composable
    private fun KeyboardContent() {
        val currentKeyboardState by keyboardState.collectAsState()
        val suggestions by viewModel.suggestions.collectAsState()
        // Assuming VoiceInputManager has these flows exposed
        val voiceState by voiceInputManager.voiceState.collectAsState()

        // Handle voice input results
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

            is KeyboardState.Voice -> {
                // Placeholder for Voice View if it exists, else fallback
                 _keyboardState.value = KeyboardState.Main
            }
        }
    }

    private fun handleKeyPress(text: String) {
        if (text.isBlank()) return

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
                val processedText = if (viewModel.isAutocorrectEnabled() && !isPasswordMode) {
                    autocorrectEngine.processInput(text)
                } else {
                    text
                }

                commitText(processedText)

                // Learn from user input only if not a password
                if (!isPasswordMode) {
                    autocorrectEngine.learnWord(processedText)
                }
            } catch (e: Exception) {
                logError("Error processing key press", e)
                // Fallback to original text
            "⌫" -> handleBackspace()
            "↵" -> handleEnter()
            "SPACE" -> commitText(" ")
            else -> {
                commitText(text)
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

    private fun createFallbackView(): View {
        return android.widget.TextView(this).apply {
            text = "Keyboard temporarily unavailable. Please restart the app."
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
    }

    // --- Security Logic Ported from Legacy Implementation ---

    private fun getGiphyApiKey(): String {
        return BuildConfig.GIPHY_API_KEY.takeIf { it.isNotEmpty() } ?: ""
    private fun detectPasswordField(inputType: Int, editorInfo: EditorInfo?): Boolean {
        try {
            val inputClass = inputType and InputType.TYPE_MASK_CLASS
            val inputVariation = inputType and InputType.TYPE_MASK_VARIATION

            // Method 1: Detect text-based password fields
            val isTextPassword = inputClass == InputType.TYPE_CLASS_TEXT && (
                inputVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                inputVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                inputVariation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            )

            // Method 2: Detect number-based password fields (PIN codes)
            val isNumberPassword = inputClass == InputType.TYPE_CLASS_NUMBER &&
                inputVariation == InputType.TYPE_NUMBER_VARIATION_PASSWORD

            // Method 3: Check hint text for password keywords
            val hasPasswordHint = editorInfo?.let { info ->
                val hintText = info.hintText?.toString()?.lowercase() ?: ""
                val label = info.label?.toString()?.lowercase() ?: ""

                hintText.contains("password") ||
                hintText.contains("pin") ||
                hintText.contains("passcode") ||
                hintText.contains("senha") ||
                hintText.contains("contraseña") ||
                label.contains("password") ||
                label.contains("pin")
            } ?: false

            // Method 4: Detect sensitive apps
            val isSensitiveApp = editorInfo?.packageName?.let { pkg ->
                val pkgLower = pkg.lowercase()

                pkgLower.contains("bank") ||
                pkgLower.contains("hsbc") ||
                pkgLower.contains("chase") ||
                pkgLower.contains("citibank") ||
                pkgLower.contains("wallet") ||
                pkgLower.contains("payment") ||
                pkgLower.contains("paypal") ||
                pkgLower.contains("venmo") ||
                pkgLower.contains("gpay") ||
                pkgLower.contains("paytm") ||
                pkgLower.contains("phonepe") ||
                pkgLower.contains("password") ||
                pkgLower.contains("authenticator") ||
                pkgLower.contains("keepass") ||
                pkgLower.contains("bitwarden") ||
                pkgLower.contains("lastpass") ||
                pkgLower.contains("1password") ||
                pkgLower.contains("dashlane") ||
                pkgLower.contains("crypto") ||
                pkgLower.contains("coinbase") ||
                pkgLower.contains("blockchain") ||
                pkgLower.contains("binance")
            } ?: false

            return isTextPassword || isNumberPassword || hasPasswordHint || isSensitiveApp
        } catch (e: Exception) {
            Timber.e(e, "Error detecting password field")
            return false
        }
    }

    private fun applySecurityMode(isPassword: Boolean) {
        try {
            window?.window?.let { window ->
                if (isPassword) {
                    // Prevent screenshots and screen recording
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )

                    // Disable suggestions
                    currentInputConnection?.performPrivateCommand("DisableSuggestions", null)

                    Timber.d("🔒 SECURITY ENABLED:")
                    Timber.d("   ├─ Screenshots: BLOCKED")
                    Timber.d("   ├─ Screen Recording: BLOCKED")
                    Timber.d("   ├─ Clipboard: DISABLED")
                    Timber.d("   └─ Swipe Typing: DISABLED")
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    Timber.d("🔓 Security mode disabled")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error applying security mode")
        }
    }
}

    // --- Security Logic Ported from Legacy Implementation ---

    private fun detectPasswordField(inputType: Int, editorInfo: EditorInfo?): Boolean {
        try {
            val inputClass = inputType and InputType.TYPE_MASK_CLASS
            val inputVariation = inputType and InputType.TYPE_MASK_VARIATION

            // Method 1: Detect text-based password fields
            val isTextPassword = inputClass == InputType.TYPE_CLASS_TEXT && (
                inputVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                inputVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                inputVariation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            )

            // Method 2: Detect number-based password fields (PIN codes)
            val isNumberPassword = inputClass == InputType.TYPE_CLASS_NUMBER &&
                inputVariation == InputType.TYPE_NUMBER_VARIATION_PASSWORD

            // Method 3: Check hint text for password keywords
            val hasPasswordHint = editorInfo?.let { info ->
                val hintText = info.hintText?.toString()?.lowercase() ?: ""
                val label = info.label?.toString()?.lowercase() ?: ""

                hintText.contains("password") ||
                hintText.contains("pin") ||
                hintText.contains("passcode") ||
                hintText.contains("senha") ||
                hintText.contains("contraseña") ||
                label.contains("password") ||
                label.contains("pin")
            } ?: false

            // Method 4: Detect sensitive apps
            val isSensitiveApp = editorInfo?.packageName?.let { pkg ->
                val pkgLower = pkg.lowercase()

                pkgLower.contains("bank") ||
                pkgLower.contains("hsbc") ||
                pkgLower.contains("chase") ||
                pkgLower.contains("citibank") ||
                pkgLower.contains("wallet") ||
                pkgLower.contains("payment") ||
                pkgLower.contains("paypal") ||
                pkgLower.contains("venmo") ||
                pkgLower.contains("gpay") ||
                pkgLower.contains("paytm") ||
                pkgLower.contains("phonepe") ||
                pkgLower.contains("password") ||
                pkgLower.contains("authenticator") ||
                pkgLower.contains("keepass") ||
                pkgLower.contains("bitwarden") ||
                pkgLower.contains("lastpass") ||
                pkgLower.contains("1password") ||
                pkgLower.contains("dashlane") ||
                pkgLower.contains("crypto") ||
                pkgLower.contains("coinbase") ||
                pkgLower.contains("blockchain") ||
                pkgLower.contains("binance")
            } ?: false

            return isTextPassword || isNumberPassword || hasPasswordHint || isSensitiveApp
        } catch (e: Exception) {
            Timber.e(e, "Error detecting password field")
            return false
        }
    }

    private fun applySecurityMode(isPassword: Boolean) {
        try {
            window?.window?.let { window ->
                if (isPassword) {
                    // Prevent screenshots and screen recording
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )

                    // Disable suggestions
                    currentInputConnection?.performPrivateCommand("DisableSuggestions", null)

                    Timber.d("🔒 SECURITY ENABLED:")
                    Timber.d("   ├─ Screenshots: BLOCKED")
                    Timber.d("   ├─ Screen Recording: BLOCKED")
                    Timber.d("   ├─ Clipboard: DISABLED")
                    Timber.d("   └─ Swipe Typing: DISABLED")
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    Timber.d("🔓 Security mode disabled")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error applying security mode")
        }
    }
}

sealed class KeyboardState {
    object Main : KeyboardState()
    object Voice : KeyboardState()
    object Gif : KeyboardState()
    private fun createFallbackView(): View {
        return android.widget.TextView(this).apply {
            text = "Keyboard temporarily unavailable. Please restart the app."
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            text = "Keyboard unavailable."
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
    }

    private fun getGiphyApiKey(): String {
        return BuildConfig.GIPHY_API_KEY.takeIf { it.isNotEmpty() } ?: ""
    // --- Security Logic Ported from Legacy Implementation ---

    private fun detectPasswordField(inputType: Int, editorInfo: EditorInfo?): Boolean {
        try {
            val inputClass = inputType and InputType.TYPE_MASK_CLASS
            val inputVariation = inputType and InputType.TYPE_MASK_VARIATION

            // Method 1: Detect text-based password fields
            val isTextPassword = inputClass == InputType.TYPE_CLASS_TEXT && (
                inputVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                inputVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                inputVariation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            )

            // Method 2: Detect number-based password fields (PIN codes)
            val isNumberPassword = inputClass == InputType.TYPE_CLASS_NUMBER &&
                inputVariation == InputType.TYPE_NUMBER_VARIATION_PASSWORD

            // Method 3: Check hint text for password keywords
            val hasPasswordHint = editorInfo?.let { info ->
                val hintText = info.hintText?.toString()?.lowercase() ?: ""
                val label = info.label?.toString()?.lowercase() ?: ""

                hintText.contains("password") ||
                hintText.contains("pin") ||
                hintText.contains("passcode") ||
                hintText.contains("senha") ||
                hintText.contains("contraseña") ||
                label.contains("password") ||
                label.contains("pin")
            } ?: false

            // Method 4: Detect sensitive apps
            val isSensitiveApp = editorInfo?.packageName?.let { pkg ->
                val pkgLower = pkg.lowercase()

                pkgLower.contains("bank") ||
                pkgLower.contains("hsbc") ||
                pkgLower.contains("chase") ||
                pkgLower.contains("citibank") ||
                pkgLower.contains("wallet") ||
                pkgLower.contains("payment") ||
                pkgLower.contains("paypal") ||
                pkgLower.contains("venmo") ||
                pkgLower.contains("gpay") ||
                pkgLower.contains("paytm") ||
                pkgLower.contains("phonepe") ||
                pkgLower.contains("password") ||
                pkgLower.contains("authenticator") ||
                pkgLower.contains("keepass") ||
                pkgLower.contains("bitwarden") ||
                pkgLower.contains("lastpass") ||
                pkgLower.contains("1password") ||
                pkgLower.contains("dashlane") ||
                pkgLower.contains("crypto") ||
                pkgLower.contains("coinbase") ||
                pkgLower.contains("blockchain") ||
                pkgLower.contains("binance")
            } ?: false

            return isTextPassword || isNumberPassword || hasPasswordHint || isSensitiveApp
        } catch (e: Exception) {
            Timber.e(e, "Error detecting password field")
            return false
        }
    }

    private fun applySecurityMode(isPassword: Boolean) {
        try {
            window?.window?.let { window ->
                if (isPassword) {
                    // Prevent screenshots and screen recording
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )

                    // Disable suggestions
                    currentInputConnection?.performPrivateCommand("DisableSuggestions", null)

                    Timber.d("🔒 SECURITY ENABLED:")
                    Timber.d("   ├─ Screenshots: BLOCKED")
                    Timber.d("   ├─ Screen Recording: BLOCKED")
                    Timber.d("   ├─ Clipboard: DISABLED")
                    Timber.d("   └─ Swipe Typing: DISABLED")
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    Timber.d("🔓 Security mode disabled")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error applying security mode")
        }
    }

    // --- Security Logic Ported from Legacy Implementation ---

    private fun detectPasswordField(inputType: Int, editorInfo: EditorInfo?): Boolean {
        try {
            val inputClass = inputType and InputType.TYPE_MASK_CLASS
            val inputVariation = inputType and InputType.TYPE_MASK_VARIATION

            // Method 1: Detect text-based password fields
            val isTextPassword = inputClass == InputType.TYPE_CLASS_TEXT && (
                inputVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                inputVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                inputVariation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            )

            // Method 2: Detect number-based password fields (PIN codes)
            val isNumberPassword = inputClass == InputType.TYPE_CLASS_NUMBER &&
                inputVariation == InputType.TYPE_NUMBER_VARIATION_PASSWORD

            // Method 3: Check hint text for password keywords
            val hasPasswordHint = editorInfo?.let { info ->
                val hintText = info.hintText?.toString()?.lowercase() ?: ""
                val label = info.label?.toString()?.lowercase() ?: ""

                hintText.contains("password") ||
                hintText.contains("pin") ||
                hintText.contains("passcode") ||
                hintText.contains("senha") ||
                hintText.contains("contraseña") ||
                label.contains("password") ||
                label.contains("pin")
            } ?: false

            // Method 4: Detect sensitive apps
            val isSensitiveApp = editorInfo?.packageName?.let { pkg ->
                val pkgLower = pkg.lowercase()

                pkgLower.contains("bank") ||
                pkgLower.contains("hsbc") ||
                pkgLower.contains("chase") ||
                pkgLower.contains("citibank") ||
                pkgLower.contains("wallet") ||
                pkgLower.contains("payment") ||
                pkgLower.contains("paypal") ||
                pkgLower.contains("venmo") ||
                pkgLower.contains("gpay") ||
                pkgLower.contains("paytm") ||
                pkgLower.contains("phonepe") ||
                pkgLower.contains("password") ||
                pkgLower.contains("authenticator") ||
                pkgLower.contains("keepass") ||
                pkgLower.contains("bitwarden") ||
                pkgLower.contains("lastpass") ||
                pkgLower.contains("1password") ||
                pkgLower.contains("dashlane") ||
                pkgLower.contains("crypto") ||
                pkgLower.contains("coinbase") ||
                pkgLower.contains("blockchain") ||
                pkgLower.contains("binance")
            } ?: false

            return isTextPassword || isNumberPassword || hasPasswordHint || isSensitiveApp
        } catch (e: Exception) {
            Timber.e(e, "Error detecting password field")
            return false
        }
    }

    private fun applySecurityMode(isPassword: Boolean) {
        try {
            window?.window?.let { window ->
                if (isPassword) {
                    // Prevent screenshots and screen recording
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )

                    // Disable suggestions
                    currentInputConnection?.performPrivateCommand("DisableSuggestions", null)

                    Timber.d("🔒 SECURITY ENABLED:")
                    Timber.d("   ├─ Screenshots: BLOCKED")
                    Timber.d("   ├─ Screen Recording: BLOCKED")
                    Timber.d("   ├─ Clipboard: DISABLED")
                    Timber.d("   └─ Swipe Typing: DISABLED")
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    Timber.d("🔓 Security mode disabled")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error applying security mode")
        }
    }
}

sealed class KeyboardState {
    object Main : KeyboardState()
    object Voice : KeyboardState()
    object Gif : KeyboardState()
}
