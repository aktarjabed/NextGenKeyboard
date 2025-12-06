package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun Key(
    char: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPositioned: ((Rect) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                onPositioned?.invoke(layoutCoordinates.boundsInRoot())
            }
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .semantics {
                role = Role.Button
                onClick(label = char) {
                    onClick()
                    true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = char, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
