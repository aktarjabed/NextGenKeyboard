package com.aktarjabed.nextgenkeyboard.data.model

import java.util.Locale

/**
 * Complete language definition for the keyboard
 * @param code Locale code (e.g., "en_US", "es_ES")
 * @param name English name of the language
 * @param nativeName Native name of the language
 * @param flagIcon Emoji flag representation
 * @param layout Keyboard layout structure
 * @param isRTL True if language is right-to-left (default false)
 * @param accentMap Map of base characters to their accented variants
 */
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flagIcon: String,
    val layout: LanguageLayout,
    val isRTL: Boolean = false,
    val accentMap: Map<String, List<String>> = emptyMap()
) {
    companion object {
        /**
         * Convert language code to Locale object
         */
        fun toLocale(code: String): Locale = Locale.forLanguageTag(code.replace("_", "-"))
    }
}