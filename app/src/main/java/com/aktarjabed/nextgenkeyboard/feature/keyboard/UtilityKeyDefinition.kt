package com.aktarjabed.nextgenkeyboard.feature.keyboard

enum class UtilityKeyAction {
    COPY_EMAIL,          // Copy predefined email
    COPY_PHONE,          // Copy predefined phone
    COPY_ADDRESS,        // Copy predefined address
    PASTE_CLIPBOARD,     // One-tap paste (no double-press)
    PASTE_LAST_5,        // Show last 5 clipboard items (triggers clipboard strip)
    UNDO_LAST_DELETE,    // Quick undo
    INSERT_DATE,         // Insert today's date
    INSERT_TIME,         // Insert current time
    SELECT_ALL,          // Select all text
    COPY,                // Copy selected text
    CUT                  // Cut selected text
}

data class UtilityKey(
    val id: String,
    val label: String,
    val action: UtilityKeyAction,
    val icon: String? = null // Placeholder for icon resource name if needed
)
