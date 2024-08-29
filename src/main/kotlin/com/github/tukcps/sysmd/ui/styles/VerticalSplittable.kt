@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package com.github.tukcps.sysmd.ui.styles

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.composables.PanelSize
import java.awt.Cursor


class SplitterState {
    var isResizing by mutableStateOf(false)
    var isResizeEnabled by mutableStateOf(true)
}

/**
 * Vertical drag bar
 */
@Composable
fun VerticalSplitter(
    splitterState: SplitterState,
    onResize: (delta: Dp) -> Unit,
    color: Color = AppTheme.colors.backgroundDark
) = Box {
    val density = LocalDensity.current
    /**
     * Enables dragging the vertical with the cursor in a range of 8.dp
     */
    Box(
        Modifier
            .width(8.dp)
            .fillMaxHeight()
            .run {
                if (splitterState.isResizeEnabled) {
                    this.draggable(
                        state = rememberDraggableState {
                            with(density) {
                                onResize(it.toDp())
                            }
                        },
                        orientation = Orientation.Horizontal,
                        startDragImmediately = true,
                        onDragStarted = { splitterState.isResizing = true },
                        onDragStopped = { splitterState.isResizing = false }
                        // this should change the Cursor Icon the same way CursorForHorizontalResize did
                        //
                    ).pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                } else {
                    this
                }
            }
    )

    /**
     * The visual vertical bar
     */
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color)
    )
}


/**
 * Composable combining the left menu panel, the vertical bar for changing size and the actual editor box
 */
@Composable
fun VerticalSplittable(
    modifier: Modifier,
    leftSplitterState: SplitterState,
    onResizeLeft: (delta: Dp) -> Unit,
    rightSplitterState: SplitterState,
    onResizeRight: (delta: Dp) -> Unit,
    children: @Composable () -> Unit
) = Layout({
    children()
    VerticalSplitter(leftSplitterState, onResizeLeft)
    VerticalSplitter(rightSplitterState, onResizeRight)
    //+splitter +agenda
}, modifier, measurePolicy = { measurables, constraints ->
    require(measurables.size == 5)

    /**
     * Left panel
     */
    val firstPlaceable = measurables[0].measure(
        constraints.copy(
            minWidth = 0,
            maxWidth = constraints.maxWidth - PanelSize.COLLAPSED_SIZE.value.toInt() - 2
        )
    )

    //Configuring right panel
    /**
     * Right panel
     */
    val rightPanelPlaceable = measurables[2].measure(
        constraints.copy(
            minWidth = 0,
            //TODO The width sizing mechanism differs between the right and left panel. A common solution would be better
            maxWidth = (constraints.maxWidth - firstPlaceable.width - 2).coerceAtLeast(0)
        )
    )

    val secondWidth = constraints.maxWidth - firstPlaceable.width - rightPanelPlaceable.width

    val secondPlaceable = measurables[1].measure(
        Constraints(
            minWidth = secondWidth,
            maxWidth = secondWidth,
            minHeight = constraints.maxHeight,
            maxHeight = constraints.maxHeight
        )
    )


    val leftSplitterPlaceable = measurables[3].measure(constraints)
    val rightSplitterPlaceable = measurables[4].measure(constraints)
    layout(constraints.maxWidth, constraints.maxHeight) {
        firstPlaceable.place(0, 0)
        secondPlaceable.place(firstPlaceable.width, 0)
        leftSplitterPlaceable.place(firstPlaceable.width, 0)
        rightPanelPlaceable.place(firstPlaceable.width + secondPlaceable.width, 0)
        rightSplitterPlaceable.place(firstPlaceable.width + secondPlaceable.width, 0)
    }
})


// no longer works like this in beta5, but also not needed any more I think
/*
fun Modifier.cursorForHorizontalResize(): Modifier = composed {
    var isHover by remember { mutableStateOf(false) }

    pointerMoveFilter(
        onEnter = { isHover = true; true },
        onExit = { isHover = false; true }
    ).pointerIcon(
        PointerIcon(
            if (isHover) {
                Cursor(Cursor.E_RESIZE_CURSOR)
            } else {
                Cursor.getDefaultCursor()
            }
        )
    )
}
*/
