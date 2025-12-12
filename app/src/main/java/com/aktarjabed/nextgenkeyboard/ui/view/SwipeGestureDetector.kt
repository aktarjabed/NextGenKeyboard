package com.aktarjabed.nextgenkeyboard.ui.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

fun Modifier.detectSwipeGesture(
    onSwipeComplete: (List<Offset>) -> Unit,
    onTap: (Offset) -> Unit
): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val pointerId = down.id
        var path = mutableListOf<Offset>()
        path.add(down.position)
        var isSwipe = false

        // Track ONLY this specific pointer
        do {
            val event = awaitPointerEvent()
            val change = event.changes.find { it.id == pointerId }

            if (change != null && change.pressed) {
                if (!isSwipe && (change.position - down.position).getDistance() > 20f) {
                    isSwipe = true
                }
                if (isSwipe) {
                    path.add(change.position)
                }
                change.consume() // Consume events for this pointer
            } else {
                break // Pointer lifted
            }
        } while (change?.pressed == true)

        if (isSwipe) {
            onSwipeComplete(path)
        } else {
            // Only register tap if it wasn't a swipe and wasn't consumed elsewhere
            onTap(down.position)
        }
    }
}
