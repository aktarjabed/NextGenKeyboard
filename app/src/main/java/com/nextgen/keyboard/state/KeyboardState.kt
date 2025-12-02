package com.nextgen.keyboard.state

import android.view.inputmethod.EditorInfo
import com.nextgen.keyboard.data.models.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class KeyboardState {
    // Current language state
    private val _currentLanguage = MutableStateFlow<Language?>(null)
    val currentLanguage: StateFlow<Language?> = _currentLanguage

    // Input connection state
    private val _editorInfo = MutableStateFlow<EditorInfo?>(null)
    val editorInfo: StateFlow<EditorInfo?> = _editorInfo

    // Keyboard visibility state
    private val _isKeyboardVisible = MutableStateFlow(false)
    val isKeyboardVisible: StateFlow<Boolean> = _isKeyboardVisible

    // Text composition state
    private val _composingText = MutableStateFlow("")
    val composingText: StateFlow<String> = _composingText

    // Correction state
    private val _correctionType = MutableStateFlow<CorrectionType>(CorrectionType.NONE)
    val correctionType: StateFlow<CorrectionType> = _correctionType

    fun setLanguage(language: Language?) {
        _currentLanguage.value = language
    }

    fun setEditorInfo(info: EditorInfo?) {
        _editorInfo.value = info
    }

    fun setKeyboardVisible(visible: Boolean) {
        _isKeyboardVisible.value = visible
    }

    fun setComposingText(text: String) {
        _composingText.value = text
    }

    fun setCorrectionType(type: CorrectionType) {
        _correctionType.value = type
    }

    fun reset() {
        _currentLanguage.value = null
        _editorInfo.value = null
        _isKeyboardVisible.value = false
        _composingText.value = ""
        _correctionType.value = CorrectionType.NONE
    }
}

enum class CorrectionType {
    NONE, AUTO_CORRECT, SUGGEST_NEXT
}