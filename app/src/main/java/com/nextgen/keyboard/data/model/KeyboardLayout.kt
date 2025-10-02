package com.nextgen.keyboard.data.model

sealed class KeyboardLayout(val name: String, val rows: List<List<String>>) {

    object Qwerty : KeyboardLayout(
        name = "QWERTY",
        rows = listOf(
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf("Z", "X", "C", "V", "B", "N", "M"),
            listOf("📋", "SPACE", "⌫", "↵")
        )
    )

    object Dvorak : KeyboardLayout(
        name = "Dvorak",
        rows = listOf(
            listOf("'", ",", ".", "P", "Y", "F", "G", "C", "R", "L"),
            listOf("A", "O", "E", "U", "I", "D", "H", "T", "N", "S"),
            listOf(";", "Q", "J", "K", "X", "B", "M", "W", "V", "Z"),
            listOf("📋", "SPACE", "⌫", "↵")
        )
    )

    object Colemak : KeyboardLayout(
        name = "Colemak",
        rows = listOf(
            listOf("Q", "W", "F", "P", "G", "J", "L", "U", "Y", ";"),
            listOf("A", "R", "S", "T", "D", "H", "N", "E", "I", "O"),
            listOf("Z", "X", "C", "V", "B", "K", "M"),
            listOf("📋", "SPACE", "⌫", "↵")
        )
    )

    object Numeric : KeyboardLayout(
        name = "Numeric",
        rows = listOf(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            listOf("@", "#", "$", "%", "^", "&", "*", "(", ")"),
            listOf("+", "-", "/", "=", ".", ",", "_"),
            listOf("📋", "SPACE", "⌫", "↵")
        )
    )

    object Symbol : KeyboardLayout(
        name = "Symbol",
        rows = listOf(
            listOf("`", "~", "@", "#", "$", "%", "^", "&", "*", "|"),
            listOf("(", ")", "{", "}", "[", "]", "<", ">", "/", "\\"),
            listOf("!", "?", "=", "+", "-", "_", ":", ";", "'", "\""),
            listOf("📋", "SPACE", "⌫", "↵")
        )
    )

    object Emoji : KeyboardLayout(
        name = "Emoji",
        rows = listOf(
            listOf("😊", "😂", "😍", "😎", "😢", "😡", "🤔", "😱", "🥰"),
            listOf("👍", "👎", "👏", "🙌", "🤝", "💪", "🙏", "✌️", "🤞"),
            listOf("❤️", "💔", "💯", "🔥", "⭐", "✨", "🎉", "🎈", "🎁"),
            listOf("🚀", "💡", "🌈", "🌟", "☀️", "🌙", "⚡", "🌸", "🍀"),
            listOf("📋", "SPACE", "⌫", "↵")
        )
    )

    object Phone : KeyboardLayout(
        name = "Phone",
        rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#"),
            listOf("📋", "SPACE", "⌫", "↵")
        )
    )

    object Gaming : KeyboardLayout(
        name = "Gaming",
        rows = listOf(
            listOf("W", "↑", "1", "2", "3", "4"),
            listOf("A", "S", "D", "←", "↓", "→"),
            listOf("Q", "E", "R", "F", "G", "H"),
            listOf("SPACE", "⌫", "↵")
        )
    )

    object Minimal : KeyboardLayout(
        name = "Minimal",
        rows = listOf(
            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
            listOf("Z", "X", "C", "V", "B", "N", "M"),
            listOf("SPACE", "⌫", "↵")
        )
    )

    object Accessible : KeyboardLayout(
        name = "Accessible",
        rows = listOf(
            listOf("A", "B", "C", "D", "E"),
            listOf("F", "G", "H", "I", "J"),
            listOf("K", "L", "M", "N", "O"),
            listOf("P", "Q", "R", "S", "T"),
            listOf("U", "V", "W", "X", "Y"),
            listOf("Z", "⌫", "SPACE", "↵")
        )
    )

    companion object {
        fun getLayoutByName(name: String): KeyboardLayout {
            return when (name) {
                "Dvorak" -> Dvorak
                "Colemak" -> Colemak
                "Numeric" -> Numeric
                "Symbol" -> Symbol
                "Emoji" -> Emoji
                "Phone" -> Phone
                "Gaming" -> Gaming
                "Minimal" -> Minimal
                "Accessible" -> Accessible
                else -> Qwerty
            }
        }

        fun getAllLayouts(): List<KeyboardLayout> {
            return listOf(
                Qwerty, Dvorak, Colemak, Numeric, Symbol,
                Emoji, Phone, Gaming, Minimal, Accessible
            )
        }
    }
}