package com.nextgen.keyboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nextgen.keyboard.data.model.KeyboardLayout
import com.nextgen.keyboard.ui.viewmodel.SettingsViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = true)
    val selectedLayout by viewModel.selectedLayout.collectAsState(initial = "QWERTY")
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState(initial = true)
    val isSwipeEnabled by viewModel.isSwipeEnabled.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // APPEARANCE SECTION
            item {
                SectionHeader(
                    title = "Appearance",
                    icon = Icons.Default.Palette
                )
            }

            item {
                SettingsCard {
                    ThemeSwitchSetting(
                        isDarkMode = isDarkMode,
                        onThemeChange = {
                            try {
                                viewModel.setDarkMode(it)
                            } catch (e: Exception) {
                                Timber.e(e, "Error changing theme")
                            }
                        }
                    )
                }
            }

            // KEYBOARD LAYOUT SECTION
            item {
                SectionHeader(
                    title = "Keyboard Layouts",
                    icon = Icons.Default.Keyboard
                )
            }

            item {
                LayoutSelectionCard(
                    selectedLayout = selectedLayout,
                    onLayoutSelected = {
                        try {
                            viewModel.setLayout(it)
                        } catch (e: Exception) {
                            Timber.e(e, "Error changing layout")
                        }
                    }
                )
            }

            // TYPING SECTION
            item {
                SectionHeader(
                    title = "Typing",
                    icon = Icons.Default.TouchApp
                )
            }

            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SwitchSetting(
                            icon = Icons.Default.Vibration,
                            title = "Haptic Feedback",
                            description = "Vibrate on key press",
                            checked = isHapticEnabled,
                            onCheckedChange = {
                                try {
                                    viewModel.setHapticFeedback(it)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error toggling haptic feedback")
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        SwitchSetting(
                            icon = Icons.Default.Gesture,
                            title = "Swipe Typing",
                            description = "Type by swiping across letters",
                            checked = isSwipeEnabled,
                            onCheckedChange = {
                                try {
                                    viewModel.setSwipeTyping(it)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error toggling swipe typing")
                                }
                            }
                        )
                    }
                }
            }

            // SECURITY SECTION
            item {
                SectionHeader(
                    title = "Security & Privacy",
                    icon = Icons.Default.Security
                )
            }

            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SecurityInfoItem(
                            icon = Icons.Default.Lock,
                            title = "Password Protection",
                            description = "Clipboard and swipe disabled in password fields"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SecurityInfoItem(
                            icon = Icons.Default.Shield,
                            title = "Screenshot Prevention",
                            description = "Screen capture blocked for sensitive inputs"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SecurityInfoItem(
                            icon = Icons.Default.History,
                            title = "No Password Logging",
                            description = "Password input never saved to history"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SecurityInfoItem(
                            icon = Icons.Default.NoAccounts,
                            title = "No Data Collection",
                            description = "Your typing data stays on your device"
                        )
                    }
                }
            }

            // CLIPBOARD SECTION
            item {
                SectionHeader(
                    title = "Clipboard",
                    icon = Icons.Default.ContentPaste
                )
            }

            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Clipboard History",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Saves recent text for quick access",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = {
                                    try {
                                        viewModel.clearClipboardHistory()
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error clearing clipboard")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Clear All")
                            }
                        }
                    }
                }
            }

            // ABOUT SECTION
            item {
                SectionHeader(
                    title = "About",
                    icon = Icons.Default.Info
                )
            }

            item {
                SettingsCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "NextGen Keyboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Advanced keyboard with 10 layouts, swipe typing, clipboard management, and enterprise-grade security",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun ThemeSwitchSetting(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isDarkMode) "Currently enabled" else "Currently disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = isDarkMode,
            onCheckedChange = onThemeChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun LayoutSelectionCard(
    selectedLayout: String,
    onLayoutSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val allLayouts = KeyboardLayout.getAllLayouts()

    SettingsCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Current Layout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        selectedLayout,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                allLayouts.forEach { layout ->
                    LayoutOption(
                        layoutName = layout.name,
                        isSelected = selectedLayout == layout.name,
                        description = getLayoutDescription(layout.name),
                        onClick = {
                            onLayoutSelected(layout.name)
                            expanded = false
                        }
                    )
                    if (layout != allLayouts.last()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LayoutOption(
    layoutName: String,
    isSelected: Boolean,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = layoutName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SecurityInfoItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getLayoutDescription(layoutName: String): String {
    return when (layoutName) {
        "QWERTY" -> "Standard keyboard layout - familiar and widely used"
        "Dvorak" -> "Ergonomic layout - reduces finger movement"
        "Colemak" -> "Modern efficient layout - optimized for English"
        "Numeric" -> "Numbers and symbols - ideal for data entry"
        "Symbol" -> "Programming symbols - perfect for developers"
        "Emoji" -> "Express yourself - quick emoji access"
        "Phone" -> "Dialpad style - phone number entry"
        "Gaming" -> "WASD controls - optimized for gaming"
        "Minimal" -> "Distraction-free - clean and simple"
        "Accessible" -> "Large keys - easier to read and tap"
        else -> "Custom keyboard layout"
    }
}