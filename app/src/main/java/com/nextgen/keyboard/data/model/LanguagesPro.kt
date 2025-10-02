package com.nextgen.keyboard.data.model

import com.nextgen.keyboard.R

object LanguagesPro {

    // ========== INDIAN LANGUAGES ==========

    // ✅ Hindi (Devanagari)
    val HINDI = Language(
        code = "hi_IN",
        name = "Hindi",
        nativeName = "हिन्दी",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
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
        ),
        accentMap = mapOf(
            "a" to listOf("ा", "आ", "अ"),
            "i" to listOf("ि", "ी", "इ", "ई"),
            "u" to listOf("ु", "ू", "उ", "ऊ"),
            "e" to listOf("े", "ै", "ए", "ऐ"),
            "o" to listOf("ो", "ौ", "ओ", "औ")
        )
    )

    // ✅ Bengali (Bangla)
    val BENGALI = Language(
        code = "bn_IN",
        name = "Bengali",
        nativeName = "বাংলা",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
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
    )

    // ✅ Tamil
    val TAMIL = Language(
        code = "ta_IN",
        name = "Tamil",
        nativeName = "தமிழ்",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ௌ", "௧"), KeyData("ை", "௨"), KeyData("ா", "௩"),
                    KeyData("ீ", "௪"), KeyData("ூ", "௫"), KeyData("ப", "௬"),
                    KeyData("ஹ", "௭"), KeyData("க", "௮"), KeyData("த", "௯"),
                    KeyData("ச", "௦")
                ),
                listOf(
                    KeyData("ோ", "ஔ"), KeyData("ே", "ஏ"), KeyData("்", "ஆ"),
                    KeyData("ி", "இ"), KeyData("ு", "உ"), KeyData("ம", "ங"),
                    KeyData("ன", "ண"), KeyData("வ", "ள"), KeyData("ர", "ற"),
                    KeyData("ல", "ழ")
                ),
                listOf(
                    KeyData("ஂ", "ஃ"), KeyData("ய", "ஞ"), KeyData("ள", "ற"),
                    KeyData("ன", "ண"), KeyData(",", "।"), KeyData(".", "॥")
                )
            )
        )
    )

    // ✅ Telugu
    val TELUGU = Language(
        code = "te_IN",
        name = "Telugu",
        nativeName = "తెలుగు",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ౌ", "౧"), KeyData("ై", "౨"), KeyData("ా", "౩"),
                    KeyData("ీ", "౪"), KeyData("ూ", "౫"), KeyData("బ", "౬"),
                    KeyData("హ", "౭"), KeyData("గ", "౮"), KeyData("ద", "౯"),
                    KeyData("జ", "౦")
                ),
                listOf(
                    KeyData("ో", "ఔ"), KeyData("ే", "ఏ"), KeyData("్", "ఆ"),
                    KeyData("ి", "ఇ"), KeyData("ు", "ఉ"), KeyData("ప", "ఫ"),
                    KeyData("ర", "ఋ"), KeyData("క", "ఖ"), KeyData("త", "థ"),
                    KeyData("చ", "ఛ")
                ),
                listOf(
                    KeyData("ం", "ః"), KeyData("మ", "ణ"), KeyData("న", "ఞ"),
                    KeyData("వ", "ళ"), KeyData("ల", "ఱ"), KeyData("స", "శ"),
                    KeyData("య", "ౘ"), KeyData(",", "।"), KeyData(".", "॥")
                )
            )
        )
    )

    // ✅ Malayalam
    val MALAYALAM = Language(
        code = "ml_IN",
        name = "Malayalam",
        nativeName = "മലയാളം",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ൌ", "൧"), KeyData("ൈ", "൨"), KeyData("ാ", "൩"),
                    KeyData("ീ", "൪"), KeyData("ൂ", "൫"), KeyData("ബ", "൬"),
                    KeyData("ഹ", "൭"), KeyData("ഗ", "൮"), KeyData("ദ", "൯"),
                    KeyData("ജ", "൦")
                ),
                listOf(
                    KeyData("ോ", "ഔ"), KeyData("േ", "ഏ"), KeyData("്", "ആ"),
                    KeyData("ി", "ഇ"), KeyData("ു", "ഉ"), KeyData("പ", "ഫ"),
                    KeyData("ര", "ഋ"), KeyData("ക", "ഖ"), KeyData("ത", "ഥ"),
                    KeyData("ച", "ഛ")
                ),
                listOf(
                    KeyData("ം", "ഃ"), KeyData("മ", "ണ"), KeyData("ന", "ഞ"),
                    KeyData("വ", "ള"), KeyData("ല", "ഴ"), KeyData("സ", "ശ"),
                    KeyData("യ", "യ"), KeyData(",", "।"), KeyData(".", "॥")
                )
            )
        )
    )

    // ✅ Kannada
    val KANNADA = Language(
        code = "kn_IN",
        name = "Kannada",
        nativeName = "ಕನ್ನಡ",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ೌ", "೧"), KeyData("ೈ", "೨"), KeyData("ಾ", "೩"),
                    KeyData("ೀ", "೪"), KeyData("ೂ", "೫"), KeyData("ಬ", "೬"),
                    KeyData("ಹ", "೭"), KeyData("ಗ", "೮"), KeyData("ದ", "೯"),
                    KeyData("ಜ", "೦")
                ),
                listOf(
                    KeyData("ೋ", "ಔ"), KeyData("ೇ", "ಏ"), KeyData("್", "ಆ"),
                    KeyData("ಿ", "ಇ"), KeyData("ು", "ಉ"), KeyData("ಪ", "ಫ"),
                    KeyData("ರ", "ಋ"), KeyData("ಕ", "ಖ"), KeyData("ತ", "ಥ"),
                    KeyData("ಚ", "ಛ")
                ),
                listOf(
                    KeyData("ಂ", "ಃ"), KeyData("ಮ", "ಣ"), KeyData("ನ", "ಞ"),
                    KeyData("ವ", "ಳ"), KeyData("ಲ", "ೞ"), KeyData("ಸ", "ಶ"),
                    KeyData("ಯ", "ಯ"), KeyData(",", "।"), KeyData(".", "॥")
                )
            )
        )
    )

    // ✅ Marathi
    val MARATHI = Language(
        code = "mr_IN",
        name = "Marathi",
        nativeName = "मराठी",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
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
    )

    // ✅ Gujarati
    val GUJARATI = Language(
        code = "gu_IN",
        name = "Gujarati",
        nativeName = "ગુજરાતી",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ૌ", "૧"), KeyData("ૈ", "૨"), KeyData("ા", "૩"),
                    KeyData("ી", "૪"), KeyData("ૂ", "૫"), KeyData("બ", "૬"),
                    KeyData("હ", "૭"), KeyData("ગ", "૮"), KeyData("દ", "૯"),
                    KeyData("જ", "૦")
                ),
                listOf(
                    KeyData("ો", "ઔ"), KeyData("ે", "એ"), KeyData("્", "આ"),
                    KeyData("િ", "ઇ"), KeyData("ુ", "ઉ"), KeyData("પ", "ફ"),
                    KeyData("ર", "ઋ"), KeyData("ક", "ખ"), KeyData("ત", "થ"),
                    KeyData("ચ", "છ")
                ),
                listOf(
                    KeyData("ં", "ઃ"), KeyData("મ", "ણ"), KeyData("ન", "ઞ"),
                    KeyData("વ", "ળ"), KeyData("લ", "ળ"), KeyData("સ", "શ"),
                    KeyData("ય", "ય"), KeyData(",", "।"), KeyData(".", "॥")
                )
            )
        )
    )

    // ✅ Punjabi (Gurmukhi)
    val PUNJABI = Language(
        code = "pa_IN",
        name = "Punjabi",
        nativeName = "ਪੰਜਾਬੀ",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ੌ", "੧"), KeyData("ੈ", "੨"), KeyData("ਾ", "੩"),
                    KeyData("ੀ", "੪"), KeyData("ੂ", "੫"), KeyData("ਬ", "੬"),
                    KeyData("ਹ", "੭"), KeyData("ਗ", "੮"), KeyData("ਦ", "੯"),
                    KeyData("ਜ", "੦")
                ),
                listOf(
                    KeyData("ੋ", "ਔ"), KeyData("ੇ", "ਏ"), KeyData("੍", "ਆ"),
                    KeyData("ਿ", "ਇ"), KeyData("ੁ", "ਉ"), KeyData("ਪ", "ਫ"),
                    KeyData("ਰ", "ੜ"), KeyData("ਕ", "ਖ"), KeyData("ਤ", "ਥ"),
                    KeyData("ਚ", "ਛ")
                ),
                listOf(
                    KeyData("ਂ", "ਃ"), KeyData("ਮ", "ਣ"), KeyData("ਨ", "ਞ"),
                    KeyData("ਵ", "ਲ਼"), KeyData("ਲ", "ਲ਼"), KeyData("ਸ", "ਸ਼"),
                    KeyData("ਯ", "ਯ"), KeyData(",", "।"), KeyData(".", "॥")
                )
            )
        )
    )

    // ✅ Odia
    val ODIA = Language(
        code = "or_IN",
        name = "Odia",
        nativeName = "ଓଡ଼ିଆ",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ୌ", "୧"), KeyData("ୈ", "୨"), KeyData("ା", "୩"),
                    KeyData("ୀ", "୪"), KeyData("ୂ", "୫"), KeyData("ବ", "୬"),
                    KeyData("ହ", "୭"), KeyData("ଗ", "୮"), KeyData("ଦ", "୯"),
                    KeyData("ଜ", "୦")
                ),
                listOf(
                    KeyData("ୋ", "ଔ"), KeyData("େ", "ଏ"), KeyData("୍", "ଆ"),
                    KeyData("ି", "ଇ"), KeyData("ୁ", "ଉ"), KeyData("ପ", "ଫ"),
                    KeyData("ର", "ଋ"), KeyData("କ", "ଖ"), KeyData("ତ", "ଥ"),
                    KeyData("ଚ", "ଛ")
                ),
                listOf(
                    KeyData("ଂ", "ଃ"), KeyData("ମ", "ଣ"), KeyData("ନ", "ଞ"),
                    KeyData("ଵ", "ଳ"), KeyData("ଲ", "ଳ"), KeyData("ସ", "ଶ"),
                    KeyData("ଯ", "ୟ"), KeyData(",", "।"), KeyData(".", "॥")
                )
            )
        )
    )

    // ✅ Assamese
    val ASSAMESE = Language(
        code = "as_IN",
        name = "Assamese",
        nativeName = "অসমীয়া",
        flagIcon = R.drawable.flag_in,
        layout = LanguageLayout(
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
                    KeyData("ৰ", "ড়"), KeyData("ক", "খ"), KeyData("ত", "থ"),
                    KeyData("চ", "ছ")
                ),
                listOf(
                    KeyData("ং", "ঃ"), KeyData("ম", "ণ"), KeyData("ন", "ঞ"),
                    KeyData("ৱ", "য়"), KeyData("ল", "ঌ"), KeyData("স", "শ"),
                    KeyData("য", "য়"), KeyData(",", "।"), KeyData(".", "॥")
                )
            )
        )
    )

    // ✅ Urdu
    val URDU = Language(
        code = "ur_PK",
        name = "Urdu",
        nativeName = "اردو",
        flagIcon = R.drawable.flag_pk,
        isRTL = true,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ط", "۱"), KeyData("ص", "۲"), KeyData("ث", "۳"),
                    KeyData("ق", "۴"), KeyData("ف", "۵"), KeyData("غ", "۶"),
                    KeyData("ع", "۷"), KeyData("ہ", "۸"), KeyData("خ", "۹"),
                    KeyData("ح", "۰")
                ),
                listOf(
                    KeyData("ش", "!"), KeyData("س", "@"), KeyData("ی", "#"),
                    KeyData("ب", "$"), KeyData("ل", "%"), KeyData("ا", "^"),
                    KeyData("ت", "&"), KeyData("ن", "*"), KeyData("م", "("),
                    KeyData("ک", ")")
                ),
                listOf(
                    KeyData("ظ", "+"), KeyData("ط", "="), KeyData("ذ", "_"),
                    KeyData("د", "-"), KeyData("ج", "/"), KeyData("ح", "\\"),
                    KeyData("خ", "?"), KeyData("و", "!"), KeyData("ز", ","),
                    KeyData("ر", ".")
                )
            )
        )
    )

    // ========== ASIAN LANGUAGES ==========

    // ✅ Korean
    val KOREAN = Language(
        code = "ko_KR",
        name = "Korean",
        nativeName = "한국어",
        flagIcon = R.drawable.flag_kr,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ㅂ", "1"), KeyData("ㅈ", "2"), KeyData("ㄷ", "3"),
                    KeyData("ㄱ", "4"), KeyData("ㅅ", "5"), KeyData("ㅛ", "6"),
                    KeyData("ㅕ", "7"), KeyData("ㅑ", "8"), KeyData("ㅐ", "9"),
                    KeyData("ㅔ", "0")
                ),
                listOf(
                    KeyData("ㅁ", "!"), KeyData("ㄴ", "@"), KeyData("ㅇ", "#"),
                    KeyData("ㄹ", "$"), KeyData("ㅎ", "%"), KeyData("ㅗ", "^"),
                    KeyData("ㅓ", "&"), KeyData("ㅏ", "*"), KeyData("ㅣ", "(")
                ),
                listOf(
                    KeyData("ㅋ", "+"), KeyData("ㅌ", "="), KeyData("ㅊ", "_"),
                    KeyData("ㅍ", "-"), KeyData("ㅠ", "/"), KeyData("ㅜ", "\\"),
                    KeyData("ㅡ", "?")
                )
            )
        )
    )

    // ✅ Vietnamese
    val VIETNAMESE = Language(
        code = "vi_VN",
        name = "Vietnamese",
        nativeName = "Tiếng Việt",
        flagIcon = R.drawable.flag_vn,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"), KeyData("w", "2"), KeyData("e", "3", listOf("ê", "é", "è", "ẻ", "ẽ", "ẹ")),
                    KeyData("r", "4"), KeyData("t", "5"), KeyData("y", "6", listOf("ý", "ỳ", "ỷ", "ỹ", "ỵ")),
                    KeyData("u", "7", listOf("ư", "ú", "ù", "ủ", "ũ", "ụ")),
                    KeyData("i", "8", listOf("í", "ì", "ỉ", "ĩ", "ị")),
                    KeyData("o", "9", listOf("ô", "ơ", "ó", "ò", "ỏ", "õ", "ọ")),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@", listOf("â", "ă", "á", "à", "ả", "ã", "ạ")),
                    KeyData("s", "#"), KeyData("d", "$", listOf("đ")),
                    KeyData("f", "%"), KeyData("g", "^"), KeyData("h", "&"),
                    KeyData("j", "*"), KeyData("k", "("), KeyData("l", ")")
                ),
                listOf(
                    KeyData("z", "+"), KeyData("x", "="), KeyData("c", "_"),
                    KeyData("v", "-"), KeyData("b", "/"), KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("à", "á", "ả", "ã", "ạ", "â", "ă"),
            "e" to listOf("è", "é", "ẻ", "ẽ", "ẹ", "ê"),
            "i" to listOf("ì", "í", "ỉ", "ĩ", "ị"),
            "o" to listOf("ò", "ó", "ỏ", "õ", "ọ", "ô", "ơ"),
            "u" to listOf("ù", "ú", "ủ", "ũ", "ụ", "ư"),
            "y" to listOf("ỳ", "ý", "ỷ", "ỹ", "ỵ"),
            "d" to listOf("đ")
        )
    )

    // ✅ Thai
    val THAI = Language(
        code = "th_TH",
        name = "Thai",
        nativeName = "ไทย",
        flagIcon = R.drawable.flag_th,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ๆ", "๑"), KeyData("ไ", "๒"), KeyData("ำ", "๓"),
                    KeyData("พ", "๔"), KeyData("ะ", "๕"), KeyData("ั", "๖"),
                    KeyData("ี", "๗"), KeyData("ร", "๘"), KeyData("น", "๙"),
                    KeyData("ย", "๐")
                ),
                listOf(
                    KeyData("ฟ", "!"), KeyData("ห", "@"), KeyData("ก", "#"),
                    KeyData("ด", "$"), KeyData("เ", "%"), KeyData("้", "^"),
                    KeyData("่", "&"), KeyData("า", "*"), KeyData("ส", "("),
                    KeyData("ว", ")")
                ),
                listOf(
                    KeyData("ผ", "+"), KeyData("ป", "="), KeyData("แ", "_"),
                    KeyData("อ", "-"), KeyData("ิ", "/"), KeyData("ื", "\\"),
                    KeyData("ท", "?"), KeyData("ม", "!"), KeyData("ใ", ","),
                    KeyData("ฝ", ".")
                )
            )
        )
    )

    // ========== EUROPEAN LANGUAGES ==========

    // ✅ Italian
    val ITALIAN = Language(
        code = "it_IT",
        name = "Italian",
        nativeName = "Italiano",
        flagIcon = R.drawable.flag_it,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"), KeyData("w", "2"), KeyData("e", "3", listOf("è", "é", "ê")),
                    KeyData("r", "4"), KeyData("t", "5"), KeyData("y", "6"),
                    KeyData("u", "7", listOf("ù", "ú", "û")),
                    KeyData("i", "8", listOf("ì", "í", "î")),
                    KeyData("o", "9", listOf("ò", "ó", "ô")),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@", listOf("à", "á", "â")),
                    KeyData("s", "#"), KeyData("d", "$"), KeyData("f", "%"),
                    KeyData("g", "^"), KeyData("h", "&"), KeyData("j", "*"),
                    KeyData("k", "("), KeyData("l", ")")
                ),
                listOf(
                    KeyData("z", "+"), KeyData("x", "="), KeyData("c", "_", listOf("ç")),
                    KeyData("v", "-"), KeyData("b", "/"), KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("à", "á", "â"),
            "e" to listOf("è", "é", "ê"),
            "i" to listOf("ì", "í", "î"),
            "o" to listOf("ò", "ó", "ô"),
            "u" to listOf("ù", "ú", "û")
        )
    )

    // ✅ Portuguese
    val PORTUGUESE = Language(
        code = "pt_BR",
        name = "Portuguese",
        nativeName = "Português",
        flagIcon = R.drawable.flag_br,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"), KeyData("w", "2"), KeyData("e", "3", listOf("é", "ê", "è")),
                    KeyData("r", "4"), KeyData("t", "5"), KeyData("y", "6"),
                    KeyData("u", "7", listOf("ú", "ü", "ù")),
                    KeyData("i", "8", listOf("í", "ì")),
                    KeyData("o", "9", listOf("ó", "ô", "õ", "ò")),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@", listOf("á", "à", "â", "ã")),
                    KeyData("s", "#"), KeyData("d", "$"), KeyData("f", "%"),
                    KeyData("g", "^"), KeyData("h", "&"), KeyData("j", "*"),
                    KeyData("k", "("), KeyData("l", ")")
                ),
                listOf(
                    KeyData("z", "+"), KeyData("x", "="), KeyData("c", "_", listOf("ç")),
                    KeyData("v", "-"), KeyData("b", "/"), KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("á", "à", "â", "ã"),
            "e" to listOf("é", "ê", "è"),
            "i" to listOf("í", "ì"),
            "o" to listOf("ó", "ô", "õ", "ò"),
            "u" to listOf("ú", "ü", "ù"),
            "c" to listOf("ç")
        )
    )

    // ✅ Dutch
    val DUTCH = Language(
        code = "nl_NL",
        name = "Dutch",
        nativeName = "Nederlands",
        flagIcon = R.drawable.flag_nl,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"), KeyData("w", "2"), KeyData("e", "3", listOf("é", "è", "ê", "ë")),
                    KeyData("r", "4"), KeyData("t", "5"), KeyData("y", "6"),
                    KeyData("u", "7", listOf("ú", "ü", "ù")),
                    KeyData("i", "8", listOf("í", "ï")),
                    KeyData("o", "9", listOf("ó", "ö", "ô")),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@", listOf("á", "ä", "à")),
                    KeyData("s", "#"), KeyData("d", "$"), KeyData("f", "%"),
                    KeyData("g", "^"), KeyData("h", "&"), KeyData("j", "*"),
                    KeyData("k", "("), KeyData("l", ")")
                ),
                listOf(
                    KeyData("z", "+"), KeyData("x", "="), KeyData("c", "_"),
                    KeyData("v", "-"), KeyData("b", "/"), KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("á", "ä", "à"),
            "e" to listOf("é", "è", "ê", "ë"),
            "i" to listOf("í", "ï"),
            "o" to listOf("ó", "ö", "ô"),
            "u" to listOf("ú", "ü", "ù")
        )
    )

    // ✅ Polish
    val POLISH = Language(
        code = "pl_PL",
        name = "Polish",
        nativeName = "Polski",
        flagIcon = R.drawable.flag_pl,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"), KeyData("w", "2"), KeyData("e", "3", listOf("ę", "é")),
                    KeyData("r", "4"), KeyData("t", "5"), KeyData("y", "6"),
                    KeyData("u", "7"), KeyData("i", "8"), KeyData("o", "9", listOf("ó")),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@", listOf("ą")), KeyData("s", "#", listOf("ś")),
                    KeyData("d", "$"), KeyData("f", "%"), KeyData("g", "^"),
                    KeyData("h", "&"), KeyData("j", "*"), KeyData("k", "("),
                    KeyData("l", ")", listOf("ł"))
                ),
                listOf(
                    KeyData("z", "+", listOf("ź", "ż")), KeyData("x", "="),
                    KeyData("c", "_", listOf("ć")), KeyData("v", "-"),
                    KeyData("b", "/"), KeyData("n", "!", listOf("ń")),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("ą"),
            "c" to listOf("ć"),
            "e" to listOf("ę"),
            "l" to listOf("ł"),
            "n" to listOf("ń"),
            "o" to listOf("ó"),
            "s" to listOf("ś"),
            "z" to listOf("ź", "ż")
        )
    )

    // ✅ Turkish
    val TURKISH = Language(
        code = "tr_TR",
        name = "Turkish",
        nativeName = "Türkçe",
        flagIcon = R.drawable.flag_tr,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"), KeyData("w", "2"), KeyData("e", "3"),
                    KeyData("r", "4"), KeyData("t", "5"), KeyData("y", "6"),
                    KeyData("u", "7", listOf("ü", "û")), KeyData("ı", "8", listOf("i", "î")),
                    KeyData("o", "9", listOf("ö", "ô")), KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@", listOf("â")), KeyData("s", "#", listOf("ş")),
                    KeyData("d", "$"), KeyData("f", "%"), KeyData("g", "^", listOf("ğ")),
                    KeyData("h", "&"), KeyData("j", "*"), KeyData("k", "("),
                    KeyData("l", ")")
                ),
                listOf(
                    KeyData("z", "+"), KeyData("x", "="), KeyData("c", "_", listOf("ç")),
                    KeyData("v", "-"), KeyData("b", "/"), KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("â"),
            "c" to listOf("ç"),
            "g" to listOf("ğ"),
            "i" to listOf("ı", "î"),
            "o" to listOf("ö", "ô"),
            "s" to listOf("ş"),
            "u" to listOf("ü", "û")
        )
    )

    // ✅ Swedish
    val SWEDISH = Language(
        code = "sv_SE",
        name = "Swedish",
        nativeName = "Svenska",
        flagIcon = R.drawable.flag_se,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"), KeyData("w", "2"), KeyData("e", "3"),
                    KeyData("r", "4"), KeyData("t", "5"), KeyData("y", "6"),
                    KeyData("u", "7"), KeyData("i", "8"), KeyData("o", "9"),
                    KeyData("p", "0", listOf("å"))
                ),
                listOf(
                    KeyData("a", "@", listOf("ä")), KeyData("s", "#"),
                    KeyData("d", "$"), KeyData("f", "%"), KeyData("g", "^"),
                    KeyData("h", "&"), KeyData("j", "*"), KeyData("k", "("),
                    KeyData("l", ")", listOf("ö"))
                ),
                listOf(
                    KeyData("z", "+"), KeyData("x", "="), KeyData("c", "_"),
                    KeyData("v", "-"), KeyData("b", "/"), KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        ),
        accentMap = mapOf(
            "a" to listOf("ä", "å"),
            "o" to listOf("ö")
        )
    )

    // ✅ Indonesian
    val INDONESIAN = Language(
        code = "id_ID",
        name = "Indonesian",
        nativeName = "Bahasa Indonesia",
        flagIcon = R.drawable.flag_id,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("q", "1"), KeyData("w", "2"), KeyData("e", "3"),
                    KeyData("r", "4"), KeyData("t", "5"), KeyData("y", "6"),
                    KeyData("u", "7"), KeyData("i", "8"), KeyData("o", "9"),
                    KeyData("p", "0")
                ),
                listOf(
                    KeyData("a", "@"), KeyData("s", "#"), KeyData("d", "$"),
                    KeyData("f", "%"), KeyData("g", "^"), KeyData("h", "&"),
                    KeyData("j", "*"), KeyData("k", "("), KeyData("l", ")")
                ),
                listOf(
                    KeyData("z", "+"), KeyData("x", "="), KeyData("c", "_"),
                    KeyData("v", "-"), KeyData("b", "/"), KeyData("n", "!"),
                    KeyData("m", "?")
                )
            )
        )
    )

    // ========== MIDDLE EASTERN LANGUAGES ==========

    // ✅ Persian (Farsi)
    val PERSIAN = Language(
        code = "fa_IR",
        name = "Persian",
        nativeName = "فارسی",
        flagIcon = R.drawable.flag_ir,
        isRTL = true,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ض", "۱"), KeyData("ص", "۲"), KeyData("ث", "۳"),
                    KeyData("ق", "۴"), KeyData("ف", "۵"), KeyData("غ", "۶"),
                    KeyData("ع", "۷"), KeyData("ه", "۸"), KeyData("خ", "۹"),
                    KeyData("ح", "۰")
                ),
                listOf(
                    KeyData("ش", "!"), KeyData("س", "@"), KeyData("ی", "#"),
                    KeyData("ب", "$"), KeyData("ل", "%"), KeyData("ا", "^"),
                    KeyData("ت", "&"), KeyData("ن", "*"), KeyData("م", "("),
                    KeyData("ک", ")")
                ),
                listOf(
                    KeyData("ظ", "+"), KeyData("ط", "="), KeyData("ز", "_"),
                    KeyData("ر", "-"), KeyData("ذ", "/"), KeyData("د", "\\"),
                    KeyData("پ", "?"), KeyData("و", "!"), KeyData("چ", ","),
                    KeyData("ژ", ".")
                )
            )
        )
    )

    // ✅ Hebrew
    val HEBREW = Language(
        code = "he_IL",
        name = "Hebrew",
        nativeName = "עברית",
        flagIcon = R.drawable.flag_il,
        isRTL = true,
        layout = LanguageLayout(
            rows = listOf(
                listOf(
                    KeyData("ק", "1"), KeyData("ר", "2"), KeyData("א", "3"),
                    KeyData("ט", "4"), KeyData("ו", "5"), KeyData("ן", "6"),
                    KeyData("ם", "7"), KeyData("פ", "8"), KeyData("ש", "9"),
                    KeyData("ד", "0")
                ),
                listOf(
                    KeyData("ף", "!"), KeyData("ך", "@"), KeyData("ל", "#"),
                    KeyData("ח", "$"), KeyData("י", "%"), KeyData("ע", "^"),
                    KeyData("כ", "&"), KeyData("ג", "*"), KeyData("נ", "("),
                    KeyData("מ", ")")
                ),
                listOf(
                    KeyData("ץ", "+"), KeyData("ת", "="), KeyData("צ", "_"),
                    KeyData("מ", "-"), KeyData("נ", "/"), KeyData("ה", "\\"),
                    KeyData("ב", "?"), KeyData("ס", "!"), KeyData("ז", ",")
                )
            )
        )
    )

    // Get all 30+ languages
    fun getAllLanguages(): List<Language> {
        return listOf(
            // English & Major European
            Languages.ENGLISH_US,
            Languages.SPANISH,
            Languages.FRENCH,
            Languages.GERMAN,
            ITALIAN,
            PORTUGUESE,
            DUTCH,
            POLISH,
            TURKISH,
            SWEDISH,
            Languages.RUSSIAN,

            // Indian Languages
            HINDI,
            BENGALI,
            TAMIL,
            TELUGU,
            MALAYALAM,
            KANNADA,
            MARATHI,
            GUJARATI,
            PUNJABI,
            ODIA,
            ASSAMESE,
            URDU,

            // Asian Languages
            Languages.CHINESE_SIMPLIFIED,
            Languages.JAPANESE,
            KOREAN,
            VIETNAMESE,
            THAI,
            INDONESIAN,

            // Middle Eastern
            Languages.ARABIC,
            PERSIAN,
            HEBREW
        )
    }

    fun getLanguageByCode(code: String): Language {
        return getAllLanguages().find { it.code == code } ?: Languages.ENGLISH_US
    }

    fun getIndianLanguages(): List<Language> {
        return listOf(
            HINDI, BENGALI, TAMIL, TELUGU, MALAYALAM, KANNADA,
            MARATHI, GUJARATI, PUNJABI, ODIA, ASSAMESE, URDU
        )
    }
}