package com.aktarjabed.nxtgenkeyboard.grammar

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineGrammarServiceTest {

    private val service = OfflineGrammarService()

    @Test
    fun detectsRepeatedWord() = runBlocking {
        assertTrue(service.check("This is is a test").any { it.message.startsWith("Repeated word") })
    }

    @Test
    fun detectsSpaceBeforePunctuation() = runBlocking {
        assertTrue(service.check("Hello !").any { it.message.contains("space before punctuation") })
    }
}