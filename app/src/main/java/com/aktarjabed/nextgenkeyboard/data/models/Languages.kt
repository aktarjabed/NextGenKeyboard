package com.aktarjabed.nextgenkeyboard.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Languages(
    val languages: List<Language>
) {
    companion object {
        fun getDefault(): Languages = Languages(
            languages = Language.SUPPORTED_LANGUAGES
        )
    }
}