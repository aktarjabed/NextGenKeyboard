package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.Context
import com.aktarjabed.nextgenkeyboard.data.local.ClipboardDatabase
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardRepositorySensitiveDataTest {

    private val context = mockk<Context>(relaxed = true)
    private val database = mockk<ClipboardDatabase>(relaxed = true)
    private val preferencesRepository = mockk<PreferencesRepository>(relaxed = true)

    // Create an instance of the repository to test the logic
    private val repository = ClipboardRepository(context, database, preferencesRepository)

    @Test
    fun `isSensitiveContent detects actual sensitive data`() {
        // OTP
        assertTrue("Should detect 6 digit OTP", repository.isSensitiveContent("123456"))

        // Password keyword
        assertTrue("Should detect password keyword", repository.isSensitiveContent("my password is secure"))
    }

    @Test
    fun `isSensitiveContent allows non-sensitive data`() {
        assertFalse("Should allow normal text", repository.isSensitiveContent("Hello world"))
        assertFalse("Should allow normal numbers", repository.isSensitiveContent("123"))
    }

    @Test
    fun `isSensitiveContent false positive check`() {
        // This is where we suspect the bug is
        assertFalse("Should allow text containing 'pin' as part of a word like 'pink'", repository.isSensitiveContent("I like pink color"))

        assertFalse("Should allow text containing 'token' as part of a word like 'tokenized'", repository.isSensitiveContent("This text is tokenized"))
    }
}
