package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import timber.log.Timber

fun Modifier.swipeGestureDetector(
    onSwipeStart: (Offset) -> Unit,
    onSwipeMove: (Offset) -> Unit,
    onSwipeEnd: () -> Unit
): Modifier {
    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset ->
                onSwipeStart(offset)
                Timber.d("Swipe started at $offset")
            },
            onDrag = { change, dragAmount ->
                change.consume()
                onSwipeMove(change.position)
            },
            onDragEnd = {
                onSwipeEnd()
                Timber.d("Swipe ended")
            }
        )
    }
}
