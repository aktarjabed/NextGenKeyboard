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
    var path = mutableListOf<Offset>()
    var isSwipe = false

    awaitEachGesture {
        val down = awaitFirstDown()
        path.clear()
        path.add(down.position)

        do {
            val event = awaitPointerEvent()
            val change = event.changes.first()

            if (change.positionChange().getDistance() > 20f) {
                isSwipe = true
            }

            if (isSwipe) {
                path.add(change.position)
            }

        } while (change.pressed)

        if (isSwipe) {
            onSwipeComplete(path)
        } else {
            onTap(down.position)
        }

        isSwipe = false
    }
}
