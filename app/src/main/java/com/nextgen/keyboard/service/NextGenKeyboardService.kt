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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
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
    }
}