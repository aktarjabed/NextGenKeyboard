package com.nextgen.keyboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nextgen.keyboard.data.model.KeyData
import com.nextgen.keyboard.data.model.Language

@Composable
fun KeyboardView(
    language: Language,
    onKeyPress: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(2.dp)
    ) {
        language.layout.rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { keyData ->
                    Key(
                        keyData = keyData,
                        onKeyPress = onKeyPress,
                        modifier = Modifier.weight(keyData.keyWidth)
                    )
                }
                if (index == 2) { // Add backspace key to the 3rd row
                    IconKey(
                        icon = Icons.Default.Backspace,
                        onClick = { onKeyPress("BACKSPACE") },
                        description = "Backspace",
                        modifier = Modifier.weight(1.5f) // make it a bit wider
                    )
                }
            }
        }
        // Bottom row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Key(keyData = KeyData("?123"), onKeyPress = { /* TODO */ }, modifier = Modifier.weight(1.5f))
            Key(keyData = KeyData(","), onKeyPress = onKeyPress, modifier = Modifier.weight(1f))
            Key(keyData = KeyData(""), onKeyPress = { onKeyPress(" ") }, modifier = Modifier.weight(5f))
            Key(keyData = KeyData("."), onKeyPress = onKeyPress, modifier = Modifier.weight(1f))
            IconKey(
                icon = Icons.Default.Send,
                onClick = { onKeyPress("ENTER") },
                description = "Enter",
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}

@Composable
fun Key(
    keyData: KeyData,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onKeyPress(keyData.primary) }
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = keyData.primary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun IconKey(
    icon: ImageVector,
    onClick: () -> Unit,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .height(56.dp), // same height as other keys
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}