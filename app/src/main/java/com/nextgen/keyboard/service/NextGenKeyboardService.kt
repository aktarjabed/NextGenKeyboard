package com.nextgen.keyboard.service

import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.nextgen.keyboard.data.model.KeyboardLayout
import com.nextgen.keyboard.data.repository.ClipboardRepository
import com.nextgen.keyboard.data.repository.PreferencesRepository
import com.nextgen.keyboard.ui.view.NextGenKeyboardView
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
import com.nextgen.keyboard.data.repository.PreferencesRepository
import com.nextgen.keyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.nextgen.keyboard.feature.gif.GiphyManager
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NextGenKeyboardService : InputMethodService() {

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private lateinit var keyboardView: NextGenKeyboardView
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isPasswordMode = false
    private var currentPackageName: String? = null
    private var isHapticEnabled = true
    private var isSwipeEnabled = true

    override fun onCreateInputView(): View {
        Timber.d("Creating keyboard input view")

        try {
            keyboardView = NextGenKeyboardView(this).apply {
                setOnKeyPressListener { key -> handleKeyPress(key) }
            }

            // Load preferences
            loadPreferences()

            return keyboardView
        } catch (e: Exception) {
            Timber.e(e, "Error creating keyboard view")
            // Fallback to basic keyboard view
            return View(this)
        }
    }

    private fun loadPreferences() {
        // Load theme preference
        serviceScope.launch {
            try {
                preferencesRepository.isDarkMode.collect { isDark ->
                    keyboardView.updateTheme(isDark)
                    Timber.d("Theme updated: ${if (isDark) "Dark" else "Light"}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading theme preference")
            }
        }

        // Load layout preference
        serviceScope.launch {
            try {
                preferencesRepository.selectedLayout.collect { layoutName ->
                    val layout = KeyboardLayout.getLayoutByName(layoutName)
                    keyboardView.updateKeyboardLayout(layout)
                    Timber.d("Layout changed to: $layoutName")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading layout preference")
            }
        }

        // Load haptic feedback preference
        serviceScope.launch {
            try {
                preferencesRepository.isHapticFeedbackEnabled.collect { enabled ->
                    isHapticEnabled = enabled
                    keyboardView.setHapticEnabled(enabled)
                    Timber.d("Haptic feedback: ${if (enabled) "Enabled" else "Disabled"}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading haptic preference")
            }
        }

        // Load swipe typing preference
        serviceScope.launch {
            try {
                preferencesRepository.isSwipeTypingEnabled.collect { enabled ->
                    isSwipeEnabled = enabled
                    keyboardView.setSwipeEnabled(enabled)
                    Timber.d("Swipe typing: ${if (enabled) "Enabled" else "Disabled"}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading swipe preference")
            }
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

            // Update keyboard UI
            if (::keyboardView.isInitialized) {
                keyboardView.setPasswordMode(isPasswordMode)
            }

            if (isPasswordMode) {
                Timber.d("🔒 PASSWORD FIELD DETECTED")
                Timber.d("   └─ App: ${attribute?.packageName}")
                Timber.d("   └─ Input Type: ${Integer.toHexString(inputType)}")
                Timber.d("   └─ Security: ENABLED")
            } else {
                Timber.d("📝 Regular input field in ${attribute?.packageName}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onStartInput")
        }
    }

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

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd,
            newSelStart, newSelEnd,
            candidatesStart, candidatesEnd
        )

        if (isPasswordMode) {
            Timber.d("🔒 Password mode: Clipboard history disabled")
        }
    }

    private fun handleKeyPress(key: String) {
        try {
            val ic: InputConnection = currentInputConnection ?: run {
                Timber.w("InputConnection is null")
                return
            }

            when (key) {
                "BACKSPACE" -> {
                    ic.deleteSurroundingText(1, 0)
                }
                "DELETE_WORD" -> {
                    deleteWord(ic)
                }
                "ENTER" -> {
                    ic.sendKeyEvent(android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_DOWN,
                        android.view.KeyEvent.KEYCODE_ENTER
                    ))
                    ic.sendKeyEvent(android.view.KeyEvent(
                        android.view.KeyEvent.ACTION_UP,
                        android.view.KeyEvent.KEYCODE_ENTER
                    ))
                }
                "SPACE" -> {
                    ic.commitText(" ", 1)
                }
                else -> {
                    ic.commitText(key, 1)

                    // Save to clipboard history (ONLY if not in password mode)
                    if (!isPasswordMode && key.length > 1) {
                        serviceScope.launch {
                            try {
                                clipboardRepository.saveClip(key)
                                Timber.d("💾 Saved to clipboard: $key")
                            } catch (e: Exception) {
                                Timber.e(e, "Error saving to clipboard")
                            }
                        }
                    } else if (isPasswordMode) {
                        Timber.d("🔒 Password mode: Not saving to clipboard")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling key press: $key")
        }
    }

    private fun deleteWord(ic: InputConnection) {
        try {
            val textBeforeCursor = ic.getTextBeforeCursor(100, 0) ?: return
            val lastSpaceIndex = textBeforeCursor.lastIndexOf(' ')
            val deleteCount = if (lastSpaceIndex == -1) {
                textBeforeCursor.length
            } else {
                textBeforeCursor.length - lastSpaceIndex - 1
            }

            if (deleteCount > 0) {
                ic.deleteSurroundingText(deleteCount, 0)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting word")
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

    override fun onDestroy() {
        try {
            serviceScope.cancel()
            Timber.d("Keyboard service destroyed")
        } catch (e: Exception) {
            Timber.e(e, "Error in onDestroy")
        } finally {
            super.onDestroy()
        }
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        try {
            serviceScope.cancel()
        } catch (e: Exception) {
            Timber.e(e, "Error in onUnbind")
        }
        return super.onUnbind(intent)
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
        viewModel = KeyboardViewModel(autocorrectEngine, preferencesRepository, giphyManager)
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

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        _keyboardState.value = KeyboardState.Main
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
                        val currentKeyboardState by keyboardState.collectAsState()
                        val suggestions by viewModel.suggestions.collectAsState()
                        val voiceState by voiceInputManager.voiceState.collectAsState()
                        val voiceVolume by voiceInputManager.volume.collectAsState()

                        LaunchedEffect(voiceState) {
                            if (voiceState is VoiceInputState.Result) {
                                commitText((voiceState as VoiceInputState.Result).text)
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
        try {
            if (text.isBlank()) {
                logWarning("Attempted to commit empty text.")
                return
        if (text.isBlank()) return

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