package com.aktarjabed.nextgenkeyboard.data.model

/**
 * Central repository of all supported languages
 * Use LanguagesPro for runtime access with dependency injection
 */
object Languages {
    // ===== English & Major European =====
    val ENGLISH_US = Language(
        code = "en_US",
        name = "English (US)",
        nativeName = "English",
        flagIcon = "🇺🇸",
        layout = Layouts.QWERTY
    )

    val ENGLISH_UK = Language(
        code = "en_GB",
        name = "English (UK)",
        nativeName = "English (UK)",
        flagIcon = "🇬🇧",
        layout = Layouts.QWERTY
    )

    val SPANISH = Language(
        code = "es_ES",
        name = "Spanish",
        nativeName = "Español",
        flagIcon = "🇪🇸",
        layout = Layouts.QWERTY,
        accentMap = mapOf(
            "a" to listOf("á", "à", "ä", "â", "ã"),
            "e" to listOf("é", "è", "ë", "ê"),
            "i" to listOf("í", "ì", "ï", "î"),
            "n" to listOf("ñ"),
            "o" to listOf("ó", "ò", "ö", "ô", "õ"),
            "u" to listOf("ú", "ù", "ü", "û")
        )
    )

    val FRENCH = Language(
        code = "fr_FR",
        name = "French",
        nativeName = "Français",
        flagIcon = "🇫🇷",
        layout = Layouts.AZERTY,
        accentMap = mapOf(
            "a" to listOf("à", "â", "æ"),
            "c" to listOf("ç"),
            "e" to listOf("é", "è", "ê", "ë"),
            "i" to listOf("î", "ï"),
            "o" to listOf("ô", "œ"),
            "u" to listOf("ù", "û", "ü")
        )
    )

    val GERMAN = Language(
        code = "de_DE",
        name = "German",
        nativeName = "Deutsch",
        flagIcon = "🇩🇪",
        layout = Layouts.QWERTZ,
        accentMap = mapOf(
            "a" to listOf("ä"),
            "o" to listOf("ö"),
            "s" to listOf("ß"),
            "u" to listOf("ü")
        )
    )

    val ITALIAN = Language(
        code = "it_IT",
        name = "Italian",
        nativeName = "Italiano",
        flagIcon = "🇮🇹",
        layout = Layouts.QWERTY,
        accentMap = mapOf(
            "a" to listOf("à"),
            "e" to listOf("é", "è"),
            "i" to listOf("ì", "í"),
            "o" to listOf("ò", "ó"),
            "u" to listOf("ù", "ú")
        )
    )

    val PORTUGUESE = Language(
        code = "pt_PT",
        name = "Portuguese",
        nativeName = "Português",
        flagIcon = "🇵🇹",
        layout = Layouts.QWERTY,
        accentMap = mapOf(
            "a" to listOf("á", "ã", "â", "à"),
            "c" to listOf("ç"),
            "e" to listOf("é", "ê"),
            "i" to listOf("í"),
            "o" to listOf("ó", "õ", "ô"),
            "u" to listOf("ú", "ü")
        )
    )

    // ===== Indian Languages =====
    val HINDI = Language(
        code = "hi_IN",
        name = "Hindi",
        nativeName = "हिन्दी",
        flagIcon = "🇮🇳",
        layout = Layouts.HINDI,
        accentMap = mapOf(
            "a" to listOf("ा", "आ", "अ"),
            "i" to listOf("ि", "ी", "इ", "ई"),
            "u" to listOf("ु", "ू", "उ", "ऊ"),
            "e" to listOf("े", "ै", "ए", "ऐ"),
            "o" to listOf("ो", "ौ", "ओ", "औ")
        )
    )

    val BENGALI = Language(
        code = "bn_IN",
        name = "Bengali",
        nativeName = "বাংলা",
        flagIcon = "🇮🇳",
        layout = Layouts.BENGALI
    )

    // ===== Asian Languages =====
    val JAPANESE = Language(
        code = "ja_JP",
        name = "Japanese",
        nativeName = "日本語",
        flagIcon = "🇯🇵",
        layout = Layouts.QWERTY // Romaji input base
    )

    val KOREAN = Language(
        code = "ko_KR",
        name = "Korean",
        nativeName = "한국어",
        flagIcon = "🇰🇷",
        layout = Layouts.QWERTY // Romaja input base
    )

    val CHINESE_SIMPLIFIED = Language(
        code = "zh_CN",
        name = "Chinese (Simplified)",
        nativeName = "简体中文",
        flagIcon = "🇨🇳",
        layout = Layouts.QWERTY // Pinyin input base
    )

    // ===== Middle Eastern (RTL) =====
    val ARABIC = Language(
        code = "ar_SA",
        name = "Arabic",
        nativeName = "العربية",
        flagIcon = "🇸🇦",
        layout = Layouts.ARABIC,
        isRTL = true
    )

    val HEBREW = Language(
        code = "he_IL",
        name = "Hebrew",
        nativeName = "עברית",
        flagIcon = "🇮🇱",
        layout = Layouts.HEBREW,
        isRTL = true
    )

    // ===== Nordic =====
    val SWEDISH = Language(
        code = "sv_SE",
        name = "Swedish",
        nativeName = "Svenska",
        flagIcon = "🇸🇪",
        layout = Layouts.QWERTY,
        accentMap = mapOf(
            "a" to listOf("á", "à", "â"),
            "o" to listOf("ö"),
            "u" to listOf("ü")
        )
    )

    val DANISH = Language(
        code = "da_DK",
        name = "Danish",
        nativeName = "Dansk",
        flagIcon = "🇩🇰",
        layout = Layouts.QWERTY,
        accentMap = mapOf(
            "a" to listOf("æ", "å"),
            "o" to listOf("ø"),
            "u" to listOf("ü")
        )
    )

    // ===== Eastern European =====
    val POLISH = Language(
        code = "pl_PL",
        name = "Polish",
        nativeName = "Polski",
        flagIcon = "🇵🇱",
        layout = Layouts.QWERTY,
        accentMap = mapOf(
            "a" to listOf("ą"),
            "c" to listOf("ć"),
            "e" to listOf("ę"),
            "l" to listOf("ł"),
            "n" to listOf("ń"),
            "o" to listOf("ó"),
            "s" to listOf("ś"),
            "z" to listOf("ż", "ź")
        )
    )

    val RUSSIAN = Language(
        code = "ru_RU",
        name = "Russian",
        nativeName = "Русский",
        flagIcon = "🇷🇺",
        layout = Layouts.CYRILLIC
    )

    // ===== All Supported Languages =====
    val all: List<Language> = listOf(
        ENGLISH_US, ENGLISH_UK, SPANISH, FRENCH, GERMAN, ITALIAN, PORTUGUESE,
        HINDI, BENGALI,
        JAPANESE, KOREAN, CHINESE_SIMPLIFIED,
        ARABIC, HEBREW,
        SWEDISH, DANISH, POLISH, RUSSIAN
    )
}