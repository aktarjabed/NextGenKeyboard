package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

fun Modifier.detectSwipeGesture(
    onSwipeComplete: (List<Offset>) -> Unit,
    onTap: (Offset) -> Unit
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val activePointers = mutableMapOf<PointerId, MutableList<Offset>>()

        // Initialize with the first pointer
        activePointers[down.id] = mutableListOf(down.position)

        do {
            val event = awaitPointerEvent()

            // Handle new pointers down or pointers moving
            for (change in event.changes) {
                if (change.changedToDown()) {
                    activePointers[change.id] = mutableListOf(change.position)
                } else if (change.pressed) {
                    val path = activePointers[change.id]
                    if (path != null) {
                        val lastPos = path.lastOrNull() ?: change.position
                        if ((change.position - lastPos).getDistance() > 10f) {
                            path.add(change.position)
                        }
                    }
                }
                // Don't consume yet, let other detectors work if needed?
                // But we are the main detector.
                if (change.pressed) {
                     change.consume()
                }
            }

            // Handle pointers up
            // Use a safe copy of keys to avoid ConcurrentModificationException
            val pointerIds = activePointers.keys.toList()
            for (pointerId in pointerIds) {
                val change = event.changes.find { it.id == pointerId }
                if (change == null || !change.pressed) {
                    // Pointer lifted or cancelled
                    val path = activePointers.remove(pointerId)
                    if (path != null) {
                        // Determine if it was a swipe or tap
                        // Simple heuristic: distance or point count
                        val isSwipe = path.size > 2 || (path.size > 1 && (path.last() - path.first()).getDistance() > 20f)

                        if (isSwipe) {
                            onSwipeComplete(path)
                        } else {
                            if (path.isNotEmpty()) {
                                onTap(path.first())
                            }
                        }
                    }
                }
            }
        } while (activePointers.isNotEmpty())
    }
}
