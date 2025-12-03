package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aktarjabed.nextgenkeyboard.data.model.Language

@Composable
fun MainKeyboardView(
    language: Language,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    onKeyClick: (String) -> Unit,
    onVoiceInputClick: () -> Unit,
    onGifKeyboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Top action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
            IconButton(onClick = onVoiceInputClick) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Input")
            }
            IconButton(onClick = onGifKeyboardClick) {
                Icon(Icons.Default.Gif, contentDescription = "GIF Keyboard")
            }
        }

        // Suggestion bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(suggestions) { suggestion ->
                Button(onClick = { onSuggestionClick(suggestion) }) {
                    Text(suggestion)
                }
            }
        }

        // Main keyboard layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            language.layout.rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    row.forEach { keyData ->
                        Key(
                            char = keyData.display,
                            onClick = onKeyClick
                        )
                    }
                }
            }
        }
    }
}