package com.nextgen.keyboard.service

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import com.nextgen.keyboard.data.model.Languages
import com.nextgen.keyboard.data.repository.PreferencesRepository
import com.nextgen.keyboard.ui.screens.KeyboardView
import com.nextgen.keyboard.ui.theme.NextGenKeyboardTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NextGenKeyboardService : InputMethodService() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setContent {
                val selectedLanguageCode by preferencesRepository.selectedLanguage.collectAsState(initial = "en_US")
                val selectedLanguage = Languages.getLanguageByCode(selectedLanguageCode)

                NextGenKeyboardTheme {
                    KeyboardView(
                        language = selectedLanguage,
                        onKeyPress = { text ->
                            currentInputConnection?.commitText(text, 1)
                        }
                    )
                }
            }
        }
    }
}