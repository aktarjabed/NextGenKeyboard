package com.nextgen.keyboard.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nextgen.keyboard.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = true)
    val selectedLayout by viewModel.selectedLayout.collectAsState(initial = "QWERTY")
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState(initial = true)
    val isSwipeEnabled by viewModel.isSwipeEnabled.collectAsState(initial = true)
    val isClipboardEnabled by viewModel.isClipboardEnabled.collectAsState(initial = true)
    val isBlockSensitive by viewModel.isBlockSensitive.collectAsState(initial = true)
    val autoDeleteDays by viewModel.autoDeleteDays.collectAsState(initial = 30)
    val maxClipboardItems by viewModel.maxClipboardItems.collectAsState(initial = 500)
    val giphyApiKey by viewModel.giphyApiKey.collectAsState(initial = "")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SectionHeader(
                title = "General",
                icon = Icons.Default.Tune
            )
        }
        item {
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    SwitchSetting(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        description = "Enable dark theme",
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    SwitchSetting(
                        icon = Icons.Default.Gesture,
                        title = "Swipe Typing",
                        description = "Enable swipe to type",
                        checked = isSwipeEnabled,
                        onCheckedChange = { viewModel.setSwipeTyping(it) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    SwitchSetting(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Feedback",
                        description = "Vibrate on keypress",
                        checked = isHapticEnabled,
                        onCheckedChange = { viewModel.setHapticFeedback(it) }
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Privacy & Data",
                icon = Icons.Default.PrivacyTip
            )
        }

        item {
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    SwitchSetting(
                        icon = Icons.Default.ContentPaste,
                        title = "Clipboard History",
                        description = "Save clipboard entries for quick access",
                        checked = isClipboardEnabled,
                        onCheckedChange = { viewModel.setClipboardEnabled(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    SwitchSetting(
                        icon = Icons.Default.Block,
                        title = "Block Sensitive Content",
                        description = "Don't save passwords, OTPs, or credit cards",
                        checked = isBlockSensitive,
                        onCheckedChange = { viewModel.setBlockSensitiveContent(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto-delete slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Auto-Delete After",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$autoDeleteDays days",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = autoDeleteDays.toFloat(),
                            onValueChange = { viewModel.setAutoDeleteDays(it.toInt()) },
                            valueRange = 1f..90f,
                            steps = 89,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Max items slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Maximum Items",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$maxClipboardItems items",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = maxClipboardItems.toFloat(),
                            onValueChange = { viewModel.setMaxClipboardItems(it.toInt()) },
                            valueRange = 50f..2000f,
                            steps = 39,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Data Management",
                icon = Icons.Default.CleaningServices
            )
        }

        item {
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.performManualCleanup() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clean Up Now",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Remove old and excess clipboard items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Integrations",
                icon = Icons.Default.Power
            )
        }

        item {
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = giphyApiKey,
                        onValueChange = { viewModel.setGiphyApiKey(it) },
                        label = { Text("Giphy API Key") },
                        placeholder = { Text("Enter your Giphy API key") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Gif, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun SwitchSetting(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}