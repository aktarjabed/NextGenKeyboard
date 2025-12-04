package com.nextgen.keyboard.feature.swipe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SwipePredictorTest {

    private lateinit var swipePredictor: SwipePredictor

    @Before
    fun setup() {
        swipePredictor = SwipePredictor()
    }

    @Test
    fun `predictWord should return the most frequent word`() {
        val prediction = swipePredictor.predictWord("th")
        assertEquals("the", prediction)
    }

    @Test
    fun `getSuggestions should return a list of suggestions`() {
        val suggestions = swipePredictor.getSuggestions("th", limit = 2)
        assertEquals(listOf("the", "that"), suggestions)
    }

    @Test
    fun `learnWord should add a new word to the dictionary`() {
        swipePredictor.learnWord("newword")
        val suggestions = swipePredictor.getSuggestions("neww", limit = 1)
        assertEquals(listOf("newword"), suggestions)
    }

    @Test
    fun `predictWord should return the original sequence if no match is found`() {
        val prediction = swipePredictor.predictWord("xyz")
        assertEquals("xyz", prediction)
    }

    @Test
    fun `getSuggestions should return an empty list if no match is found`() {
        val suggestions = swipePredictor.getSuggestions("xyz")
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun `predictWord should return an empty string for short input`() {
        val prediction = swipePredictor.predictWord("a")
        assertEquals("", prediction)
    }
}