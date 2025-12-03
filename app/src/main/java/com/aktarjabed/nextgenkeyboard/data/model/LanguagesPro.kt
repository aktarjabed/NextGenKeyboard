package com.aktarjabed.nextgenkeyboard.data.model

import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagesPro @Inject constructor() {
    companion object {
        fun getAllLanguages(): List<Language> = Languages.all

        fun getLanguageByCode(code: String): Language {
            return Languages.all.find { it.code.equals(code, ignoreCase = true) }
                ?: Languages.ENGLISH_US.also {
                    Timber.w("Language $code not found, defaulting to English (US)")
                }
        }

        fun getLanguageByLocale(locale: Locale): Language {
            val code = "${locale.language}_${locale.country}"
            return getLanguageByCode(code)
        }

        fun isRTLLanguage(code: String): Boolean {
            return getLanguageByCode(code).isRTL
        }

        fun getAccentMap(languageCode: String): Map<String, List<String>> {
            return getLanguageByCode(languageCode).accentMap
        }
    }
}