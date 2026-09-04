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
}