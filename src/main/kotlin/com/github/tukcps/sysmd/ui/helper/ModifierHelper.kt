package com.github.tukcps.sysmd.ui.helper

import androidx.compose.foundation.background
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent


fun Modifier.thenIf(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    if (!condition) return this
    return modifier.invoke(this)
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.hoverIndicator(): Modifier {
    val back = MaterialTheme.colors.background
    var color by remember(back) { mutableStateOf<Color?>(null) }

    return this
        // "thenIf" same as ".run { if (color != null) background(color = color!!) else this }"
        .thenIf(color != null) { background(color = color!!) }
        .onPointerEvent(PointerEventType.Enter) {
            color = Color(0f,0f,0f,0.1f)
        }
        .onPointerEvent(PointerEventType.Exit) {
            color = null
        }
}