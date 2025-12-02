package com.nextgen.keyboard.data.models

import kotlinx.serialization.Serializable

@Serializable
data class KeyData(
    val keyCode: Int,
    val label: String,
    val displayLabel: String? = null,
    val keySymbol: String? = null,
    val popupCharacters: List<String> = emptyList(),
    val keyOutputText: String? = null,
    val type: KeyType = KeyType.NORMAL,
    val width: Int = 1,
    val height: Int = 1
) {
    enum class KeyType {
        NORMAL, MODIFIER, FUNCTION, SPACER
    }
}