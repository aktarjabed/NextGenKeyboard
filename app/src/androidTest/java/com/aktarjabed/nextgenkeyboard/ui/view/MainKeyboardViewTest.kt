package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.aktarjabed.nextgenkeyboard.data.model.KeyData
import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.data.model.Layout
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainKeyboardViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockLanguage = Language(
        code = "en_TEST",
        name = "Test English",
        layout = Layout(
            rows = listOf(
                listOf(KeyData("Q"), KeyData("W"), KeyData("E")),
                listOf(KeyData("A"), KeyData("S"), KeyData("D")),
                listOf(KeyData("Z"), KeyData("X"), KeyData("C"))
            )
        )
    )

    @Test
    fun mainKeyboardView_rendersCorrectly() {
        // Arrange
        composeTestRule.setContent {
            MainKeyboardView(
                language = mockLanguage,
                suggestions = listOf("hello", "world"),
                onSuggestionClick = {},
                onKeyClick = {},
                onVoiceInputClick = {},
                onGifKeyboardClick = {},
                onSettingsClick = {}
            )
        }

        // Assert
        // Check if keys are displayed
        composeTestRule.onNodeWithText("Q").assertIsDisplayed()
        composeTestRule.onNodeWithText("S").assertIsDisplayed()
        composeTestRule.onNodeWithText("C").assertIsDisplayed()

        // Check if suggestions are displayed
        composeTestRule.onNodeWithText("hello").assertIsDisplayed()
        composeTestRule.onNodeWithText("world").assertIsDisplayed()

        // Check if action buttons are displayed
        composeTestRule.onNodeWithContentDescription("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Voice Input").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("GIF Keyboard").assertIsDisplayed()
    }

    @Test
    fun onKeyClick_isCalled_withCorrectCharacter() {
        // Arrange
        var clickedKey = ""
        composeTestRule.setContent {
            MainKeyboardView(
                language = mockLanguage,
                suggestions = emptyList(),
                onSuggestionClick = {},
                onKeyClick = { key -> clickedKey = key },
                onVoiceInputClick = {},
                onGifKeyboardClick = {},
                onSettingsClick = {}
            )
        }

        // Act
        composeTestRule.onNodeWithText("A").performClick()

        // Assert
        assertThat(clickedKey).isEqualTo("A")
    }

    @Test
    fun onSuggestionClick_isCalled_withCorrectSuggestion() {
        // Arrange
        var clickedSuggestion = ""
        composeTestRule.setContent {
            MainKeyboardView(
                language = mockLanguage,
                suggestions = listOf("suggestion1"),
                onSuggestionClick = { suggestion -> clickedSuggestion = suggestion },
                onKeyClick = {},
                onVoiceInputClick = {},
                onGifKeyboardClick = {},
                onSettingsClick = {}
            )
        }

        // Act
        composeTestRule.onNodeWithText("suggestion1").performClick()

        // Assert
        assertThat(clickedSuggestion).isEqualTo("suggestion1")
    }

    @Test
    fun onSettingsClick_isCalled() {
        // Arrange
        var settingsClicked = false
        composeTestRule.setContent {
            MainKeyboardView(
                language = mockLanguage,
                suggestions = emptyList(),
                onSuggestionClick = {},
                onKeyClick = {},
                onVoiceInputClick = {},
                onGifKeyboardClick = {},
                onSettingsClick = { settingsClicked = true }
            )
        }

        // Act
        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        // Assert
        assertThat(settingsClicked).isTrue()
    }
}