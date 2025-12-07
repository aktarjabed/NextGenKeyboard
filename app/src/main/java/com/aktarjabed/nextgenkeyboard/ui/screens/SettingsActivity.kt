package com.aktarjabed.nextgenkeyboard.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.aktarjabed.nextgenkeyboard.ui.theme.NextGenKeyboardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextGenKeyboardTheme {
                Surface {
                    SettingsScreen(onNavigateBack = { finish() })
                }
            }
        }
    }
}