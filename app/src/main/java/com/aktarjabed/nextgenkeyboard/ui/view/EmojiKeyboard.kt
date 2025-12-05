package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun EmojiKeyboard(
    onEmojiSelected: (String) -> Unit
) {
    // Placeholder for Phase 2 Emoji implementation
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Emoji Picker Coming Soon")
    }
}
