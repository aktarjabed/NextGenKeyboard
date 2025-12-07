package com.aktarjabed.nextgenkeyboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LanguageLayout(
    val languageCode: String,
    val layoutName: String,
    val rows: List<KeyRow>,
    val isDefault: Boolean = false
) {
    companion object {
        fun getEnglishLayout(): LanguageLayout {
            val row1 = KeyRow(listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map { KeyData(it.first().code, it) })
            val row2 = KeyRow(listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map { KeyData(it.first().code, it) })
            val row3 = KeyRow(listOf("z", "x", "c", "v", "b", "n", "m").map { KeyData(it.first().code, it) })
            return LanguageLayout("en", "English QWERTY", listOf(row1, row2, row3), true)
        }

        fun getHindiLayout(): LanguageLayout = LanguageLayout(
            languageCode = "hi",
            layoutName = "Hindi QWERTY",
            rows = emptyList(),
            isDefault = true
        )

        fun getBengaliLayout(): LanguageLayout = LanguageLayout(
            languageCode = "bn",
            layoutName = "Bengali QWERTY",
            rows = emptyList(),
            isDefault = true
        )
    }
}

@Serializable
data class KeyRow(
    val keys: List<KeyData>
)
