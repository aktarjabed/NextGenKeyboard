package com.aktarjabed.nextgenkeyboard.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val isSupported: Boolean = true,
    val layouts: List<LanguageLayout> = emptyList()
) {
    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            Language("en", "English", "English"),
            Language("hi", "Hindi", "हिंदी"),
            Language("bn", "Bengali", "বাংলা")
        )
    }
}