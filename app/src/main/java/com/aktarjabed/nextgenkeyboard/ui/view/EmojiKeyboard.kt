package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.aktarjabed.nextgenkeyboard.data.model.EmojiData
import com.aktarjabed.nextgenkeyboard.ui.common.GridContentKeyboard
import com.aktarjabed.nextgenkeyboard.ui.viewmodel.KeyboardViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

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

    // Robust category generation with try-catch
    val emojiCategories = remember(recentEmojis) {
        try {
            // Dynamic map merging Recent with static data
            mapOf("Recent" to recentEmojis) + EmojiData.categories.filterKeys { it != "Recent" }
        } catch (e: Exception) {
            Timber.e(e, "Error loading emoji categories")
            EmojiData.categories
        }
    }

    // Default to 'Smilies' (index 1) if 'Recent' is present, else 0
    var selectedCategoryIndex by remember { mutableStateOf(if (emojiCategories.containsKey("Recent")) 1 else 0) }
    val categories = emojiCategories.keys.toList()

    // Safety check for index out of bounds
    if (selectedCategoryIndex >= categories.size) {
        selectedCategoryIndex = 0
    }

    GridContentKeyboard(
        modifier = modifier,
        topBar = {
            if (categories.isNotEmpty()) {
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
            }
        },
        content = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val selectedCategory = categories.getOrNull(selectedCategoryIndex)
                val emojis = selectedCategory?.let { emojiCategories[it] } ?: emptyList()

                items(emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                try {
                                    onEmojiSelected(emoji)
                                    coroutineScope.launch {
                                        viewModel.trackEmojiUsage(emoji)
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e, "Error selecting emoji: $emoji")
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
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
    )
}
