package com.aktarjabed.nxtgenkeyboard.lang

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneticTransliteratorTest {

    @Test
    fun hindiCommonWords() {
        assertEquals("नमस्ते", PhoneticTransliterator.transliterate("namaste", "hi"))
        assertEquals("भारत", PhoneticTransliterator.transliterate("bharat", "hi"))
        assertEquals("मेरा", PhoneticTransliterator.transliterate("mera", "hi"))
        assertEquals("आप", PhoneticTransliterator.transliterate("aap", "hi"))
        assertEquals("क्या", PhoneticTransliterator.transliterate("kya", "hi"))
        assertEquals("है", PhoneticTransliterator.transliterate("hai", "hi"))
    }

    @Test
    fun bengaliCommonWords() {
        assertEquals("বাংলা", PhoneticTransliterator.transliterate("bangla", "bn"))
        assertEquals("আমি", PhoneticTransliterator.transliterate("ami", "bn"))
        assertEquals("আছে", PhoneticTransliterator.transliterate("ache", "bn"))
    }

    @Test
    fun languageScopedSpecialWords() {
        assertEquals("अमि", PhoneticTransliterator.transliterate("ami", "hi"))
        assertEquals("নমস্তে", PhoneticTransliterator.transliterate("namaste", "bn"))
    }

    @Test
    fun explicitVowelSigns() {
        assertEquals("कि", PhoneticTransliterator.transliterate("ki", "hi"))
        assertEquals("की", PhoneticTransliterator.transliterate("kee", "hi"))
        assertEquals("की", PhoneticTransliterator.transliterate("kii", "hi"))
        assertEquals("कु", PhoneticTransliterator.transliterate("ku", "hi"))
        assertEquals("कू", PhoneticTransliterator.transliterate("koo", "hi"))
    }

    @Test
    fun indicClusters() {
        assertEquals("क्ष", PhoneticTransliterator.transliterate("ksh", "hi"))
        assertEquals("श्र", PhoneticTransliterator.transliterate("shr", "hi"))
        assertEquals("ज्ञ", PhoneticTransliterator.transliterate("gy", "hi"))
        assertEquals("त्र", PhoneticTransliterator.transliterate("tr", "hi"))
        assertEquals("क्र", PhoneticTransliterator.transliterate("kr", "hi"))
        assertEquals("प्र", PhoneticTransliterator.transliterate("pr", "hi"))
        assertEquals("ब्र", PhoneticTransliterator.transliterate("br", "hi"))
        assertEquals("द्र", PhoneticTransliterator.transliterate("dr", "hi"))
    }
}