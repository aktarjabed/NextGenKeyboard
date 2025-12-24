package com.aktarjabed.nextgenkeyboard.ui.gestures

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import com.aktarjabed.nextgenkeyboard.ui.view.detectSwipeGesture
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GestureManager
 * Wraps gesture detection logic to provide a unified interface for
 * swipes, taps, and future gestures (double tap, long press).
 */
@Singleton
class GestureManager @Inject constructor() {

    // Currently, our main gesture logic is implemented as a Compose Modifier extension
    // in SwipeGestureDetector.kt. To "manager-ize" it, we can expose a helper
    // that returns the configured modifier.

    interface GestureCallback {
        fun onSwipe(path: List<Offset>)
        fun onTap(position: Offset)
        fun onSwipeProgress(path: List<Offset>)
    }

    fun applyGestures(
        modifier: Modifier = Modifier,
        callback: GestureCallback
    ): Modifier {
        return modifier.detectSwipeGesture(
            onSwipeComplete = { path -> callback.onSwipe(path) },
            onTap = { position -> callback.onTap(position) },
            onSwipeProgress = { path -> callback.onSwipeProgress(path) }
        )
    }
}
