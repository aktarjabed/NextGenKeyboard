package com.nextgen.keyboard.data.model

import androidx.annotation.DrawableRes
import com.nextgen.keyboard.R

data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    @DrawableRes val flagIcon: Int,
    val layout: LanguageLayout,
    val isRTL: Boolean = false,
    val accentMap: Map<String, List<String>> = emptyMap(),
    val secondarySymbols: Map<String, String> = emptyMap()
)

data class LanguageLayout(
    val rows: List<List<KeyData>>
)

data class KeyData(
    val primary: String,
    val secondary: String? = null,
    val longPressVariants: List<String> = emptyList(),
    val keyWidth: Float = 1f // Relative width
)

object Languages {

    // ✅ English (US)
    val ENGLISH_US = Language(
        code = "en_US",
        name = "English",
        nativeName = "English",
        flagIcon = R.drawable.flag_us,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1", listOf("Q", "¿")),
                    KeyData("w", "2", listOf("W", "ω")),
                    KeyData("e", "3", listOf("è", "é", "ê", "ë", "ē", "ė", "ę", "€")),
                    KeyData("r", "4", listOf("R", "®")),
                    KeyData("t", "5", listOf("T", "™")),
                    KeyData("y", "6", listOf("Y", "¥")),
                    KeyData("u", "7", listOf("ù", "ú", "û", "ü", "ū", "µ")),
                    KeyData("i", "8", listOf("ì", "í", "î", "ï", "ī", "į")),
                    KeyData("o", "9", listOf("ò", "ó", "ô", "õ", "ö", "ø", "ō", "œ")),
                    KeyData("p", "0", listOf("P", "¶"))
                ),
                listOf(
                    KeyData("a", "@", listOf("à", "á", "â", "ã", "ä", "å", "ā", "æ")),
                    KeyData("s", "#", listOf("S", "ß", "§", "$")),
                    KeyData("d", "$", listOf("D", "∂")),
                    KeyData("f", "%", listOf("F")),
                    KeyData("g", "^", listOf("G")),
                    KeyData("h", "&", listOf("H")),
                    KeyData("j", "*", listOf("J")),
                    KeyData("k", "(", listOf("K")),
                    KeyData("l", ")", listOf("L", "£"))
                ),
                listOf(
                    KeyData("z", "+", listOf("Z")),
                    KeyData("x", "=", listOf("X", "×")),
                    KeyData("c", "_", listOf("ç", "C", "©", "¢")),
                    KeyData("v", "-", listOf("V")),
                    KeyData("b", "/", listOf("B")),
                    KeyData("n", "!", listOf("ñ", "N")),
                    KeyData("m", "?", listOf("M"))
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("à", "á", "â", "ã", "ä", "å", "ā", "ă", "æ"),
            "e" to listOf("è", "é", "ê", "ë", "ē", "ė", "ę", "€"),
            "i" to listOf("ì", "í", "î", "ï", "ī", "į"),
            "o" to listOf("ò", "ó", "ô", "õ", "ö", "ø", "ō", "œ"),
            "u" to listOf("ù", "ú", "û", "ü", "ū", "ų"),
            "c" to listOf("ç", "ć", "č", "©", "¢"),
            "n" to listOf("ñ", "ń"),
            "s" to listOf("ß", "ś", "š", "§"),
            "y" to listOf("ý", "ÿ", "¥")
        )
    )

    // ✅ Spanish
    val SPANISH = Language(
        code = "es_ES",
        name = "Spanish",
        nativeName = "Español",
        flagIcon = R.drawable.flag_es,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"),
                    KeyData("w", "2"),
                    KeyData("e", "3", listOf("é", "è", "ê", "ë")),
                    KeyData("r", "4"),
                    KeyData("t", "5"),
                    KeyData("y", "6", listOf("ý")),
                    KeyData("u", "7", listOf("ú", "ü", "ù", "û")),
                    KeyData("i", "8", listOf("í", "ì", "î", "ï")),
                    KeyData("o", "9", listOf("ó", "ò", "ô", "ö")),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@", listOf("á", "à", "â", "ä")),
                    KeyData("s", "#"),
                    KeyData("d", "$"),
                    KeyData("f", "%"),
                    KeyData("g", "^"),
                    KeyData("h", "&"),
                    KeyData("j", "*"),
                    KeyData("k", "("),
                    KeyData("l", ")")
                ),
                listOf(
                    KeyData("z", "+"),
                    KeyData("x", "="),
                    KeyData("c", "_", listOf("ç")),
                    KeyData("v", "-"),
                    KeyData("b", "/"),
                    KeyData("n", "!", listOf("ñ")),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("á", "à", "â", "ä"),
            "e" to listOf("é", "è", "ê", "ë"),
            "i" to listOf("í", "ì", "î", "ï"),
            "o" to listOf("ó", "ò", "ô", "ö"),
            "u" to listOf("ú", "ü", "ù", "û"),
            "n" to listOf("ñ"),
            "c" to listOf("ç"),
            "?" to listOf("¿"),
            "!" to listOf("¡")
        )
    )

    // ✅ French
    val FRENCH = Language(
        code = "fr_FR",
        name = "French",
        nativeName = "Français",
        flagIcon = R.drawable.flag_fr,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("a", "1", listOf("à", "â", "æ")),
                    KeyData("z", "2"),
                    KeyData("e", "3", listOf("é", "è", "ê", "ë", "€")),
                    KeyData("r", "4"),
                    KeyData("t", "5"),
                    KeyData("y", "6", listOf("ÿ")),
                    KeyData("u", "7", listOf("ù", "û", "ü")),
                    KeyData("i", "8", listOf("î", "ï")),
                    KeyData("o", "9", listOf("ô", "œ")),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("q", "@"),
                    KeyData("s", "#"),
                    KeyData("d", "$"),
                    KeyData("f", "%"),
                    KeyData("g", "^"),
                    KeyData("h", "&"),
                    KeyData("j", "*"),
                    KeyData("k", "("),
                    KeyData("l", ")")
                ),
                listOf(
                    KeyData("w", "+"),
                    KeyData("x", "="),
                    KeyData("c", "_", listOf("ç")),
                    KeyData("v", "-"),
                    KeyData("b", "/"),
                    KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("à", "â", "æ"),
            "e" to listOf("é", "è", "ê", "ë", "€"),
            "i" to listOf("î", "ï"),
            "o" to listOf("ô", "œ"),
            "u" to listOf("ù", "û", "ü"),
            "c" to listOf("ç"),
            "y" to listOf("ÿ")
        )
    )

    // ✅ German
    val GERMAN = Language(
        code = "de_DE",
        name = "German",
        nativeName = "Deutsch",
        flagIcon = R.drawable.flag_de,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"),
                    KeyData("w", "2"),
                    KeyData("e", "3", listOf("é", "è", "€")),
                    KeyData("r", "4"),
                    KeyData("t", "5"),
                    KeyData("z", "6"),
                    KeyData("u", "7", listOf("ü", "ù", "ú")),
                    KeyData("i", "8"),
                    KeyData("o", "9", listOf("ö", "ò", "ó")),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@", listOf("ä", "à", "á")),
                    KeyData("s", "#", listOf("ß")),
                    KeyData("d", "$"),
                    KeyData("f", "%"),
                    KeyData("g", "^"),
                    KeyData("h", "&"),
                    KeyData("j", "*"),
                    KeyData("k", "("),
                    KeyData("l", ")")
                ),
                listOf(
                    KeyData("y", "+"),
                    KeyData("x", "="),
                    KeyData("c", "_"),
                    KeyData("v", "-"),
                    KeyData("b", "/"),
                    KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("ä", "à", "á", "â"),
            "o" to listOf("ö", "ò", "ó", "ô"),
            "u" to listOf("ü", "ù", "ú", "û"),
            "s" to listOf("ß")
        )
    )

    // ✅ Arabic (RTL)
    val ARABIC = Language(
        code = "ar_SA",
        name = "Arabic",
        nativeName = "العربية",
        flagIcon = R.drawable.flag_sa,
        isRTL = true,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ض", "1"),
                    KeyData("ص", "2"),
                    KeyData("ث", "3"),
                    KeyData("ق", "4"),
                    KeyData("ف", "5"),
                    KeyData("غ", "6"),
                    KeyData("ع", "7"),
                    KeyData("ه", "8"),
                    KeyData("خ", "9"),
                    KeyData("ح", "0")
                ),
                listOf(
                    KeyData("ش", "!"),
                    KeyData("س", "@"),
                    KeyData("ي", "#"),
                    KeyData("ب", "$"),
                    KeyData("ل", "%"),
                    KeyData("ا", "^"),
                    KeyData("ت", "&"),
                    KeyData("ن", "*"),
                    KeyData("م", "("),
                    KeyData("ك", ")")
                ),
                listOf(
                    KeyData("ظ", "+"),
                    KeyData("ط", "="),
                    KeyData("ذ", "_"),
                    KeyData("د", "-"),
                    KeyData("ج", "/"),
                    KeyData("ح", "\\"),
                    KeyData("خ", "?"),
                    KeyData("و", "!"),
                    KeyData("ز", ","),
                    KeyData("ر", ".")
                )
            )
        )
    )

    // ✅ Hindi (Devanagari)
    val HINDI = Language(
        code = "hi_IN",
        name = "Hindi",
        nativeName = "हिन्दी",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ा", "१"),
                    KeyData("ी", "२"),
                    KeyData("ू", "३"),
                    KeyData("ब", "४"),
                    KeyData("ह", "५"),
                    KeyData("ग", "६"),
                    KeyData("द", "७"),
                    KeyData("ज", "८"),
                    KeyData("ड", "९"),
                    KeyData("़", "०")
                ),
                listOf(
                    KeyData("ो", "@"),
                    KeyData("े", "#"),
                    KeyData("्", "$"),
                    KeyData("ि", "%"),
                    KeyData("ु", "^"),
                    KeyData("प", "&"),
                    KeyData("र", "*"),
                    KeyData("क", "("),
                    KeyData("त", ")"),
                    KeyData("च", "-")
                ),
                listOf(
                    KeyData("ॉ", "+"),
                    KeyData("ं", "="),
                    KeyData("म", "_"),
                    KeyData("न", ":"),
                    KeyData("व", ";"),
                    KeyData("ल", "!"),
                    KeyData("स", "?"),
                    KeyData("य", ","),
                    KeyData("ा", ".")
                )
            )
        )
    )

    // ✅ Chinese (Pinyin)
    val CHINESE_SIMPLIFIED = Language(
        code = "zh_CN",
        name = "Chinese",
        nativeName = "中文",
        flagIcon = R.drawable.flag_cn,
        layout = LanguageLayout(
            rows = listOf(
                // Standard QWERTY for Pinyin input
                listOf(
                    KeyData("q", "1"),
                    KeyData("w", "2"),
                    KeyData("e", "3"),
                    KeyData("r", "4"),
                    KeyData("t", "5"),
                    KeyData("y", "6"),
                    KeyData("u", "7"),
                    KeyData("i", "8"),
                    KeyData("o", "9"),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@"),
                    KeyData("s", "#"),
                    KeyData("d", "$"),
                    KeyData("f", "%"),
                    KeyData("g", "^"),
                    KeyData("h", "&"),
                    KeyData("j", "*"),
                    KeyData("k", "("),
                    KeyData("l", ")")
                ),
                listOf(
                    KeyData("z", "+"),
                    KeyData("x", "="),
                    KeyData("c", "_"),
                    KeyData("v", "-"),
                    KeyData("b", "/"),
                    KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        )
    )

    // ✅ Japanese (Romaji)
    val JAPANESE = Language(
        code = "ja_JP",
        name = "Japanese",
        nativeName = "日本語",
        flagIcon = R.drawable.flag_jp,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("あ", "ア"),
                    KeyData("か", "カ"),
                    KeyData("さ", "サ"),
                    KeyData("た", "タ"),
                    KeyData("な", "ナ"),
                    KeyData("は", "ハ"),
                    KeyData("ま", "マ"),
                    KeyData("や", "ヤ"),
                    KeyData("ら", "ラ"),
                    KeyData("わ", "ワ")
                ),
                listOf(
                    KeyData("い", "イ"),
                    KeyData("き", "キ"),
                    KeyData("し", "シ"),
                    KeyData("ち", "チ"),
                    KeyData("に", "ニ"),
                    KeyData("ひ", "ヒ"),
                    KeyData("み", "ミ"),
                    KeyData("り", "リ"),
                    KeyData("を", "ヲ")
                ),
                listOf(
                    KeyData("う", "ウ"),
                    KeyData("く", "ク"),
                    KeyData("す", "ス"),
                    KeyData("つ", "ツ"),
                    KeyData("ぬ", "ヌ"),
                    KeyData("ふ", "フ"),
                    KeyData("む", "ム"),
                    KeyData("ゆ", "ユ"),
                    KeyData("る", "ル"),
                    KeyData("ん", "ン")
                )
            )
        )
    )

    // ✅ Russian
    val RUSSIAN = Language(
        code = "ru_RU",
        name = "Russian",
        nativeName = "Русский",
        flagIcon = R.drawable.flag_ru,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("й", "1"),
                    KeyData("ц", "2"),
                    KeyData("у", "3"),
                    KeyData("к", "4"),
                    KeyData("е", "5", listOf("ё")),
                    KeyData("н", "6"),
                    KeyData("г", "7"),
                    KeyData("ш", "8"),
                    KeyData("щ", "9"),
                    KeyData("з", "0")
                ),
                listOf(
                    KeyData("ф", "@"),
                    KeyData("ы", "#"),
                    KeyData("в", "$"),
                    KeyData("а", "%"),
                    KeyData("п", "^"),
                    KeyData("р", "&"),
                    KeyData("о", "*"),
                    KeyData("л", "("),
                    KeyData("д", ")"),
                    KeyData("ж", "-")
                ),
                listOf(
                    KeyData("я", "+"),
                    KeyData("ч", "="),
                    KeyData("с", "_"),
                    KeyData("м", ":"),
                    KeyData("и", ";"),
                    KeyData("т", "!"),
                    KeyData("ь", "?"),
                    KeyData("б", ","),
                    KeyData("ю", ".")
                )
            )
        )
    )

    // Get all supported languages
    fun getAllLanguages(): List<Language> {
        return listOf(
            ENGLISH_US,
            SPANISH,
            FRENCH,
            GERMAN,
            ARABIC,
            HINDI,
            CHINESE_SIMPLIFIED,
            JAPANESE,
            RUSSIAN
        )
    }

    fun getLanguageByCode(code: String): Language {
        return getAllLanguages().find { it.code == code } ?: ENGLISH_US
    }
}