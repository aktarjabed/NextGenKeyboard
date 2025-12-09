package com.aktarjabed.nextgenkeyboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Layouts(
    val layouts: List<LanguageLayout>
) {
    companion object {
        fun getDefault(): Layouts = Layouts(
            layouts = listOf(
                LanguageKeyboardDatabase.getLayout("en").toLanguageLayout()
            )
        )
    }
}
