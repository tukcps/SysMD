package com.github.tukcps.sysmd.ui.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.SplitterState

/**
 * PanelSize defines constants used for panel resizing
 */
object PanelSize {
    /**
     * Size of a panel when collapsed
     */
    val COLLAPSED_SIZE: Dp = 24.dp

    /**
     * Minimal size of panel when expanded
     */
    val EXPANDED_SIZE_MIN: Dp = 90.dp
}


/**
 * The state of collapsable panel.
 */
internal class PaneState(isExpanded: Boolean) {
    val collapsedSize = PanelSize.COLLAPSED_SIZE
    var expandedSize by mutableStateOf(300.dp)
    val expandedSizeMin = PanelSize.EXPANDED_SIZE_MIN
    var isExpanded by mutableStateOf(isExpanded)
    val splitter = SplitterState()

    init {
        this.isExpanded = isExpanded
    }
}

/**
 * Enum class holding values for deciding whether left panel GUI or right panel GUI should be used
 */
enum class ResizablePanelSide {
    LEFT_SIDE, RIGHT_SIDE
}

/**
 * A composable that displays a composable as a resizable pane.
 * It can be minimized or maximized via an arrow icon.
 */
@Composable
internal fun ResizablePane(
    agendaIsEmpty: MutableState<Boolean>,
    panelPosition: ResizablePanelSide = ResizablePanelSide.LEFT_SIDE,
    modifier: Modifier,
    state: PaneState,
    content: @Composable () -> Unit,
) {
    // After recompile and with the occurrence of an error, the agenda is shown directly
    val expandOnError = remember { mutableStateOf(true) }

    if (!agendaIsEmpty.value && expandOnError.value
        && panelPosition == ResizablePanelSide.RIGHT_SIDE) {
        state.isExpanded = true
        expandOnError.value = false
    }


    val alpha by animateFloatAsState(if (state.isExpanded) 1f else 0f, SpringSpec(stiffness = Spring.StiffnessLow))

    Box(modifier) {
        Box(Modifier.fillMaxSize().graphicsLayer(alpha = alpha)) {
            content()
        }
        val modifierAlignment: Alignment =
            if (panelPosition == ResizablePanelSide.LEFT_SIDE) Alignment.TopEnd else Alignment.TopStart

        SysMDTooltipArea(tooltipText = "Hide/Show sidebar", modifier = Modifier.align(modifierAlignment)) {
            Icon(
                if (state.isExpanded && panelPosition == ResizablePanelSide.LEFT_SIDE) Icons.AutoMirrored.Filled.ArrowBack
                else if (!state.isExpanded && panelPosition == ResizablePanelSide.LEFT_SIDE) Icons.AutoMirrored.Filled.ArrowForward
                else if (state.isExpanded && panelPosition == ResizablePanelSide.RIGHT_SIDE) Icons.AutoMirrored.Filled.ArrowForward
                else Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = if (state.isExpanded) "Collapse" else "Expand",
                tint = LocalContentColor.current,
                modifier = Modifier
                    // .padding(top = 4.dp)
                    .width(20.dp)
                    .clickable { state.isExpanded = !state.isExpanded }
                    .padding(1.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}
