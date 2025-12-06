package com.aktarjabed.nextgenkeyboard.data.model

import kotlinx.serialization.Serializable

enum class ScriptType {
    LATIN,           // English, Spanish, French, German, Polish, etc.
    DEVANAGARI,      // Hindi, Sanskrit, Marathi, Nepali
    ARABIC,          // Arabic, Urdu, Persian, Kurdish
    HEBREW,          // Hebrew, Yiddish
    CYRILLIC,        // Russian, Ukrainian, Bulgarian, Serbian
    GREEK,           // Greek
    GEORGIAN,        // Georgian
    ARMENIAN,        // Armenian
    CJK,             // Chinese, Japanese, Korean (requires input method)
    THAI,            // Thai, Lao
    KHMER,           // Khmer
    MYANMAR,         // Burmese
    TIBETAN,         // Tibetan
    HANGUL,          // Korean
    KATAKANA,        // Japanese
    HIRAGANA         // Japanese
}

@Serializable
data class LanguageKeyboardLayout(
    val languageCode: String,      // "en", "es", "hi", "ar", "zh", etc.
    val languageName: String,      // "English", "Español", "हिंदी", etc.
    val nativeName: String,        // "English", "Español", "हिंदी (Hindi)", etc.
    val keyRows: List<List<String>>, // Keyboard layout grid (simplified string representation)
    val specialChars: Map<String, String> = emptyMap(), // Accents: "a" -> "á,à,ä,â"
    val scriptType: ScriptType,    // LATIN, DEVANAGARI, ARABIC, CJK, etc.
    val region: String? = null     // Optional: "US", "UK", "MX", etc.
) {
    // Helper to convert to internal KeyRow structure if needed
    fun toLanguageLayout(): LanguageLayout {
        val rows = keyRows.map { rowKeys ->
            KeyRow(rowKeys.map { KeyData(0, it, it) }) // Simplified KeyData creation
        }
        return LanguageLayout(languageCode, languageName, rows, true)
    }
}
