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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aktarjabed.nextgenkeyboard.BuildConfig
import com.aktarjabed.nextgenkeyboard.data.model.LanguageKeyboardDatabase
import com.aktarjabed.nextgenkeyboard.data.repository.ClipboardRepository
import com.aktarjabed.nextgenkeyboard.data.repository.PreferencesRepository
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.aktarjabed.nextgenkeyboard.feature.gif.GiphyManager
import com.aktarjabed.nextgenkeyboard.feature.keyboard.UtilityKey
import com.aktarjabed.nextgenkeyboard.feature.keyboard.UtilityKeyAction
import com.aktarjabed.nextgenkeyboard.feature.swipe.SwipePathProcessor
import com.aktarjabed.nextgenkeyboard.feature.swipe.SwipePredictor
import com.aktarjabed.nextgenkeyboard.feature.voice.VoiceInputManager
import com.aktarjabed.nextgenkeyboard.feature.voice.VoiceInputState
import com.aktarjabed.nextgenkeyboard.ui.screens.MainActivity
import com.aktarjabed.nextgenkeyboard.ui.theme.KeyboardThemes
import com.aktarjabed.nextgenkeyboard.ui.theme.NextGenKeyboardTheme
import com.aktarjabed.nextgenkeyboard.ui.view.EmojiKeyboard
import com.aktarjabed.nextgenkeyboard.ui.view.GifKeyboard
import com.aktarjabed.nextgenkeyboard.ui.view.MainKeyboardView
import com.aktarjabed.nextgenkeyboard.ui.viewmodel.KeyboardViewModel
import com.aktarjabed.nextgenkeyboard.util.logError
import com.aktarjabed.nextgenkeyboard.util.logInfo
import com.aktarjabed.nextgenkeyboard.util.logWarning
import com.aktarjabed.nextgenkeyboard.util.safeCommitText
import com.aktarjabed.nextgenkeyboard.util.safeDeleteSurroundingText
import com.aktarjabed.nextgenkeyboard.util.safeGetSelectedText
import com.aktarjabed.nextgenkeyboard.util.safePerformContextMenuAction
import com.aktarjabed.nextgenkeyboard.util.safeSendKeyEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Main Input Method Service for NextGenKeyboard.
 * Handles lifecycle, input connection, and Compose UI hosting.
 *
 * This class has been rewritten to:
 * 1. Use proper Hilt injection (@AndroidEntryPoint).
 * 2. Implement ViewModelStoreOwner and SavedStateRegistryOwner to support Compose ViewModels correctly.
 * 3. Clean up conflicting lifecycle methods.
 */
@AndroidEntryPoint
class NextGenKeyboardService : InputMethodService(), ViewModelStoreOwner, SavedStateRegistryOwner, LifecycleOwner {

    // Dependencies injected via Hilt
    @Inject lateinit var autocorrectEngine: AdvancedAutocorrectEngine
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var clipboardRepository: ClipboardRepository
    @Inject lateinit var voiceInputManager: VoiceInputManager
    @Inject lateinit var giphyManager: GiphyManager
    @Inject lateinit var swipePredictor: SwipePredictor
    @Inject lateinit var swipePathProcessor: SwipePathProcessor
    @Inject lateinit var utilityKeys: List<UtilityKey>

    // ViewModel - Manually created using injected dependencies
    private lateinit var viewModel: KeyboardViewModel

    // Lifecycle & State Management
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Uncaught coroutine exception", throwable)
    }
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob + exceptionHandler)

    // LifecycleOwner implementation
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    // Compose & UI
    private var composeView: ComposeView? = null
    private val _keyboardState = MutableStateFlow<KeyboardState>(KeyboardState.Main)
    private val keyboardState = _keyboardState.asStateFlow()

    // ViewModelStoreOwner implementation
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    // SavedStateRegistryOwner implementation
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    // State flags
    private var isPasswordMode = false
    private var currentPackageName: String? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        try {
            initializeComponents()
        } catch (e: Exception) {
            logError("Failed to initialize keyboard components", e)
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Explicitly recreate the input view to ensure layout adapts to new configuration
        // This is safe even if the system plans to restart the service, but ensures correct state
        // if the service is retained.
        try {
            val newInputView = onCreateInputView()
            setInputView(newInputView)
        } catch (e: Exception) {
            logError("Failed to update input view on configuration change", e)
        }
    }

    private fun initializeComponents() {
        // Construct ViewModel with injected dependencies
        viewModel = KeyboardViewModel(
            clipboardRepository = clipboardRepository,
            preferencesRepository = preferencesRepository
        )

        // Initialize Giphy
        val giphyApiKey = BuildConfig.GIPHY_API_KEY
        if (giphyApiKey.isNotEmpty()) {
            giphyManager.initialize(giphyApiKey)
        } else {
            logWarning("Giphy API key not configured")
        }

        logInfo("Keyboard components initialized successfully")
    }

    override fun onDestroy() {
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            composeView?.disposeComposition()
            composeView = null

            // if (::voiceInputManager.isInitialized) { // Check removed, assumes safe destroy or init
            //     voiceInputManager.destroy()
            // }

            serviceJob.cancel()
            store.clear() // Clear ViewModelStore

            logInfo("Keyboard service destroyed cleanly")
        } catch (e: Exception) {
            logError("Error during onDestroy", e)
        } finally {
            super.onDestroy()
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)

        try {
            currentPackageName = attribute?.packageName
            val inputType = attribute?.inputType ?: 0

            // Detect password fields
            isPasswordMode = detectPasswordField(inputType, attribute)

            // Apply security measures
            applySecurityMode(isPasswordMode)

            // Update ViewModel context
            viewModel.onInputStarted(isPasswordMode)

            if (isPasswordMode) {
                Timber.d("🔒 PASSWORD FIELD DETECTED")
            } else {
                Timber.d("📝 Regular input field in ${attribute?.packageName}")
            }

            // Clipboard Diagnostic
            val diagnostic = com.aktarjabed.nextgenkeyboard.util.ClipboardDiagnostic(this)
            Timber.d(diagnostic.runFullDiagnostic())

        } catch (e: Exception) {
            Timber.e(e, "Error in onStartInput")
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        _keyboardState.value = KeyboardState.Main
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
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

    override fun onCreateInputView(): View {
        return try {
            // Ensure lifecycle owners are set for Compose to work in a Service
            val view = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@NextGenKeyboardService)
                setViewTreeViewModelStoreOwner(this@NextGenKeyboardService)
                setViewTreeSavedStateRegistryOwner(this@NextGenKeyboardService)

                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                setContent {
                    NextGenKeyboardTheme {
                        KeyboardContent()
                    }
                }
            }
            composeView = view
            view
        } catch (e: Exception) {
            logError("Error creating input view", e)
            createFallbackView()
        }
    }

    @Composable
    private fun KeyboardContent() {
        val currentKeyboardState by keyboardState.collectAsState()
        val suggestions by viewModel.uiState.collectAsState() // Fixing suggestion flow access
        val dummySuggestions = emptyList<String>()

        val voiceState by voiceInputManager.voiceState.collectAsState()

        // Observe current theme ID
        val themeId by preferencesRepository.themePreference.collectAsState(initial = "light")
        val currentTheme = KeyboardThemes.ALL_THEMES.find { it.id == themeId } ?: KeyboardThemes.LIGHT

        // Observe current language
        val languageCode by preferencesRepository.keyboardLanguage.collectAsState(initial = "en")

        // Handle voice input results
        LaunchedEffect(voiceState) {
            if (voiceState is VoiceInputState.Result) {
                val result = voiceState as VoiceInputState.Result
                if (result.text.isNotBlank()) {
                    commitText(result.text)
                }
                _keyboardState.value = KeyboardState.Main
            }
        }

        when (currentKeyboardState) {
            is KeyboardState.Main -> {
                // Fetch layout dynamically from database based on user preference
                val languageLayoutDefinition = LanguageKeyboardDatabase.getLayout(languageCode)
                val currentLanguage = com.aktarjabed.nextgenkeyboard.data.model.Language(
                    locale = Locale(languageCode),
                    displayName = languageLayoutDefinition.languageName,
                    nativeName = languageLayoutDefinition.nativeName,
                    layouts = listOf(languageLayoutDefinition.toLanguageLayout()),
                    isRTL = languageLayoutDefinition.scriptType == com.aktarjabed.nextgenkeyboard.data.model.ScriptType.ARABIC ||
                            languageLayoutDefinition.scriptType == com.aktarjabed.nextgenkeyboard.data.model.ScriptType.HEBREW
                )

                MainKeyboardView(
                    language = currentLanguage,
                    suggestions = dummySuggestions,
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
                    },
                    onEmojiClick = {
                        _keyboardState.value = KeyboardState.Emoji
                    },
                    swipePredictor = swipePredictor, // Injected predictor
                    swipePathProcessor = swipePathProcessor, // Injected processor
                    utilityKeys = utilityKeys, // Injected utility keys
                    onUtilityKeyClick = { action -> handleUtilityAction(action) },
                    theme = currentTheme // Pass resolved theme
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
                            }
                        } finally {
                            _keyboardState.value = KeyboardState.Main
                        }
                    },
                    onClose = { _keyboardState.value = KeyboardState.Main }
                )
            }

            is KeyboardState.Voice -> {
                 VoiceInputViewWrapper(
                     onClose = { _keyboardState.value = KeyboardState.Main }
                 )
            }

            is KeyboardState.Emoji -> {
                EmojiKeyboard(
                    viewModel = viewModel,
                    onEmojiSelected = { emoji ->
                        commitText(emoji)
                    },
                    onBackspace = {
                        handleBackspace()
                    },
                    onBackToAlphabet = {
                        _keyboardState.value = KeyboardState.Main
                    }
                )
            }
        }
    }

    @Composable
    fun VoiceInputViewWrapper(onClose: () -> Unit) {
        // Placeholder for VoiceInputView
        androidx.compose.material3.Text("Voice Input (Tap to Close)", modifier = androidx.compose.ui.Modifier.androidx.compose.foundation.clickable { onClose() })
    }

    private fun handleUtilityAction(action: UtilityKeyAction) {
        when (action) {
            UtilityKeyAction.COPY -> {
                val selectedText = currentInputConnection.safeGetSelectedText(0)
                if (!selectedText.isNullOrEmpty()) {
                    serviceScope.launch { clipboardRepository.copyToClipboard(selectedText.toString(), "Selection") }
                }
            }
            UtilityKeyAction.PASTE_CLIPBOARD -> {
                serviceScope.launch {
                    val text = clipboardRepository.pasteFromClipboard()
                    if (!text.isNullOrEmpty()) {
                        commitText(text)
                    }
                }
            }
            UtilityKeyAction.SELECT_ALL -> {
                currentInputConnection.safePerformContextMenuAction(android.R.id.selectAll)
            }
            UtilityKeyAction.CUT -> {
                currentInputConnection.safePerformContextMenuAction(android.R.id.cut)
            }
            UtilityKeyAction.UNDO_LAST_DELETE -> {
                // Not standard Android API, often requires tracking history.
                // Fallback to system undo if available.
                currentInputConnection.safePerformContextMenuAction(android.R.id.undo)
            }
            UtilityKeyAction.INSERT_DATE -> {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                commitText(date)
            }
            // Add other cases as needed
            else -> {}
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

        serviceScope.launch {
            try {
                // Process through autocorrect if enabled
                // Note: autocorrectEngine.processInput is not fully implemented yet, but safe to call
                val processedText = if (!isPasswordMode) {
                    autocorrectEngine.processInput(text)
                } else {
                    text
                }

                commitText(processedText)

                if (!isPasswordMode) {
                    autocorrectEngine.learnWord(processedText)
                }
            } catch (e: Exception) {
                logError("Error processing key press", e)
                commitText(text)
            }
        }
    }

    private fun handleBackspace() {
        currentInputConnection.safeDeleteSurroundingText(1, 0)
    }

    private fun handleEnter() {
        currentInputConnection.safeSendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
        currentInputConnection.safeSendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
    }

    private fun commitText(text: String) {
        currentInputConnection.safeCommitText(text, 1)
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
            text = "Keyboard unavailable."
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
    }

    // --- Security Logic ---

    private fun detectPasswordField(inputType: Int, editorInfo: EditorInfo?): Boolean {
        // Simplified detection logic
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val inputVariation = inputType and InputType.TYPE_MASK_VARIATION

        val isTextPassword = inputClass == InputType.TYPE_CLASS_TEXT && (
            inputVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            inputVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            inputVariation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        )

        val isNumberPassword = inputClass == InputType.TYPE_CLASS_NUMBER &&
            inputVariation == InputType.TYPE_NUMBER_VARIATION_PASSWORD

        return isTextPassword || isNumberPassword
    }

    private fun applySecurityMode(isPassword: Boolean) {
        try {
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
        } catch (e: Exception) {
            Timber.e(e, "Error applying security mode")
        }
    }
}

sealed class KeyboardState {
    object Main : KeyboardState()
    object Voice : KeyboardState()
    object Gif : KeyboardState()
    object Emoji : KeyboardState()
}
