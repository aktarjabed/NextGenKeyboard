package com.nextgen.keyboard.utils

object Constants {
    // Database
    const val DATABASE_NAME = "nextgen_keyboard_db"
    const val DATABASE_VERSION = 1

    // Clipboard
    const val MAX_CLIPBOARD_ITEMS = 50
    const val MAX_CLIP_LENGTH = 1000

    // Preferences
    const val PREFS_NAME = "keyboard_preferences"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_SELECTED_LAYOUT = "selected_layout"
    const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
    const val KEY_SWIPE_TYPING = "swipe_typing"

    // Keyboard
    const val DEFAULT_LAYOUT = "QWERTY"
    const val KEY_HEIGHT_DP = 48
    const val KEY_MARGIN_DP = 2

    // Swipe
    const val SWIPE_MIN_DISTANCE = 20f
    const val SWIPE_PREDICTION_DELAY = 100L

    // Security
    val SENSITIVE_PACKAGES = setOf(
        "bank", "wallet", "payment", "password", "authenticator",
        "keepass", "bitwarden", "lastpass", "1password", "dashlane",
        "crypto", "coinbase", "blockchain", "binance"
    )

    // Logging
    const val LOG_TAG = "NextGenKeyboard"
}