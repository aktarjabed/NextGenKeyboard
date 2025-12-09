package com.aktarjabed.nextgenkeyboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val isSupported: Boolean = true,
    val isRTL: Boolean = false,
    val layouts: List<LanguageLayout> = emptyList(),
    val flagIcon: String = "" // Added to support flag icons
) {
    val layout: LanguageLayout
        get() = layouts.find { it.isDefault } ?: layouts.firstOrNull() ?: LanguageKeyboardDatabase.getLayout("en").toLanguageLayout()

    companion object {
        // Use LanguageKeyboardDatabase to get all layouts dynamically
        val SUPPORTED_LANGUAGES: List<Language> by lazy {
            val database = LanguageKeyboardDatabase
            val codes = listOf(
                "en", "es", "fr", "de", "it", "pt", "nl", "pl", "tr", "vi", "id", "ms", "tl", "cs", "hu", "ro", "sv", "da", "no", "fi",
                "ru", "uk", "bg",
                "el",
                "he",
                "ar", "fa", "ur",
                "hi", "bn", "ta", "te", "kn", "ml", "gu", "mr", "pa",
                "th"
            )

            codes.map { code ->
                val layoutData = database.getLayout(code)
                Language(
                    code = layoutData.languageCode,
                    name = layoutData.languageName,
                    nativeName = layoutData.nativeName,
                    isRTL = layoutData.scriptType == ScriptType.ARABIC || layoutData.scriptType == ScriptType.HEBREW,
                    layouts = listOf(layoutData.toLanguageLayout())
                )
            }
        }
    }
}
