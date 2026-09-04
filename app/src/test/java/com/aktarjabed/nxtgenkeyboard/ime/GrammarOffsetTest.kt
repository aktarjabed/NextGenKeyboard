package com.aktarjabed.nxtgenkeyboard.ime

import org.junit.Assert.assertEquals
import org.junit.Test

class GrammarOffsetTest {

    // Pure function extracting the same logic from NxtGenKeyboard
    private fun extractSubstring(text: String, start: Int, length: Int): String {
        val end = start + length
        return text.substring(start, end)
    }

    @Test
    fun `extract ASCII text`() {
        val text = "Hello world"
        val word = extractSubstring(text, 6, 5)
        assertEquals("world", word)
    }

    @Test
    fun `extract after emoji`() {
        val text = "Hello 🌍 world"
        // 🌍 is UTF-16 surrogate pair (length 2).
        // "Hello " is 6. Emoji + space = 3. Total start for world = 9
        val word = extractSubstring(text, 9, 5)
        assertEquals("world", word)
    }

    @Test
    fun `extract before emoji`() {
        val text = "Hello 🌍 world"
        val word = extractSubstring(text, 0, 5)
        assertEquals("Hello", word)
    }

    @Test
    fun `extract with multiple supplementary code points`() {
        val text = "🌍🌎🌏 world"
        // 3 emojis * 2 chars = 6 + 1 space = 7
        val word = extractSubstring(text, 7, 5)
        assertEquals("world", word)
    }
}
