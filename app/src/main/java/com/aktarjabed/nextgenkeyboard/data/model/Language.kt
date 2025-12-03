package com.aktarjabed.nextgenkeyboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val isSupported: Boolean = true,
    val isRTL: Boolean = false,
    val layouts: List<LanguageLayout> = emptyList()
) {
    val layout: LanguageLayout
        get() = layouts.find { it.isDefault } ?: layouts.firstOrNull() ?: LanguageLayout.getEnglishLayout()

    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            Language(
                code = "en",
                name = "English",
                nativeName = "English",
                layouts = listOf(LanguageLayout.getEnglishLayout())
            ),
            Language(
                code = "hi",
                name = "Hindi",
                nativeName = "हिंदी",
                layouts = listOf(LanguageLayout.getHindiLayout())
            ),
            Language(
                code = "bn",
                name = "Bengali",
                nativeName = "বাংলা",
                layouts = listOf(LanguageLayout.getBengaliLayout())
            ),
            // Added back commonly expected languages as placeholders to avoid regression
             Language("es", "Spanish", "Español"),
             Language("fr", "French", "Français"),
             Language("de", "German", "Deutsch")
        )
    }
}
