package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aktarjabed.nextgenkeyboard.data.model.Clip

@Composable
fun ClipboardStrip(
    clips: List<Clip>,
    onClipClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(clips) { clip ->
            ClipboardItemChip(
                text = clip.content,
                onClick = { onClipClick(clip.content) }
            )
        }
    }
}

@Composable
fun ClipboardItemChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = text.take(20).replace("\n", " "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .align(Alignment.CenterVertically)
        )
    }
}
