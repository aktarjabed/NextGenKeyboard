package com.nextgen.keyboard.data.model

/**
 * Defines the complete keyboard layout for a language
 * @param rows List of rows, where each row is a list of KeyData objects
 */
data class LanguageLayout(
    val rows: List<List<KeyData>>
)