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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Key(
    char: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPositioned: ((Rect) -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    cornerRadius: Dp = 8.dp,
    elevation: Dp = 0.dp
) {
    Box(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                onPositioned?.invoke(layoutCoordinates.boundsInRoot())
            }
            .shadow(elevation, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .semantics {
                role = Role.Button
                contentDescription = char
                onClick(label = char) {
                    onClick()
                    true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = char, color = textColor)
    }
}
