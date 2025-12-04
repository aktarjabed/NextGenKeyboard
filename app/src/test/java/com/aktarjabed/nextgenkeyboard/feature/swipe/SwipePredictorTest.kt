package com.aktarjabed.nextgenkeyboard.feature.swipe

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SwipePredictorTest {

    private lateinit var predictor: SwipePredictor

    @Before
    fun setup() {
        predictor = SwipePredictor()
    }

    @Test
    fun getSuggestions_withValidPrefix_returnsSortedCandidates() {
        // Arrange
        val prefix = "th"

        // Act
        val candidates = predictor.getSuggestions(prefix)

        // Assert
        assertThat(candidates).isNotEmpty()
        // Based on the hardcoded frequencies in SwipePredictor
        assertThat(candidates).containsExactly("the", "this", "that", "think").inOrder()
    }

    @Test
    fun getSuggestions_withFullWord_returnsWord() {
        val candidates = predictor.getSuggestions("hello")
        assertThat(candidates).contains("hello")
    }

    @Test
    fun getSuggestions_withNonExistentPrefix_returnsEmptyList() {
        val candidates = predictor.getSuggestions("xyz")
        assertThat(candidates).isEmpty()
    }

    @Test
    fun getSuggestions_withShortPrefix_returnsEmptyList() {
        val candidates = predictor.getSuggestions("a")
        assertThat(candidates).isEmpty()
    }

    @Test
    fun predictWord_withValidPrefix_returnsTopSuggestion() {
        val prediction = predictor.predictWord("th")
        assertThat(prediction).isEqualTo("the")
    }

    @Test
    fun predictWord_withNonExistentPrefix_returnsOriginal() {
        val prediction = predictor.predictWord("xyz")
        assertThat(prediction).isEqualTo("xyz")
    }

    @Test
    fun learnWord_addsNewWordToDictionary() {
        // Arrange
        val newWord = "testword"

        // Act
        predictor.learnWord(newWord)
        val candidates = predictor.getSuggestions("test")

        // Assert
        assertThat(candidates).contains(newWord)
    }

    @Test
    fun learnWord_withShortWord_isIgnored() {
        predictor.learnWord("hi")
        val candidates = predictor.getSuggestions("hi")
        assertThat(candidates).isEmpty()
    }
}