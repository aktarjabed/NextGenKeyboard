package com.aktarjabed.nextgenkeyboard.data.repository

import com.aktarjabed.nextgenkeyboard.util.SecurityUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ClipboardRepositorySensitiveDataTest {

    @Test
    fun `isSensitiveContent_flags_exact_keywords`() {
        // "pin" is a sensitive keyword
        assertThat(SecurityUtils.isSensitiveContent("Here is my pin code")).isTrue()
        // "password" is a sensitive keyword (boundary check handles "password=" or "password:")
        assertThat(SecurityUtils.isSensitiveContent("password: 123")).isTrue()
        assertThat(SecurityUtils.isSensitiveContent("api_key")).isTrue()
    }

    @Test
    fun `isSensitiveContent_ignores_safe_substrings`() {
        // "pink" contains "pin", but should NOT be sensitive
        assertThat(SecurityUtils.isSensitiveContent("I like the color pink")).isFalse()

        // "happiness" contains "pin", should be safe
        assertThat(SecurityUtils.isSensitiveContent("happiness is key")).isFalse()

        // "spinning" contains "pin"
        assertThat(SecurityUtils.isSensitiveContent("The wheel is spinning")).isFalse()
    }

    @Test
    fun `isSensitiveContent_flags_otp_and_credit_cards`() {
        assertThat(SecurityUtils.isSensitiveContent("123456")).isTrue() // OTP
        assertThat(SecurityUtils.isSensitiveContent("4111 1111 1111 1111")).isTrue() // Credit Card
    }
}
