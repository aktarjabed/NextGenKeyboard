package com.aktarjabed.nxtgenkeyboard.lang

/**
 * STOPGAP TRANSLITERATOR
 * This is a simplistic heuristic state machine combined with a hardcoded dictionary
 * bypass for common words. It is not a linguistically complete Indic text engine.
 * For long-term production, this should be replaced with a robust trie-based
 * dictionary or a lightweight local NLP model to handle complex conjunct consonants
 * dynamically.
 */
object PhoneticTransliterator {

    // Language-scoped special word dictionary to ensure common words are correct.
    private val specialWords = mapOf(
        "hi" to mapOf(
            "namaste" to "नमस्ते",
            "bharat" to "भारत",
            "mera" to "मेरा",
            "aap" to "आप",
            "kya" to "क्या",
            "hai" to "है",
            "hain" to "हैं",
            "mein" to "में"
        ),
        "bn" to mapOf(
            "bangla" to "বাংলা",
            "namaste" to "নমস্তে",
            "ami" to "আমি",
            "tumi" to "তুমি",
            "ebong" to "এবং",
            "kor" to "কর",
            "kora" to "করা",
            "nei" to "নেই",
            "ache" to "আছে"
        )
    )

    private data class Config(
        val consonants: List<Pair<String, String>>,
        val vowels: List<Pair<String, String>>,
        val independentVowels: Map<String, String>,
        val virama: String,
        val anusvara: String
    )

    private val HI = Config(
        consonants = listOf(
            "ksh" to "क्ष", "gy" to "ज्ञ", "bh" to "भ", "chh" to "छ",
            "ch" to "च", "dh" to "ध", "gh" to "घ", "jh" to "झ",
            "kh" to "ख", "ph" to "फ", "sh" to "श", "th" to "थ",
            "dr" to "द्र", "kr" to "क्र", "gr" to "ग्र", "pr" to "प्र",
            "br" to "ब्र", "mr" to "म्र", "tr" to "त्र",
            "r" to "र", "l" to "ल", "v" to "व", "w" to "व",
            "y" to "य", "h" to "ह", "m" to "म", "n" to "न",
            "t" to "त", "d" to "द", "p" to "प", "b" to "ब",
            "s" to "स", "g" to "ग", "j" to "ज", "z" to "ज़",
            "f" to "फ़", "q" to "क़", "c" to "क", "k" to "क"
        ),
        vowels = listOf(
            "ai" to "ै", "au" to "ौ", "aa" to "ा", "ee" to "ी",
            "ii" to "ी", "oo" to "ू", "uu" to "ू", "ri" to "ृ",
            "e" to "े", "i" to "ि", "o" to "ो", "u" to "ु",
            "a" to ""  // inherent vowel
        ),
        independentVowels = mapOf(
            "aa" to "आ", "ee" to "ई", "ii" to "ई", "oo" to "ऊ",
            "uu" to "ऊ", "ai" to "ऐ", "au" to "औ", "ri" to "ऋ",
            "e" to "ए", "i" to "इ", "o" to "ओ", "u" to "उ",
            "a" to "अ"
        ),
        virama = "्",
        anusvara = "ं"
    )

    private val BN = Config(
        consonants = listOf(
            "ksh" to "ক্ষ", "gy" to "জ্ঞ", "bh" to "ভ", "chh" to "ছ",
            "ch" to "চ", "dh" to "ধ", "gh" to "ঘ", "jh" to "ঝ",
            "kh" to "খ", "ph" to "ফ", "sh" to "শ", "th" to "থ",
            "dr" to "দ্র", "kr" to "ক্র", "gr" to "গ্র", "pr" to "প্র",
            "br" to "ব্র", "mr" to "ম্র", "tr" to "ত্র",
            "r" to "র", "l" to "ল", "v" to "ভ", "w" to "ও",
            "y" to "য", "h" to "হ", "m" to "ম", "n" to "ন",
            "t" to "ত", "d" to "দ", "p" to "প", "b" to "ব",
            "s" to "স", "g" to "গ", "j" to "জ", "z" to "জ",
            "f" to "ফ", "q" to "ক", "c" to "ক", "k" to "ক"
        ),
        vowels = listOf(
            "ai" to "ৈ", "au" to "ৌ", "aa" to "া", "ee" to "ী",
            "ii" to "ী", "oo" to "ূ", "uu" to "ূ", "e" to "ে",
            "i" to "ি", "o" to "ো", "u" to "ু", "a" to ""
        ),
        independentVowels = mapOf(
            "aa" to "আ", "ee" to "ঈ", "ii" to "ঈ", "oo" to "ঊ",
            "uu" to "ঊ", "ai" to "ঐ", "au" to "ঔ", "e" to "এ",
            "i" to "ই", "o" to "ও", "u" to "উ", "a" to "আ"
        ),
        virama = "्",
        anusvara = "ং"
    )

    fun transliterate(input: String, lang: String): String {
        if (input.isEmpty()) return input

        val lower = input.lowercase()
        specialWords[lang]?.get(lower)?.let { return it }

        val config = when (lang) {
            "hi" -> HI
            "bn" -> BN
            else -> return input
        }

        val output = StringBuilder()
        val word = StringBuilder()

        fun flush() {
            if (word.isNotEmpty()) {
                output.append(transliterateWord(word.toString(), config))
                word.clear()
            }
        }

        input.forEach { ch ->
            if (ch.isLetter()) word.append(ch)
            else {
                flush()
                output.append(ch)
            }
        }
        flush()
        return output.toString()
    }

    private fun transliterateWord(word: String, config: Config): String {
        val source = word.lowercase()
        val output = StringBuilder()
        var index = 0
        var previousWasConsonant = false

        while (index < source.length) {
            if (source.startsWith("ng", index)) {
                output.append(config.anusvara)
                index += 2
                previousWasConsonant = false
                continue
            }

            val consonant = config.consonants
                .filter { source.startsWith(it.first, index) }
                .maxByOrNull { it.first.length }

            val vowel = config.vowels
                .filter { source.startsWith(it.first, index) }
                .maxByOrNull { it.first.length }

            if (vowel != null && (consonant == null || vowel.first.length > consonant.first.length)) {
                val (roman, mark) = vowel
                if (!previousWasConsonant) {
                    output.append(config.independentVowels[roman] ?: roman)
                } else {
                    if (roman == "a") {
                        // No mark for inherent 'a' in Hindi/Bengali, unless it is a special case (we just don't append mark)
                        if (mark.isNotEmpty()) output.append(mark)
                    } else {
                        output.append(mark)
                    }
                }
                index += roman.length
                previousWasConsonant = false
                continue
            }

            if (consonant != null) {
                val (roman, script) = consonant
                if (previousWasConsonant) {
                    output.append(config.virama)
                }
                output.append(script)
                index += roman.length
                previousWasConsonant = true
                continue
            }

            output.append(source[index])
            index++
            previousWasConsonant = false
        }

        return output.toString()
    }
}