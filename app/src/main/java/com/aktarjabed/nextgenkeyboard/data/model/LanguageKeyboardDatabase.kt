package com.aktarjabed.nextgenkeyboard.data.model

object LanguageKeyboardDatabase {
    fun getLayout(languageCode: String): LanguageKeyboardLayout = when (languageCode) {
        "en" -> englishUSLayout()
        "es" -> spanishLayout()
        "fr" -> frenchLayout()
        "de" -> germanLayout()
        "ar" -> arabicLayout()
        "hi" -> hindiLayout()
        "bn" -> bengaliLayout()
        // Add more languages here mapped to layout functions
        else -> englishUSLayout()
    }

    private fun englishUSLayout() = LanguageKeyboardLayout(
        languageCode = "en",
        languageName = "English",
        nativeName = "English",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        scriptType = ScriptType.LATIN,
        region = "US"
    )

    private fun spanishLayout() = LanguageKeyboardLayout(
        languageCode = "es",
        languageName = "Spanish",
        nativeName = "Español",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ñ"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        specialChars = mapOf("a" to "á", "e" to "é", "i" to "í", "o" to "ó", "u" to "ú,ü", "n" to "ñ"),
        scriptType = ScriptType.LATIN
    )

    private fun frenchLayout() = LanguageKeyboardLayout(
        languageCode = "fr",
        languageName = "French",
        nativeName = "Français",
        keyRows = listOf(
            listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("q", "s", "d", "f", "g", "h", "j", "k", "l", "m"),
            listOf("w", "x", "c", "v", "b", "n")
        ),
        specialChars = mapOf("e" to "é,è,ê,ë", "a" to "à,â", "i" to "î,ï", "o" to "ô,œ", "u" to "ù,û,ü", "c" to "ç"),
        scriptType = ScriptType.LATIN
    )

    private fun germanLayout() = LanguageKeyboardLayout(
        languageCode = "de",
        languageName = "German",
        nativeName = "Deutsch",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ü"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä"),
            listOf("y", "x", "c", "v", "b", "n", "m", "ß")
        ),
        specialChars = mapOf("a" to "ä", "o" to "ö", "u" to "ü", "s" to "ß"),
        scriptType = ScriptType.LATIN
    )

    private fun arabicLayout() = LanguageKeyboardLayout(
        languageCode = "ar",
        languageName = "Arabic",
        nativeName = "العربية",
        keyRows = listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج"),
            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك"),
            listOf("ء", "ؤ", "ئ", "ة", "ى", "و", "ز", "ظ", "د")
        ),
        scriptType = ScriptType.ARABIC
    )

    private fun hindiLayout() = LanguageKeyboardLayout(
        languageCode = "hi",
        languageName = "Hindi",
        nativeName = "हिंदी",
        keyRows = listOf(
            listOf("ौ", "ै", "ा", "ी", "ू", "ब", "ह", "ग", "द", "ज", "ड"),
            listOf("ो", "े", "्", "ि", "ु", "प", "र", "क", "त", "च"),
            listOf("ं", "ः", "म", "न", "व", "ल", "स")
        ),
        specialChars = mapOf(),
        scriptType = ScriptType.DEVANAGARI
    )

    private fun bengaliLayout() = LanguageKeyboardLayout(
        languageCode = "bn",
        languageName = "Bengali",
        nativeName = "বাংলা",
        keyRows = listOf(
            listOf("ৌ", "ৈ", "া", "ী", "ূ", "ব", "হ", "গ", "দ", "জ", "ড"),
            listOf("ো", "ে", "্", "ি", "ু", "প", "র", "ক", "ত", "চ"),
            listOf("ং", "ঃ", "ম", "ন", "ব", "ল", "স")
        ),
        specialChars = mapOf(),
        scriptType = ScriptType.DEVANAGARI // Using Devanagari script type as proxy for Indic scripts
    )
}
