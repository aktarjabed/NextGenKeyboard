package com.aktarjabed.nextgenkeyboard.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GridContentKeyboard(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    content: @Composable BoxScope.() -> Unit,
    bottomBar: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            topBar()
        }

        // Main Grid Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            content = content
        )

        // Bottom Bar (Optional)
        if (bottomBar != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                bottomBar()
            }
        }
    }
}
