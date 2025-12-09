package com.aktarjabed.nextgenkeyboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LanguageLayout(
    val languageCode: String,
    val layoutName: String,
    val rows: List<KeyRow>,
    val isDefault: Boolean = false
)

@Serializable
data class KeyRow(
    val keys: List<KeyData>
)
