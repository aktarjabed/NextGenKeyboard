package com.aktarjabed.nextgenkeyboard.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Layouts(
    val layouts: List<LanguageLayout>
) {
    companion object {
        fun getDefault(): Layouts = Layouts(
            layouts = listOf(
                LanguageLayout.getHindiLayout(),
                LanguageLayout.getBengaliLayout()
            )
        )
    }
}