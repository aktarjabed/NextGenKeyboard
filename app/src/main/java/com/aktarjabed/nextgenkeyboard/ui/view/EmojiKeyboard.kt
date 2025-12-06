package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aktarjabed.nextgenkeyboard.ui.viewmodel.KeyboardViewModel
import kotlinx.coroutines.launch

@Composable
fun EmojiKeyboard(
    viewModel: KeyboardViewModel,
    onEmojiSelected: (String) -> Unit,
    onBackspace: () -> Unit,
    onBackToAlphabet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentEmojis by viewModel.recentEmojis.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    val emojiCategories = remember(recentEmojis) {
        mapOf(
            "🕐" to recentEmojis,
            "😀" to listOf("😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "😉", "😊"),
            "❤️" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕"),
            "👋" to listOf("👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞", "🤟", "🤘"),
            "🐻" to listOf("🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮"),
            "🍔" to listOf("🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒"),
            "⚽" to listOf("⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🎱", "🏓", "🏸", "🏒"),
            "🚗" to listOf("🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐", "🛻", "🚚"),
            "💡" to listOf("⌚", "📱", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "🖲️", "🕹️", "🗜️", "💽", "💾")
        )
    }

    var selectedCategoryIndex by remember { mutableStateOf(1) } // Default to smileys
    val categories = emojiCategories.keys.toList()

    Column(modifier = modifier.fillMaxSize()) {
        // Category tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            edgePadding = 0.dp
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = { Text(text = category) }
                )
            }
        }

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            val selectedCategory = categories[selectedCategoryIndex]
            val emojis = emojiCategories[selectedCategory] ?: emptyList()

            items(emojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            onEmojiSelected(emoji)
                            coroutineScope.launch {
                                viewModel.trackEmojiUsage(emoji)
                            }
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom bar with backspace and ABC button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackToAlphabet) {
                Icon(Icons.Default.Keyboard, contentDescription = "Back to Alphabet")
            }
            IconButton(onClick = onBackspace) {
                Icon(Icons.Default.Backspace, contentDescription = "Backspace")
            }
        }
    }
}
