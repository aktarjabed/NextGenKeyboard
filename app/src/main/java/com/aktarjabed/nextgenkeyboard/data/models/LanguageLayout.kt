package com.aktarjabed.nextgenkeyboard.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LanguageLayout(
    val languageCode: String,
    val layoutName: String,
    val rows: List<KeyRow>,
    val isDefault: Boolean = false
) {
    companion object {
        fun getHindiLayout(): LanguageLayout = LanguageLayout(
            languageCode = "hi",
            layoutName = "Hindi QWERTY",
            rows = emptyList(), // Populate with actual Hindi keys
            isDefault = true
        )

        fun getBengaliLayout(): LanguageLayout = LanguageLayout(
            languageCode = "bn",
            layoutName = "Bengali QWERTY",
            rows = emptyList(), // Populate with actual Bengali keys
            isDefault = true
        )
    }
}

@Serializable
data class KeyRow(
    val keys: List<KeyData>
)