package com.aktarjabed.nextgenkeyboard.data.model

object LanguageKeyboardDatabase {
    fun getLayout(languageCode: String): LanguageKeyboardLayout = when (languageCode) {
        // Latin
        "en" -> englishUSLayout()
        "es" -> spanishLayout()
        "fr" -> frenchLayout()
        "de" -> germanLayout()
        "it" -> italianLayout()
        "pt" -> portugueseLayout()
        "nl" -> dutchLayout()
        "pl" -> polishLayout()
        "tr" -> turkishLayout()
        "vi" -> vietnameseLayout()
        "id" -> indonesianLayout()
        "ms" -> malayLayout()
        "tl" -> filipinoLayout()
        "cs" -> czechLayout()
        "hu" -> hungarianLayout()
        "ro" -> romanianLayout()
        "sv" -> swedishLayout()
        "da" -> danishLayout()
        "no" -> norwegianLayout()
        "fi" -> finnishLayout()

        // Cyrillic
        "ru" -> russianLayout()
        "uk" -> ukrainianLayout()
        "bg" -> bulgarianLayout()

        // Greek
        "el" -> greekLayout()

        // Hebrew
        "he" -> hebrewLayout()

        // Arabic
        "ar" -> arabicLayout()
        "fa" -> persianLayout()
        "ur" -> urduLayout()

        // Indic
        "hi" -> hindiLayout()
        "bn" -> bengaliLayout()
        "ta" -> tamilLayout()
        "te" -> teluguLayout()
        "kn" -> kannadaLayout()
        "ml" -> malayalamLayout()
        "gu" -> gujaratiLayout()
        "mr" -> marathiLayout()
        "pa" -> punjabiLayout()

        // SE Asian
        "th" -> thaiLayout()

        else -> englishUSLayout()
    }

    // --- Latin Scripts ---

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

    private fun italianLayout() = LanguageKeyboardLayout(
        languageCode = "it",
        languageName = "Italian",
        nativeName = "Italiano",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        specialChars = mapOf("a" to "à", "e" to "è,é", "i" to "ì", "o" to "ò", "u" to "ù"),
        scriptType = ScriptType.LATIN
    )

    private fun portugueseLayout() = LanguageKeyboardLayout(
        languageCode = "pt",
        languageName = "Portuguese",
        nativeName = "Português",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ç"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        specialChars = mapOf("a" to "á,à,ã,â", "e" to "é,ê", "i" to "í", "o" to "ó,õ,ô", "u" to "ú", "c" to "ç"),
        scriptType = ScriptType.LATIN
    )

    private fun dutchLayout() = LanguageKeyboardLayout(
        languageCode = "nl",
        languageName = "Dutch",
        nativeName = "Nederlands",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        specialChars = mapOf("e" to "ë,é", "i" to "ï", "o" to "ö", "u" to "ü"),
        scriptType = ScriptType.LATIN
    )

    private fun polishLayout() = LanguageKeyboardLayout(
        languageCode = "pl",
        languageName = "Polish",
        nativeName = "Polski",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        specialChars = mapOf("a" to "ą", "c" to "ć", "e" to "ę", "l" to "ł", "n" to "ń", "o" to "ó", "s" to "ś", "z" to "ź,ż"),
        scriptType = ScriptType.LATIN
    )

    private fun turkishLayout() = LanguageKeyboardLayout(
        languageCode = "tr",
        languageName = "Turkish",
        nativeName = "Türkçe",
        keyRows = listOf(
            listOf("e", "r", "t", "y", "u", "ı", "o", "p", "ğ", "ü"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ş", "i"),
            listOf("z", "x", "c", "v", "b", "n", "m", "ö", "ç")
        ),
        specialChars = mapOf("g" to "ğ", "u" to "ü", "s" to "ş", "i" to "ı", "o" to "ö", "c" to "ç"),
        scriptType = ScriptType.LATIN
    )

    private fun vietnameseLayout() = LanguageKeyboardLayout(
        languageCode = "vi",
        languageName = "Vietnamese",
        nativeName = "Tiếng Việt",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        // Vietnamese usually needs an IME engine for VNI/Telex, simplified here
        scriptType = ScriptType.LATIN
    )

    private fun indonesianLayout() = LanguageKeyboardLayout(
        languageCode = "id",
        languageName = "Indonesian",
        nativeName = "Bahasa Indonesia",
        keyRows = englishUSLayout().keyRows,
        scriptType = ScriptType.LATIN
    )

    private fun malayLayout() = LanguageKeyboardLayout(
        languageCode = "ms",
        languageName = "Malay",
        nativeName = "Bahasa Melayu",
        keyRows = englishUSLayout().keyRows,
        scriptType = ScriptType.LATIN
    )

    private fun filipinoLayout() = LanguageKeyboardLayout(
        languageCode = "tl",
        languageName = "Filipino",
        nativeName = "Filipino",
        keyRows = englishUSLayout().keyRows,
        scriptType = ScriptType.LATIN
    )

    private fun czechLayout() = LanguageKeyboardLayout(
        languageCode = "cs",
        languageName = "Czech",
        nativeName = "Čeština",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("y", "x", "c", "v", "b", "n", "m")
        ),
        specialChars = mapOf("a" to "á", "c" to "č", "d" to "ď", "e" to "é,ě", "i" to "í", "n" to "ň", "o" to "ó", "r" to "ř", "s" to "š", "t" to "ť", "u" to "ú,ů", "y" to "ý", "z" to "ž"),
        scriptType = ScriptType.LATIN
    )

    private fun hungarianLayout() = LanguageKeyboardLayout(
        languageCode = "hu",
        languageName = "Hungarian",
        nativeName = "Magyar",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ő", "ú"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "é", "á", "ű"),
            listOf("y", "x", "c", "v", "b", "n", "m", "ö", "ü", "ó")
        ),
        scriptType = ScriptType.LATIN
    )

    private fun romanianLayout() = LanguageKeyboardLayout(
        languageCode = "ro",
        languageName = "Romanian",
        nativeName = "Română",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "ă"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ș", "ț", "â"),
            listOf("z", "x", "c", "v", "b", "n", "m", "î")
        ),
        scriptType = ScriptType.LATIN
    )

    private fun swedishLayout() = LanguageKeyboardLayout(
        languageCode = "sv",
        languageName = "Swedish",
        nativeName = "Svenska",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "å"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        scriptType = ScriptType.LATIN
    )

    private fun danishLayout() = LanguageKeyboardLayout(
        languageCode = "da",
        languageName = "Danish",
        nativeName = "Dansk",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "å"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "æ", "ø"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        scriptType = ScriptType.LATIN
    )

    private fun norwegianLayout() = LanguageKeyboardLayout(
        languageCode = "no",
        languageName = "Norwegian",
        nativeName = "Norsk",
        keyRows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "å"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ø", "æ"),
            listOf("z", "x", "c", "v", "b", "n", "m")
        ),
        scriptType = ScriptType.LATIN
    )

    private fun finnishLayout() = LanguageKeyboardLayout(
        languageCode = "fi",
        languageName = "Finnish",
        nativeName = "Suomi",
        keyRows = swedishLayout().keyRows, // Similar to Swedish
        scriptType = ScriptType.LATIN
    )

    // --- Cyrillic Scripts ---

    private fun russianLayout() = LanguageKeyboardLayout(
        languageCode = "ru",
        languageName = "Russian",
        nativeName = "Русский",
        keyRows = listOf(
            listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х", "ъ"),
            listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э"),
            listOf("я", "ч", "с", "м", "и", "т", "ь", "б", "ю")
        ),
        scriptType = ScriptType.CYRILLIC
    )

    private fun ukrainianLayout() = LanguageKeyboardLayout(
        languageCode = "uk",
        languageName = "Ukrainian",
        nativeName = "Українська",
        keyRows = listOf(
            listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х", "ї"),
            listOf("ф", "і", "в", "а", "п", "р", "о", "л", "д", "ж", "є"),
            listOf("я", "ч", "с", "м", "и", "т", "ь", "б", "ю", "ґ")
        ),
        scriptType = ScriptType.CYRILLIC
    )

    private fun bulgarianLayout() = LanguageKeyboardLayout(
        languageCode = "bg",
        languageName = "Bulgarian",
        nativeName = "Български",
        keyRows = listOf(
            listOf("я", "в", "е", "r", "т", "ъ", "у", "и", "о", "п", "ш", "щ", "ю"),
            listOf("а", "с", "д", "ф", "г", "х", "й", "к", "л", "ь"),
            listOf("з", "x", "ц", "ж", "б", "н", "м", "ч")
        ),
        scriptType = ScriptType.CYRILLIC
    )

    // --- Greek ---

    private fun greekLayout() = LanguageKeyboardLayout(
        languageCode = "el",
        languageName = "Greek",
        nativeName = "Ελληνικά",
        keyRows = listOf(
            listOf(";", "ς", "ε", "ρ", "τ", "υ", "θ", "ι", "ο", "π"),
            listOf("α", "σ", "δ", "φ", "γ", "η", "ξ", "κ", "λ"),
            listOf("ζ", "χ", "ψ", "ω", "β", "ν", "μ")
        ),
        scriptType = ScriptType.GREEK
    )

    // --- Hebrew ---

    private fun hebrewLayout() = LanguageKeyboardLayout(
        languageCode = "he",
        languageName = "Hebrew",
        nativeName = "עברית",
        keyRows = listOf(
            listOf("ק", "ר", "א", "ט", "ו", "ן", "ם", "פ"),
            listOf("ש", "ד", "ג", "כ", "ע", "י", "ח", "ל", "ך", "ף"),
            listOf("ז", "ס", "ב", "ה", "נ", "מ", "צ", "ת", "ץ")
        ),
        scriptType = ScriptType.HEBREW
    )

    // --- Arabic Scripts ---

    private fun arabicLayout() = LanguageKeyboardLayout(
        languageCode = "ar",
        languageName = "Arabic",
        nativeName = "العربية",
        keyRows = listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د"),
            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "m", "ك", "ط"),
            listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "و", "ز", "ظ")
        ),
        scriptType = ScriptType.ARABIC
    )

    private fun persianLayout() = LanguageKeyboardLayout(
        languageCode = "fa",
        languageName = "Persian",
        nativeName = "فارسی",
        keyRows = listOf(
            listOf("چ", "ج", "ح", "خ", "ه", "ع", "غ", "ف", "ق", "ث", "ص", "ض"),
            listOf("گ", "ک", "م", "ن", "ت", "ا", "ل", "ب", "ی", "س", "ش"),
            listOf("و", "پ", "د", "ذ", "ر", "ز", "ژ", "ط", "ظ")
        ),
        scriptType = ScriptType.ARABIC
    )

    private fun urduLayout() = LanguageKeyboardLayout(
        languageCode = "ur",
        languageName = "Urdu",
        nativeName = "اردو",
        keyRows = listOf(
            listOf("ٹ", "ط", "ص", "ج", "د", "ہ", "ا", "ر", "ک", "ل"),
            listOf("م", "ن", "ت", "ب", "گ", "ع", "س", "ف", "ق", "ح"),
            listOf("ز", "خ", "ش", "غ", "ض", "ظ", "ث", "ذ", "ڈ", "ڑ")
        ),
        scriptType = ScriptType.ARABIC
    )

    // --- Indic Scripts ---

    private fun hindiLayout() = LanguageKeyboardLayout(
        languageCode = "hi",
        languageName = "Hindi",
        nativeName = "हिंदी",
        keyRows = listOf(
            listOf("ौ", "ै", "ा", "ी", "ू", "ब", "ह", "ग", "द", "ज", "ड"),
            listOf("ो", "े", "्", "ि", "ु", "प", "र", "क", "त", "च", "ट"),
            listOf("ं", "ः", "म", "न", "व", "ल", "स", "य")
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
            listOf("ো", "ে", "্", "ি", "ু", "প", "র", "ক", "ত", "চ", "ট"),
            listOf("ং", "ঃ", "ম", "ন", "ব", "ল", "স", "য")
        ),
        specialChars = mapOf(),
        scriptType = ScriptType.DEVANAGARI
    )

    private fun tamilLayout() = LanguageKeyboardLayout(
        languageCode = "ta",
        languageName = "Tamil",
        nativeName = "தமிழ்",
        keyRows = listOf(
            listOf("ௌ", "ை", "ா", "ீ", "ூ", "ப", "ஹ", "க", "த", "ஜ", "ட"),
            listOf("ோ", "ே", "்", "ி", "ு", "ற", "ன", "க", "த", "ச", "ஞ"),
            listOf("ங", "ம", "ன", "வ", "ல", "ச", "ய", "ழ")
        ),
        scriptType = ScriptType.DEVANAGARI
    )

    private fun teluguLayout() = LanguageKeyboardLayout(
        languageCode = "te",
        languageName = "Telugu",
        nativeName = "తెలుగు",
        keyRows = listOf(
            listOf("ౌ", "ై", "ా", "ీ", "ూ", "బ", "హ", "గ", "ద", "జ", "డ"),
            listOf("ో", "ే", "్", "ి", "ు", "ప", "ర", "క", "త", "చ", "ట"),
            listOf("ం", "ః", "మ", "న", "వ", "ల", "స", "య")
        ),
        scriptType = ScriptType.DEVANAGARI
    )

    private fun kannadaLayout() = LanguageKeyboardLayout(
        languageCode = "kn",
        languageName = "Kannada",
        nativeName = "ಕನ್ನಡ",
        keyRows = listOf(
            listOf("ೌ", "ೈ", "ಾ", "ೀ", "ೂ", "ಬ", "ಹ", "ಗ", "ದ", "ಜ", "ಡ"),
            listOf("ೋ", "ೇ", "್", "ಿ", "ು", "ಪ", "ರ", "ಕ", "ತ", "ಚ", "ಟ"),
            listOf("ಂ", "ಃ", "ಮ", "ನ", "ವ", "ಲ", "ಸ", "ಯ")
        ),
        scriptType = ScriptType.DEVANAGARI
    )

    private fun malayalamLayout() = LanguageKeyboardLayout(
        languageCode = "ml",
        languageName = "Malayalam",
        nativeName = "മലയാളം",
        keyRows = listOf(
            listOf("ൌ", "ൈ", "ാ", "ീ", "ൂ", "ബ", "ഹ", "ഗ", "ദ", "ജ", "ഡ"),
            listOf("ോ", "േ", "്", "ി", "ു", "പ", "ര", "ക", "ത", "ച", "ട"),
            listOf("ം", "ഃ", "മ", "ന", "വ", "ല", "സ", "യ")
        ),
        scriptType = ScriptType.DEVANAGARI
    )

    private fun gujaratiLayout() = LanguageKeyboardLayout(
        languageCode = "gu",
        languageName = "Gujarati",
        nativeName = "ગુજરાતી",
        keyRows = listOf(
            listOf("ૌ", "ૈ", "ા", "ી", "ૂ", "બ", "હ", "ગ", "દ", "જ", "ડ"),
            listOf("ો", "ે", "્", "િ", "ુ", "પ", "ર", "ક", "ત", "ચ", "ટ"),
            listOf("ં", "ઃ", "મ", "ન", "વ", "લ", "સ", "ય")
        ),
        scriptType = ScriptType.DEVANAGARI
    )

    private fun marathiLayout() = LanguageKeyboardLayout(
        languageCode = "mr",
        languageName = "Marathi",
        nativeName = "मराठी",
        keyRows = hindiLayout().keyRows, // Same as Hindi/Devanagari
        scriptType = ScriptType.DEVANAGARI
    )

    private fun punjabiLayout() = LanguageKeyboardLayout(
        languageCode = "pa",
        languageName = "Punjabi",
        nativeName = "ਪੰਜਾਬੀ",
        keyRows = listOf(
            listOf("ੌ", "ੈ", "ਾ", "ੀ", "ੂ", "ਬ", "ਹ", "ਗ", "ਦ", "ਜ", "ਡ"),
            listOf("ੋ", "ੇ", "੍", "ਿ", "ੁ", "ਪ", "ਰ", "ਕ", "ਤ", "ਚ", "ਟ"),
            listOf("ੰ", "ਃ", "ਮ", "ਨ", "ਵ", "ਲ", "ਸ", "ਯ")
        ),
        scriptType = ScriptType.DEVANAGARI
    )

    // --- SE Asian ---

    private fun thaiLayout() = LanguageKeyboardLayout(
        languageCode = "th",
        languageName = "Thai",
        nativeName = "ไทย",
        keyRows = listOf(
            listOf("ๅ", "ภ", "ถ", "ุ", "ึ", "ค", "ต", "จ", "ข", "ช"),
            listOf("ๆ", "ไ", "ำ", "พ", "ะ", "ั", "ี", "ร", "น", "ย", "บ", "ล"),
            listOf("ฟ", "ห", "ก", "ด", "เ", "้", "่", "า", "ส", "ว", "ง")
        ),
        scriptType = ScriptType.THAI
    )
}
