package com.aktarjabed.nextgenkeyboard.data.model

import kotlinx.serialization.Serializable
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagesPro @Inject constructor() {
    companion object {
        fun getAllLanguages(): List<Language> = Language.SUPPORTED_LANGUAGES

        fun getLanguageByCode(code: String): Language {
            return Language.SUPPORTED_LANGUAGES.find { it.code.equals(code, ignoreCase = true) }
                ?: Language.SUPPORTED_LANGUAGES.first()
        }

        fun getLanguageByLocale(locale: Locale): Language {
            val code = "${locale.language}_${locale.country}"
            return getLanguageByCode(code)
        }

        fun isRTLLanguage(code: String): Boolean {
            // Simple check, can be expanded
            return code.startsWith("ar") || code.startsWith("he") || code.startsWith("fa")
        }
    }
}