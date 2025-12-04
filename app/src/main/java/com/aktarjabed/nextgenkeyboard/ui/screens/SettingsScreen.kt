package com.aktarjabed.nextgenkeyboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aktarjabed.nextgenkeyboard.ui.viewmodel.SettingsViewModel
import timber.log.Timber

/**
 * Settings Screen Composable
 * Displays clipboard history, multi-language options, and preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val clearSuccess by viewModel.clearSuccess.collectAsState()
    val pinnedClips by viewModel.pinnedClips.collectAsState(emptyList())
    val recentClips by viewModel.recentClips.collectAsState(emptyList())

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ================== ERROR MESSAGE ==================
                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // ================== SUCCESS MESSAGE ==================
                if (clearSuccess) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Clipboard cleared successfully",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ================== LANGUAGE SETTINGS ==================
                SettingsSection(title = "Language & Keyboard") {
                    LanguageSettingsCard(viewModel = viewModel)
                }

                // ================== CLIPBOARD MANAGEMENT ==================
                SettingsSection(title = "Clipboard Management") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showClearDialog = true },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Clear All Clips")
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearUnpinnedClips() },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Unpinned Only")
                        }

                        Text(
                            text = "Total clips: ${pinnedClips.size + recentClips.size}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // ================== CLIPBOARD HISTORY ==================
                if (pinnedClips.isNotEmpty() || recentClips.isNotEmpty()) {
                    SettingsSection(title = "Clipboard History") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (pinnedClips.isNotEmpty()) {
                                Text(
                                    text = "Pinned (${pinnedClips.size})",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                pinnedClips.forEach { clip ->
                                    ClipboardItemRow(
                                        text = clip.content.take(50),
                                        isPinned = true
                                    )
                                }
                            }

                            if (recentClips.isNotEmpty()) {
                                Text(
                                    text = "Recent (${recentClips.size})",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                recentClips.take(5).forEach { clip ->
                                    ClipboardItemRow(
                                        text = clip.content.take(50),
                                        isPinned = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ================== CLEAR CONFIRMATION DIALOG ==================
    if (showClearDialog) {
        AlertDialog(
            title = { Text("Clear Clipboard?") },
            text = { Text("This will delete all clipboard history. This action cannot be undone.") },
            onDismissRequest = { showClearDialog = false },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearClipboardHistory()
                    showClearDialog = false
                }) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Settings Section with title and content
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

/**
 * Language Settings Card
 */
@Composable
private fun LanguageSettingsCard(viewModel: SettingsViewModel) {
    val languages = listOf(
        "English (US)" to "en_US",
        "Español" to "es_ES",
        "Français" to "fr_FR",
        "Deutsch" to "de_DE",
        "العربية" to "ar_SA",
        "हिन्दी" to "hi_IN",
        "中文" to "zh_CN",
        "日本語" to "ja_JP",
        "Русский" to "ru_RU"
    )

    var selectedLanguage by remember { mutableStateOf(viewModel.getKeyboardLanguage()) }
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Keyboard Language",
            style = MaterialTheme.typography.labelSmall
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedLanguage.ifEmpty { "Select Language" },
                    modifier = Modifier.weight(1f)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                languages.forEach { (name, code) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            selectedLanguage = code
                            viewModel.setKeyboardLanguage(code)
                            expanded = false
                            Timber.d("Language changed to: $name ($code)")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Clipboard Item Row
 */
@Composable
private fun ClipboardItemRow(
    text: String,
    isPinned: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        if (isPinned) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "📌",
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
