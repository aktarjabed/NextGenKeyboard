package com.aktarjabed.nextgenkeyboard.util

import timber.log.Timber

object SecurityUtils {

    // Compile regex patterns once for performance
    private val OTP_PATTERN = Regex("^\\d{6}$")
    private val CREDIT_CARD_PATTERN = Regex("^\\d{13,19}$")
    private val SENSITIVE_KEYWORDS = listOf(
        "password", "token", "secret", "pin", "ssn", "api_key", "private_key"
    )

    /**
     * Detects sensitive data patterns:
     * - OTP (6 digits only)
     * - Credit cards (13-19 digits)
     * - Keywords: password, token, secret, pin, ssn, etc.
     * - High entropy + length (encrypted/API keys)
     */
    fun isSensitiveContent(text: String): Boolean {
        return try {
            when {
                // OTP: exactly 6 digits only
                text.matches(OTP_PATTERN) -> {
                    Timber.d("Detected OTP pattern")
                    true
                }

                // Credit card: 13-19 digits (with possible spaces)
                text.replace(" ", "").matches(CREDIT_CARD_PATTERN) -> {
                    Timber.d("Detected credit card pattern")
                    true
                }

                // Sensitive keywords (check whole words to avoid false positives like "spinning")
                containsSensitiveKeyword(text) -> {
                    Timber.d("Detected sensitive keyword")
                    true
                }

                // High entropy: 20+ chars with mixed digits/special chars (suggests encrypted/token)
                text.length >= 20 &&
                        text.any { it.isDigit() } &&
                        text.any { !it.isLetterOrDigit() && it != ' ' } -> {
                    Timber.d("Detected high entropy pattern (possible encrypted data)")
                    true
                }

                else -> false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking sensitive content")
            false
        }
    }

    private fun containsSensitiveKeyword(text: String): Boolean {
        val lowerText = text.lowercase()
        return SENSITIVE_KEYWORDS.any { keyword ->
            // Simple containment check for now, can be improved to regex word boundary if needed
            // But for privacy, over-matching is better than under-matching
            lowerText.contains(keyword)
        }
    }
}
