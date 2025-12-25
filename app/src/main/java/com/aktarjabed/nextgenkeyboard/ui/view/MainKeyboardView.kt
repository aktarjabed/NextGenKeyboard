package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.aktarjabed.nextgenkeyboard.data.model.Clip
import com.aktarjabed.nextgenkeyboard.data.model.Language
import com.aktarjabed.nextgenkeyboard.feature.keyboard.UtilityKey
import com.aktarjabed.nextgenkeyboard.feature.keyboard.UtilityKeyAction
import com.aktarjabed.nextgenkeyboard.feature.swipe.SwipePathProcessor
import com.aktarjabed.nextgenkeyboard.feature.swipe.SwipePredictor
import com.aktarjabed.nextgenkeyboard.ui.theme.KeyboardTheme
import com.aktarjabed.nextgenkeyboard.ui.theme.KeyboardThemes
import com.aktarjabed.nextgenkeyboard.ui.gestures.GestureManager

@Composable
fun MainKeyboardView(
    language: Language,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    onKeyClick: (String) -> Unit,
    onVoiceInputClick: () -> Unit,
    onGifKeyboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    swipePredictor: SwipePredictor,
    swipePathProcessor: SwipePathProcessor,
    utilityKeys: List<UtilityKey> = emptyList(),
    gestureManager: GestureManager,
    onUtilityKeyClick: (UtilityKeyAction) -> Unit = {},
    theme: KeyboardTheme = KeyboardThemes.LIGHT, // Injected Theme
    onClipboardClick: () -> Unit = {},
    onEmojiClick: () -> Unit = {},
    recentClips: List<Clip> = emptyList(),
    onClipSelected: (String) -> Unit = {}
) {
    val layoutDirection = if (language.isRTL) LayoutDirection.Rtl else LayoutDirection.Ltr
    var showClipboard by remember { mutableStateOf(false) }

    // State to track the keyboard's global position offset
    var keyboardRootOffset by remember { mutableStateOf(Offset.Zero) }
    var lastTapTime by remember { mutableStateOf(0L) }

    // Swipe Trail State
    var currentSwipePath by remember { mutableStateOf(emptyList<Offset>()) }

    // State for Symbol Mode
    var isSymbolMode by remember { mutableStateOf(false) }

    // Clear stale key positions when the language layout OR symbol mode changes
    DisposableEffect(language, isSymbolMode) {
        swipePathProcessor.clearKeyPositions()
        onDispose {
            swipePathProcessor.clearKeyPositions()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.backgroundColor) // Apply Theme Background
        ) {
            // Top action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Apply theme colors to icons
                val iconTint = theme.keyTextColor.copy(alpha = 0.7f)

                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = iconTint)
                }
                IconButton(onClick = onVoiceInputClick) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = iconTint)
                }
                IconButton(onClick = onGifKeyboardClick) {
                    Icon(Icons.Default.Gif, contentDescription = "GIF Keyboard", tint = iconTint)
                }
                IconButton(onClick = { showClipboard = !showClipboard }) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Clipboard", tint = iconTint)
                }
                IconButton(onClick = onEmojiClick) {
                    Icon(Icons.Default.EmojiEmotions, contentDescription = "Emoji", tint = iconTint)
                }
            }

            // Dynamic Content Area
            if (showClipboard) {
                ClipboardStrip(
                    clips = recentClips,
                    onClipClick = {
                        onClipSelected(it)
                        showClipboard = false
                    }
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(suggestions) { suggestion ->
                        Button(
                            onClick = { onSuggestionClick(suggestion) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primaryAccentColor,
                                contentColor = theme.keyTextColor
                            )
                        ) {
                            Text(suggestion)
                        }
                    }
                }
            }

            // Utility Keys Row (PC Keys)
            if (utilityKeys.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(utilityKeys) { key ->
                        Button(
                            onClick = { onUtilityKeyClick(key.action) },
                            contentPadding = ButtonDefaults.ContentPadding,
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.secondaryAccentColor,
                                contentColor = theme.keyTextColor
                            ),
                            shape = RoundedCornerShape(theme.keyCornerRadius)
                        ) {
                            Text(text = key.label, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Main keyboard layout with Swipe Detector
            val gestureCallback = remember(keyboardRootOffset, swipePathProcessor, swipePredictor, lastTapTime) {
                object : GestureManager.GestureCallback {
                    override fun onSwipe(path: List<Offset>) {
                        // Clear visual trail
                        currentSwipePath = emptyList()

                        val globalPath = path.map { it + keyboardRootOffset }
                        val keySequence = swipePathProcessor.processPathToKeySequence(globalPath)
                        if (keySequence.isNotEmpty()) {
                            val prediction = swipePredictor.predictWord(keySequence)
                            if (prediction.isNotEmpty()) {
                                onSuggestionClick(prediction)
                            }
                        }
                    }

                    override fun onTap(position: Offset) {
                        currentSwipePath = emptyList() // Ensure clear
                        val globalOffset = position + keyboardRootOffset
                        val key = swipePathProcessor.findKeyAt(globalOffset)
                        if (key != null) {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastTapTime > 50) {
                                lastTapTime = currentTime
                                onKeyClick(key)
                            }
                        }
                    }

                    override fun onSwipeProgress(path: List<Offset>) {
                         // Update visual trail state in real-time
                         currentSwipePath = path
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                // 1. The Keyboard Layout (Interactive Layer)
                Column(
                    modifier = gestureManager.applyGestures(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(theme.keyPadding) // Apply Theme Padding
                            .onGloballyPositioned { coordinates ->
                                keyboardRootOffset = coordinates.boundsInRoot().topLeft
                            },
                        callback = gestureCallback
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Determine Active Rows (Language vs Symbols)
                    val activeRows = if (isSymbolMode) {
                        // Convert KeyboardLayout.Symbol strings to KeyData structure locally
                        // This replicates the structure from LanguageKeyboardDatabase for symbols
                        com.aktarjabed.nextgenkeyboard.data.model.KeyboardLayout.Symbol.rows.map { rowStrings ->
                            com.aktarjabed.nextgenkeyboard.data.model.KeyRow(
                                rowStrings.filter { it !in listOf("📋", "SPACE", "⌫", "↵") } // Filter out bottom row if present in static def
                                    .map { label ->
                                        com.aktarjabed.nextgenkeyboard.data.model.KeyData(0, label, label)
                                    }
                            )
                        }
                    } else {
                        language.layout.rows
                    }

                    // Render Active Rows
                    activeRows.forEach { keyRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                        ) {
                            keyRow.keys.forEach { keyData ->
                                // Standard Key Rendering
                                val weight = 1f
                                Key(
                                    char = keyData.display,
                                    onClick = { onKeyClick(keyData.display) },
                                    modifier = Modifier.weight(weight),
                                    onPositioned = { rect ->
                                        swipePathProcessor.registerKeyPosition(keyData.display, rect)
                                    },
                                    backgroundColor = theme.keyBackgroundColor,
                                    textColor = theme.keyTextColor,
                                    cornerRadius = theme.keyCornerRadius,
                                    elevation = theme.keyElevation
                                )
                            }
                        }
                    }

                    // Render Manual Bottom Row (Toggle, Comma, Space, Dot, Backspace, Enter)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                    ) {
                        // ?123 / ABC Toggle
                        val modeLabel = if (isSymbolMode) "ABC" else "?123"
                        Key(
                            char = modeLabel,
                            onClick = { isSymbolMode = !isSymbolMode },
                            modifier = Modifier.weight(1.5f),
                            backgroundColor = theme.secondaryAccentColor, // Highlight
                            textColor = theme.keyTextColor
                        )

                        // Comma
                        Key(
                            char = ",",
                            onClick = { onKeyClick(",") },
                            modifier = Modifier.weight(1f),
                            onPositioned = { r -> swipePathProcessor.registerKeyPosition(",", r) },
                            backgroundColor = theme.keyBackgroundColor,
                            textColor = theme.keyTextColor
                        )

                        // SPACE
                        Key(
                            char = "SPACE",
                            onClick = { onKeyClick("SPACE") },
                            modifier = Modifier.weight(4f), // Wide space
                            backgroundColor = theme.keyBackgroundColor,
                            textColor = theme.keyTextColor
                        )

                        // Period
                        Key(
                            char = ".",
                            onClick = { onKeyClick(".") },
                            modifier = Modifier.weight(1f),
                            onPositioned = { r -> swipePathProcessor.registerKeyPosition(".", r) },
                            backgroundColor = theme.keyBackgroundColor,
                            textColor = theme.keyTextColor
                        )

                        // Backspace
                        Key(
                            char = "⌫",
                            onClick = { onKeyClick("⌫") },
                            modifier = Modifier.weight(1.5f),
                            backgroundColor = theme.secondaryAccentColor,
                            textColor = theme.keyTextColor
                        )

                        // Enter
                        Key(
                            char = "↵",
                            onClick = { onKeyClick("↵") },
                            modifier = Modifier.weight(1.5f),
                            backgroundColor = theme.primaryAccentColor,
                            textColor = theme.keyTextColor
                        )
                    }
                }

                // 2. The Swipe Trail Overlay (Visual Layer)
                if (currentSwipePath.isNotEmpty()) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val path = Path()
                        if (currentSwipePath.size > 1) {
                            path.moveTo(currentSwipePath.first().x, currentSwipePath.first().y)
                            // Use quadratic bezier for smoother curves if needed,
                            // but straight lines for high sampling rate are often sufficient.
                            // Here we just connect points.
                            for (i in 1 until currentSwipePath.size) {
                                path.lineTo(currentSwipePath[i].x, currentSwipePath[i].y)
                            }

                            drawPath(
                                path = path,
                                color = theme.primaryAccentColor.copy(alpha = 0.6f),
                                style = Stroke(
                                    width = 12.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
