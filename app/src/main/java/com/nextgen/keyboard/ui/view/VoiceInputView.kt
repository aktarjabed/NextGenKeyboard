package com.nextgen.keyboard.ui.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nextgen.keyboard.feature.voice.VoiceInputState
import kotlin.math.sin

@Composable
fun VoiceInputSheet(
    state: VoiceInputState,
    volume: Float,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                // Voice visualizer
                when (state) {
                    is VoiceInputState.Listening -> {
                        VoiceVisualizer(volume = volume)
                    }
                    is VoiceInputState.PartialResult -> {
                        VoiceVisualizer(volume = volume)
                    }
                    else -> {
                        MicrophoneIcon(isActive = false)
                    }
                }

                // Status text
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                    },
                    label = "voice_status"
                ) { currentState ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (currentState) {
                            is VoiceInputState.Idle -> {
                                Text(
                                    "Tap to speak",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Voice typing is ready",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            is VoiceInputState.Listening -> {
                                Text(
                                    "Listening...",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Speak now",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            is VoiceInputState.PartialResult -> {
                                Text(
                                    currentState.text,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            is VoiceInputState.Result -> {
                                Text(
                                    "✓ ${currentState.text}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            is VoiceInputState.Error -> {
                                Text(
                                    "Error",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    currentState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (state) {
                        is VoiceInputState.Idle, is VoiceInputState.Error -> {
                            FilledTonalButton(
                                onClick = onStartListening,
                                modifier = Modifier.width(160.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Start")
                            }
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("Cancel")
                            }
                        }
                        is VoiceInputState.Listening, is VoiceInputState.PartialResult -> {
                            Button(
                                onClick = onStopListening,
                                modifier = Modifier.width(160.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Stop")
                            }
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("Cancel")
                            }
                        }
                        is VoiceInputState.Result -> {
                            // Auto-dismiss after result
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceVisualizer(
    volume: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val scale by animateFloatAsState(
        targetValue = 1f + volume * 0.3f,
        animationSpec = tween(100),
        label = "scale"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Animated waves
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val baseRadius = size.minDimension / 4

            // Draw 3 concentric waves
            for (i in 1..3) {
                val radius = baseRadius * (1 + i * 0.3f) * scale
                val alpha = (1f - i * 0.25f) * volume

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00B7EB).copy(alpha = alpha),
                            Color(0xFF00B7EB).copy(alpha = 0f)
                        )
                    ),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Center microphone icon
        Surface(
            modifier = Modifier
                .size(80.dp)
                .scale(scale),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun MicrophoneIcon(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(100.dp),
        shape = CircleShape,
        color = if (isActive)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}