package com.aktarjabed.nextgenkeyboard.feature.ai

import com.aktarjabed.nextgenkeyboard.util.SecurityUtils
import io.mockk.mockkObject
import io.mockk.every
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AiContextManagerTest {

    private lateinit var aiContextManager: AiContextManager

    @Before
    fun setup() {
        aiContextManager = AiContextManager()
        mockkObject(SecurityUtils)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `addCommittedText adds text to history when not sensitive`() {
        // Arrange
        val text = "Hello"
        every { SecurityUtils.isSensitiveContent(text) } returns false

        // Act
        aiContextManager.addCommittedText(text)

        // Assert
        val context = aiContextManager.getContext("World")
        assertEquals("Hello World", context)
    }

    @Test
    fun `addCommittedText does NOT add text when sensitive`() {
        // Arrange
        val sensitiveText = "MyPassword123"
        every { SecurityUtils.isSensitiveContent(sensitiveText) } returns true

        // Act
        aiContextManager.addCommittedText(sensitiveText)

        // Assert
        val context = aiContextManager.getContext("World")
        assertEquals("World", context) // History should be empty
    }

    @Test
    fun `addCommittedText trims whitespace`() {
        // Arrange
        val text = "  trimmed  "
        every { SecurityUtils.isSensitiveContent("trimmed") } returns false

        // Act
        aiContextManager.addCommittedText(text)

        // Assert
        val context = aiContextManager.getContext("input")
        assertEquals("trimmed input", context)
    }

    @Test
    fun `history size is limited to 5`() {
        // Arrange
        every { SecurityUtils.isSensitiveContent(any()) } returns false

        // Act
        for (i in 1..10) {
            aiContextManager.addCommittedText("word$i")
        }

        // Assert
        val context = aiContextManager.getContext("current")
        // Should contain last 5: word6 word7 word8 word9 word10
        val expected = "word6 word7 word8 word9 word10 current"
        assertEquals(expected, context)
    }
}
