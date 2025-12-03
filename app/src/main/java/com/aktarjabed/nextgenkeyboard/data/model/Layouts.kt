package com.aktarjabed.nextgenkeyboard.data.model

/**
 * Pre-defined keyboard layouts for all supported languages
 * Each layout follows standard keyboard arrangements
 */
object Layouts {
    /**
     * Standard QWERTY layout (US/UK)
     */
    val QWERTY = LanguageLayout(
        rows = listOf(
            // Top row: QWERTYUIOP
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map {
                KeyData(primary = it, secondary = it.uppercase())
            },
            // Middle row: ASDFGHJKL
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map {
                KeyData(primary = it, secondary = it.uppercase())
            },
            // Bottom row: ZXCVBNM
            listOf("z", "x", "c", "v", "b", "n", "m").map {
                KeyData(primary = it, secondary = it.uppercase())
            }
        )
    )

    /**
     * QWERTZ layout (German, Swiss, Austrian)
     * Z and Y positions swapped from QWERTY
     */
    val QWERTZ = LanguageLayout(
        rows = listOf(
            listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p").map {
                KeyData(primary = it, secondary = it.uppercase())
            },
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map {
                KeyData(primary = it, secondary = it.uppercase())
            },
            listOf("y", "x", "c", "v", "b", "n", "m").map {
                KeyData(primary = it, secondary = it.uppercase())
            }
        )
    )

    /**
     * AZERTY layout (French, Belgian)
     * Common in French-speaking regions
     */
    val AZERTY = LanguageLayout(
        rows = listOf(
            listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p").map {
                KeyData(primary = it, secondary = it.uppercase())
            },
            listOf("q", "s", "d", "f", "g", "h", "j", "k", "l", "m").map {
                KeyData(primary = it, secondary = it.uppercase())
            },
            listOf("w", "x", "c", "v", "b", "n").map {
                KeyData(primary = it, secondary = it.uppercase())
            }
        )
    )

    /**
     * Arabic layout (RTL - Right to Left)
     * Simplified standard Arabic keyboard layout
     */
    val ARABIC = LanguageLayout(
        rows = listOf(
            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج").map { KeyData(it) },
            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك").map { KeyData(it) },
            listOf("ئ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ").map { KeyData(it) }
        )
    )

    /**
     * Hebrew layout (RTL - Right to Left)
     * Standard Hebrew keyboard layout
     */
    val HEBREW = LanguageLayout(
        rows = listOf(
            listOf("ק", "ר", "א", "ט", "ו", "ן", "ם", "פ").map { KeyData(it) },
            listOf("ש", "ד", "ג", "כ", "ע", "י", "ח", "ל", "ך", "ף").map { KeyData(it) },
            listOf("ז", "ס", "ב", "ה", "נ", "מ", "צ", "ת", "ץ").map { KeyData(it) }
        )
    )

    /**
     * Cyrillic layout (Russian, Ukrainian, Bulgarian)
     * Standard Russian keyboard layout
     */
    val CYRILLIC = LanguageLayout(
        rows = listOf(
            listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х", "ъ").map { KeyData(it) },
            listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э").map { KeyData(it) },
            listOf("я", "ч", "с", "м", "и", "т", "ь", "б", "ю", ".").map { KeyData(it) }
        )
    )

    // Hindi Layout (Devanagari)
    val HINDI = LanguageLayout(
        rows = listOf(
            listOf(
                KeyData("ौ", "१"), KeyData("ै", "२"), KeyData("ा", "३"),
                KeyData("ी", "४"), KeyData("ू", "५"), KeyData("ब", "६"),
                KeyData("ह", "७"), KeyData("ग", "८"), KeyData("द", "९"),
                KeyData("ज", "०")
            ),
            listOf(
                KeyData("ो", "ऑ"), KeyData("े", "ऐ"), KeyData("्", "आ"),
                KeyData("ि", "इ"), KeyData("ु", "उ"), KeyData("प", "फ"),
                KeyData("र", "ऱ"), KeyData("क", "ख"), KeyData("त", "थ"),
                KeyData("च", "छ")
            ),
            listOf(
                KeyData("ॉ", "ओ"), KeyData("ं", "ँ"), KeyData("म", "ण"),
                KeyData("न", "ऩ"), KeyData("व", "ळ"), KeyData("ल", "ऌ"),
                KeyData("स", "श"), KeyData("य", "य़"), KeyData(",", "।"),
                KeyData(".", "॥")
            )
        )
    )

    // Bengali Layout (Bangla)
    val BENGALI = LanguageLayout(
        rows = listOf(
            listOf(
                KeyData("ৌ", "১"), KeyData("ৈ", "২"), KeyData("া", "৩"),
                KeyData("ী", "৪"), KeyData("ূ", "৫"), KeyData("ব", "৬"),
                KeyData("হ", "৭"), KeyData("গ", "৮"), KeyData("দ", "৯"),
                KeyData("জ", "০")
            ),
            listOf(
                KeyData("ো", "ও"), KeyData("ে", "এ"), KeyData("্", "আ"),
                KeyData("ি", "ই"), KeyData("ু", "উ"), KeyData("প", "ফ"),
                KeyData("র", "ড়"), KeyData("ক", "খ"), KeyData("ত", "থ"),
                KeyData("চ", "ছ")
            ),
            listOf(
                KeyData("ং", "ঃ"), KeyData("ম", "ণ"), KeyData("ন", "ঞ"),
                KeyData("ব", "ভ"), KeyData("ল", "য়"), KeyData("স", "শ"),
                KeyData("য", "য়"), KeyData(",", "।"), KeyData(".", "॥")
            )
        )
    )

    /**
     * Japanese layout placeholder
     * Uses QWERTY base for romaji input (IME handles conversion)
     */
    val JAPANESE = QWERTY

    /**
     * Korean layout placeholder
     * Uses QWERTY base for romaja input (IME handles conversion)
     */
    val KOREAN = QWERTY

    /**
     * Chinese Simplified layout placeholder
     * Uses QWERTY base for pinyin input (IME handles conversion)
     */
    val CHINESE_SIMPLIFIED = QWERTY
}