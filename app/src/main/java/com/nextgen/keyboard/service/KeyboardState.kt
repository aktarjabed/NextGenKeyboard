package com.nextgen.keyboard.service

sealed class KeyboardState {
    object Main : KeyboardState()
    object Voice : KeyboardState()
    object Gif : KeyboardState()
}