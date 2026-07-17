package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.AppTheme

/**
 * A Composable that displays an icon in a color, and a tooltips.
 * If selected, the lambda parameter onSelection is called.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class) //TODO rewrite pointerMoveFilter to be stable
@Suppress("EXPERIMENTAL_API_USAGE_FUTURE_ERROR", "EXPERIMENTAL_IS_NOT_ENABLED")
@Composable
fun SelectableIcon(
    icon: ImageVector,
    color: Color,
    tooltip: String,
    onSelection: () -> Unit
) {
    val active = remember { mutableStateOf(false) }
    TooltipArea(tooltip = {
        Box( modifier = Modifier.background(AppTheme.colors.backgroundWarning)) {
            Text(modifier = Modifier.padding(10.dp), text = tooltip)
        }
    },
        modifier = Modifier.offset(4.dp).requiredSize(AppTheme.fontSize.value.dp)
            .onPointerEvent(PointerEventType.Enter) { active.value = true }
            .onPointerEvent(PointerEventType.Exit) { active.value = false },
        delayMillis = 400, // in milliseconds
        // offset = DpOffset((-16).dp, 0.dp),
        // to use required size to enforce fitting size
        //offset the Icon to be fully shown in front of the line number
        tooltipPlacement = TooltipPlacement.CursorPoint(
            offset = DpOffset(0.dp, 16.dp)
        )) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier
                .clickable { onSelection() },
            tint = color
        )
    }
}
