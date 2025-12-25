package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.CancellationException
import timber.log.Timber

fun Modifier.detectSwipeGesture(
    onSwipeComplete: (List<Offset>) -> Unit,
    onTap: (Offset) -> Unit,
    onSwipeProgress: (List<Offset>) -> Unit = {}
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        try {
            val down = awaitFirstDown(requireUnconsumed = false)
            val activePointers = mutableMapOf<PointerId, MutableList<Offset>>()
            val MAX_POINTERS = 10

            // Initialize with the first pointer
            activePointers[down.id] = mutableListOf(down.position)
            onSwipeProgress(activePointers[down.id]!!)

            do {
                val event = awaitPointerEvent()
                var hasUpdates = false

                // Handle new pointers down or pointers moving
                for (change in event.changes) {
                    if (change.changedToDown()) {
                        if (activePointers.size < MAX_POINTERS) {
                            activePointers[change.id] = mutableListOf(change.position)
                            hasUpdates = true
                            // Trigger immediate tap logic on down for responsiveness?
                            // No, wait for up to distinguish swipe.
                        } else {
                            Timber.w("Max pointers reached ($MAX_POINTERS), ignoring new pointer")
                        }
                    } else if (change.pressed) {
                        val path = activePointers[change.id]
                        if (path != null) {
                            val lastPos = path.lastOrNull() ?: change.position
                            // Only add if moved significantly to reduce noise
                            if ((change.position - lastPos).getDistance() > 10f) {
                                // Safety: Cap individual path size if needed, though processor handles it too
                                if (path.size < 600) {
                                    path.add(change.position)
                                    hasUpdates = true
                                }
                            }
                        }
                    }
                    // IMPORTANT: Only consume if we are actively tracking it as a swipe/gesture
                    // In a keyboard, we likely want to consume all touches to prevent underlying view interaction
                    if (change.pressed) {
                        change.consume()
                    }
                }

                // Notify progress for visual trail
                if (hasUpdates) {
                    // We only track the first active pointer for the visual trail for now
                    // to avoid visual clutter with multi-touch swipes
                    val firstPointerId = activePointers.keys.firstOrNull()
                    if (firstPointerId != null) {
                        val path = activePointers[firstPointerId]
                        if (path != null) {
                            onSwipeProgress(path.toList())
                        }
                    }
                }

                // Handle pointers up
                val pointerIds = activePointers.keys.toList()
                for (pointerId in pointerIds) {
                    val change = event.changes.find { it.id == pointerId }
                    if (change == null || !change.pressed) {
                        // Pointer lifted or cancelled
                        val path = activePointers.remove(pointerId)
                        if (path != null) {
                            try {
                                // Clear visual trail on lift
                                onSwipeProgress(emptyList())

                                // Determine if it was a swipe or tap
                                val isSwipe = path.size > 2 || (path.size > 1 && (path.last() - path.first()).getDistance() > 20f)

                                if (isSwipe) {
                                    onSwipeComplete(path)
                                } else {
                                    if (path.isNotEmpty()) {
                                        onTap(path.first())
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error processing gesture completion")
                            }
                        }
                    }
                }
            } while (activePointers.isNotEmpty())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Error in gesture detector loop")
        }
    }
}
