package com.aktarjabed.nextgenkeyboard.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.aktarjabed.nextgenkeyboard.ui.theme.NextGenKeyboardTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install splash screen
        installSplashScreen()

        Timber.d("MainActivity created")

        setContent {
            NextGenKeyboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var isKeyboardEnabled by remember { mutableStateOf(isKeyboardEnabledInSystem(context)) }
    var isKeyboardSelected by remember { mutableStateOf(isKeyboardSelectedInSystem(context)) }

    // Check status on resume
    LaunchedEffect(Unit) {
        isKeyboardEnabled = isKeyboardEnabledInSystem(context)
        isKeyboardSelected = isKeyboardSelectedInSystem(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Assuming Icons.Default.Keyboard exists or using a substitute
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "App Icon",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "NextGen Keyboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Hero Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Welcome to NextGen Keyboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Advanced keyboard with 10 layouts, swipe typing, clipboard management, and enterprise-grade security",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Setup Steps
            SetupStepCard(
                stepNumber = 1,
                title = "Enable Keyboard",
                description = "Add NextGen Keyboard to your device",
                isCompleted = isKeyboardEnabled,
                buttonText = if (isKeyboardEnabled) "✓ Enabled" else "Enable Now",
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    } catch (e: Exception) {
                        Timber.e(e, "Error opening input method settings")
                    }
                }
            )

            SetupStepCard(
                stepNumber = 2,
                title = "Select Keyboard",
                description = "Choose NextGen Keyboard as your default",
                isCompleted = isKeyboardSelected,
                buttonText = if (isKeyboardSelected) "✓ Selected" else "Select Now",
                onClick = {
                    try {
                        val imm = context.getSystemService(InputMethodManager::class.java)
                        imm.showInputMethodPicker()
                    } catch (e: Exception) {
                        Timber.e(e, "Error showing input method picker")
                    }
                }
            )

            SetupStepCard(
                stepNumber = 3,
                title = "Configure Settings",
                description = "Customize your keyboard experience",
                isCompleted = true,
                buttonText = "Open Settings",
                onClick = {
                    try {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    } catch (e: Exception) {
                        Timber.e(e, "Error opening settings")
                    }
                }
            )

            // Features Section
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Key Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            FeatureCard(
                icon = Icons.Default.Abc,
                title = "10 Keyboard Layouts",
                description = "QWERTY, Dvorak, Colemak, Numeric, Symbol, Emoji, Phone, Gaming, Minimal, Accessible"
            )

            FeatureCard(
                icon = Icons.Default.TouchApp,
                title = "Swipe Typing",
                description = "Fast and accurate gesture typing for efficient text input"
            )

            FeatureCard(
                icon = Icons.Default.ContentPaste,
                title = "Smart Clipboard",
                description = "Manage clipboard history with search and pin favorites"
            )

            FeatureCard(
                icon = Icons.Default.Security,
                title = "Enterprise Security",
                description = "Password field detection, screenshot blocking, no data logging"
            )

            FeatureCard(
                icon = Icons.Default.Vibration,
                title = "Haptic Feedback",
                description = "Customizable tactile feedback for every key press"
            )

            FeatureCard(
                icon = Icons.Default.DarkMode,
                title = "Dark & Light Themes",
                description = "Comfortable typing in any lighting condition"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SetupStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    isCompleted: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step Number
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (isCompleted)
                    MaterialTheme.colorScheme.secondary
                else
                    MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        Text(
                            text = stepNumber.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Button
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                ),
                enabled = !isCompleted || stepNumber == 3
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun isKeyboardEnabledInSystem(context: Context): Boolean {
    return try {
        val inputMethodManager = context.getSystemService(InputMethodManager::class.java)
        val enabledInputMethods = inputMethodManager.enabledInputMethodList
        enabledInputMethods.any { it.packageName == context.packageName }
    } catch (e: Exception) {
        Timber.e(e, "Error checking keyboard enabled status")
        false
    }
}

fun isKeyboardSelectedInSystem(context: Context): Boolean {
    return try {
        val currentInputMethod = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        currentInputMethod?.contains(context.packageName) == true
    } catch (e: Exception) {
        Timber.e(e, "Error checking keyboard selected status")
        false
    }
}
