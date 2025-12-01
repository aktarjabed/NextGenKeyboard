package com.nextgen.keyboard.data.model

/**
 * Represents a single key on the keyboard
 * @param primary Main character/text displayed on the key
 * @param secondary Optional secondary character (e.g., long-press symbol)
 * @param accents Optional list of accent characters for long-press menu
 */
data class KeyData(
    val primary: String,
    val secondary: String? = null,
    val accents: List<String>? = null
)