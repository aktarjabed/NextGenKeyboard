package com.aktarjabed.nextgenkeyboard.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.aktarjabed.nextgenkeyboard.feature.ai.AiContextManager
import com.aktarjabed.nextgenkeyboard.feature.ai.SmartPredictionUseCase
import com.aktarjabed.nextgenkeyboard.feature.autocorrect.AdvancedAutocorrectEngine
import com.aktarjabed.nextgenkeyboard.feature.gif.GiphyManager
import com.aktarjabed.nextgenkeyboard.feature.keyboard.UtilityKey
import com.aktarjabed.nextgenkeyboard.feature.keyboard.UtilityKeyAction
import com.aktarjabed.nextgenkeyboard.feature.swipe.SwipePathProcessor
import com.aktarjabed.nextgenkeyboard.feature.swipe.SwipePredictor
import com.aktarjabed.nextgenkeyboard.feature.voice.VoiceInputManager
import com.aktarjabed.nextgenkeyboard.feature.voice.VoiceInputState
import com.aktarjabed.nextgenkeyboard.ui.gestures.GestureManager
import com.aktarjabed.nextgenkeyboard.ui.screens.MainActivity
import com.aktarjabed.nextgenkeyboard.ui.theme.KeyboardThemes
import com.aktarjabed.nextgenkeyboard.ui.theme.NextGenKeyboardTheme
import com.aktarjabed.nextgenkeyboard.ui.view.EmojiKeyboard
import com.aktarjabed.nextgenkeyboard.ui.view.GifKeyboard
import com.aktarjabed.nextgenkeyboard.ui.view.MainKeyboardView
import com.aktarjabed.nextgenkeyboard.ui.viewmodel.KeyboardUiState
import com.aktarjabed.nextgenkeyboard.ui.viewmodel.KeyboardViewModel
import com.aktarjabed.nextgenkeyboard.util.logError
import com.aktarjabed.nextgenkeyboard.util.logInfo
import com.aktarjabed.nextgenkeyboard.util.logWarning
import com.aktarjabed.nextgenkeyboard.util.safeCommitText
import com.aktarjabed.nextgenkeyboard.util.safeDeleteSurroundingText
import com.aktarjabed.nextgenkeyboard.util.safeFinishComposingText
import com.aktarjabed.nextgenkeyboard.util.safeGetSelectedText
import com.aktarjabed.nextgenkeyboard.util.safePerformContextMenuAction
import com.aktarjabed.nextgenkeyboard.util.safeSendKeyEvent
import com.aktarjabed.nextgenkeyboard.util.safeSetComposingText
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
    @Inject lateinit var gestureManager: GestureManager

    // Injected for ViewModel creation
    @Inject lateinit var smartPredictionUseCase: SmartPredictionUseCase
    @Inject lateinit var aiContextManager: AiContextManager
    @Inject lateinit var compositeSuggestionProvider: com.aktarjabed.nextgenkeyboard.feature.suggestions.CompositeSuggestionProvider

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

    // Composing text state
    private val currentComposingText = StringBuilder()

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
            preferencesRepository = preferencesRepository,
            smartPredictionUseCase = smartPredictionUseCase,
            compositeSuggestionProvider = compositeSuggestionProvider,
            aiContextManager = aiContextManager,
            giphyManager = giphyManager
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

            // Fix memory leaks on Android 14+
            composeView?.let { view ->
                view.disposeComposition()
                view.setViewTreeLifecycleOwner(null)
                view.setViewTreeViewModelStoreOwner(null)
                view.setViewTreeSavedStateRegistryOwner(null)
            }
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
            // Check for package change to clear context history
            val newPackageName = attribute?.packageName
            if (newPackageName != currentPackageName) {
                aiContextManager.clearHistory()
                Timber.d("Context history cleared due to package change: $currentPackageName -> $newPackageName")
            }
            currentPackageName = newPackageName

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
            // Cancel any pending predictions
            // (ViewModel handles clearing state onInputStarted)

            isPasswordMode = false
            currentPackageName = null

            // Clear composing text on finish
            currentComposingText.clear()

            // Clear security flags
            window?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            Timber.d("Input session finished - Security reset")
        } catch (e: Exception) {
            Timber.e(e, "Error in onFinishInput")
        }
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)

        // If the selection moved and we have composing text, verify if we need to commit.
        // This is a simplified check: if the cursor moved outside the expected range, commit everything.
        // A robust implementation would check if the movement was caused by our own setComposingText.

        if (currentComposingText.isNotEmpty()) {
             // Check if the cursor is at the expected end of the composing text
             // We can use candidatesEnd as a proxy for where the composing text ends

             if (candidatesEnd != -1 && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
                 // The user moved the cursor manually (or another app did).
                 // We should commit our internal state to prevent desync.
                 Timber.d("Cursor moved outside composing region. Committing buffer.")
                 commitCurrentComposingText()
             }
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
        val uiState by viewModel.uiState.collectAsState()
        val suggestions = (uiState as? KeyboardUiState.Ready)?.suggestions ?: emptyList()

        val voiceState by voiceInputManager.voiceState.collectAsState()

        // Observe current theme ID
        val themeId by preferencesRepository.themePreference.collectAsState(initial = "light")
        val currentTheme = KeyboardThemes.ALL_THEMES.find { it.id == themeId } ?: KeyboardThemes.LIGHT

        // Debug theme changes
        LaunchedEffect(currentTheme) {
            Timber.d("Theme updated to: ${currentTheme.id}")
        }

        // Observe current language
        val languageCode by preferencesRepository.keyboardLanguage.collectAsState(initial = "en")

        // Observe clipboard history
        val recentClips by clipboardRepository.getRecentClips().collectAsState(initial = emptyList())

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
                    code = languageCode,
                    name = languageLayoutDefinition.languageName,
                    nativeName = languageLayoutDefinition.nativeName,
                    layouts = listOf(languageLayoutDefinition.toLanguageLayout()),
                    isRTL = languageLayoutDefinition.scriptType == com.aktarjabed.nextgenkeyboard.data.model.ScriptType.ARABIC ||
                            languageLayoutDefinition.scriptType == com.aktarjabed.nextgenkeyboard.data.model.ScriptType.HEBREW
                )

                MainKeyboardView(
                    language = currentLanguage,
                    suggestions = suggestions,
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
                    gestureManager = gestureManager, // Injected gesture manager
                    onUtilityKeyClick = { action -> handleUtilityAction(action) },
                    theme = currentTheme, // Pass resolved theme
                    recentClips = recentClips, // Pass real clipboard history
                    onClipSelected = { clipContent ->
                        commitText(clipContent)
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
        androidx.compose.material3.Text(
            "Voice Input (Tap to Close)",
            modifier = Modifier.clickable { onClose() }
        )
    }

    private fun handleUtilityAction(action: UtilityKeyAction) {
        try {
            if (currentInputConnection == null) {
                Timber.w("No input connection for utility action: $action")
                return
            }

            when (action) {
                UtilityKeyAction.COPY -> {
                    try {
                        val selectedText = currentInputConnection.safeGetSelectedText(0)
                        if (!selectedText.isNullOrEmpty()) {
                            serviceScope.launch {
                                clipboardRepository.copyToClipboard(selectedText.toString(), "Selection")
                            }
                        } else {
                            Timber.d("No text selected to copy")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error copying text")
                    }
                }
                UtilityKeyAction.PASTE_CLIPBOARD -> {
                    serviceScope.launch {
                        try {
                            val text = clipboardRepository.pasteFromClipboard()
                            if (!text.isNullOrEmpty()) {
                                commitText(text)
                            } else {
                                Timber.d("No clipboard content to paste")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error pasting from clipboard")
                        }
                    }
                }
                UtilityKeyAction.SELECT_ALL -> {
                    try {
                        currentInputConnection.safePerformContextMenuAction(android.R.id.selectAll)
                    } catch (e: Exception) {
                        Timber.e(e, "Error selecting all text")
                    }
                }
                UtilityKeyAction.CUT -> {
                    try {
                        currentInputConnection.safePerformContextMenuAction(android.R.id.cut)
                    } catch (e: Exception) {
                        Timber.e(e, "Error cutting text")
                    }
                }
                UtilityKeyAction.UNDO_LAST_DELETE -> {
                    try {
                        // Fallback to system undo if available
                        currentInputConnection.safePerformContextMenuAction(android.R.id.undo)
                    } catch (e: Exception) {
                        Timber.e(e, "Error performing undo")
                    }
                }
                UtilityKeyAction.INSERT_DATE -> {
                    try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        commitText(date)
                    } catch (e: Exception) {
                        Timber.e(e, "Error inserting date")
                    }
                }
                else -> {
                    Timber.w("Unhandled utility action: $action")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Critical error handling utility action: $action")
        }
    }

    private fun handleKeyPress(text: String) {
        if (text.isBlank()) {
            Timber.w("Attempted to handle blank key press")
            return
        }

        // Handle all key events sequentially in the coroutine scope to prevent race conditions
        // between synchronous Special Keys (Space/Enter) and asynchronous Character Keys.
        serviceScope.launch {
            try {
                when (text) {
                    "⌫" -> {
                        handleBackspace()
                    }
                    "↵" -> {
                        commitCurrentComposingText()
                        handleEnter()
                    }
                    "SPACE" -> {
                        commitCurrentComposingText()
                        commitText(" ")
                    }
                    else -> {
                        // Validate input connection
                        if (currentInputConnection != null) {
                            if (isPasswordMode) {
                                // In password mode, commit directly without composing text
                                commitText(text)
                            } else {
                                // Append to composing buffer
                                currentComposingText.append(text)

                                // Set composing text to show underline
                                currentInputConnection.safeSetComposingText(currentComposingText.toString(), 1)

                                // Update predictions based on composing text
                                viewModel.onTextChanged(currentComposingText.toString())
                            }
                        } else {
                            Timber.w("No input connection available")
                        }
                    }
                }
            } catch (e: Exception) {
                logError("Error processing key press in coroutine", e)
                // Fallback: try to commit original text
                try {
                    Timber.w("Fallback: Committing raw text for '$text'")
                    commitText(text)
                } catch (commitError: Exception) {
                    Timber.e(commitError, "Failed to commit fallback text")
                }
            }
        }
    }

    private fun commitCurrentComposingText() {
        if (currentComposingText.isNotEmpty()) {
            val textToCommit = currentComposingText.toString()

            // Clear buffer immediately to prevent double-commit logic in commitText
            currentComposingText.clear()

            // Process through autocorrect before committing
            val processedText = if (!isPasswordMode && autocorrectEngine.isReady()) {
                try {
                    autocorrectEngine.processInput(textToCommit)
                } catch (e: Exception) {
                    Timber.e(e, "Error in autocorrect processing")
                    textToCommit
                }
            } else {
                textToCommit
            }

            // This commit will automatically replace the active composing region
            commitText(processedText)

            // Learn the word
            if (!isPasswordMode && processedText.isNotBlank()) {
                try {
                    autocorrectEngine.learnWord(processedText)
                } catch (e: Exception) {
                    Timber.e(e, "Error learning word")
                }
            }
        }
    }

    private fun handleBackspace() {
        try {
            if (currentInputConnection == null) {
                Timber.w("No input connection for backspace")
                return
            }

            if (currentComposingText.isNotEmpty()) {
                // Delete last character from composing text
                currentComposingText.deleteCharAt(currentComposingText.length - 1)

                if (currentComposingText.isEmpty()) {
                    currentInputConnection.safeSetComposingText("", 0) // Clear composing region
                    currentInputConnection.safeFinishComposingText()
                    viewModel.onTextChanged("") // Clear suggestions
                } else {
                    currentInputConnection.safeSetComposingText(currentComposingText.toString(), 1)
                    viewModel.onTextChanged(currentComposingText.toString())
                }
            } else {
                // Fallback to deleting surrounding text if no composing text
                currentInputConnection.safeDeleteSurroundingText(1, 0)

                // Update predictions based on new context
                try {
                    val textBeforeCursor = currentInputConnection?.getTextBeforeCursor(100, 0)?.toString()
                    if (!textBeforeCursor.isNullOrBlank()) {
                        viewModel.onTextChanged(textBeforeCursor)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error updating predictions after backspace")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling backspace")
        }
    }

    private fun handleEnter() {
        try {
            if (currentInputConnection == null) {
                Timber.w("No input connection for enter")
                return
            }
            currentInputConnection.safeSendKeyEvent(
                android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER)
            )
            currentInputConnection.safeSendKeyEvent(
                android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER)
            )
        } catch (e: Exception) {
            Timber.e(e, "Error handling enter key")
        }
    }

    private fun commitText(text: String) {
        try {
            if (text.isBlank()) {
                Timber.w("Attempted to commit blank text")
                return
            }
            
            if (currentInputConnection == null) {
                Timber.w("No input connection for commit")
                return
            }

            // Always clear composing state when committing external text
            if (currentComposingText.isNotEmpty()) {
                currentComposingText.clear()
                currentInputConnection.safeFinishComposingText()
            }

            val success = currentInputConnection.safeCommitText(text, 1)
            if (success) {
                viewModel.onTextCommitted(text)
            } else {
                Timber.w("Failed to commit text: $text")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error committing text")
        }
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
