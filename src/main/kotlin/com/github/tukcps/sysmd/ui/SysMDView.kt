@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.agenda.Agenda
import com.github.tukcps.sysmd.ui.composables.*
import com.github.tukcps.sysmd.ui.dialogs.messageProjectAlreadyExits
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.VerticalSplittable
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel

/**
 * The main window with three sections:
 * - On top, a menu line with items to select actions.
 * - below, two columns:
 *   + A treeview for selection and navigation in hierarchy.
 *   + A notebook with tabs for editing and viewing packages.
 */
data class DisplayState(
    val scrollState: LazyListState,
    val scaleState: MutableState<Float>,
    val offsetState: MutableState<Offset>,
    val editState: MutableState<Boolean>,
    val showInfo: Map<Int, MutableState<Boolean>>,
)


/**
 * Animates the size of the given panel, represented by PanelState object
 */
@Composable
internal fun panelSizeAnimation(panelState: PanelState): Dp {
    val animatedSize = if (panelState.splitter.isResizing) {
        if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize
    } else {
        animateDpAsState(
            if (panelState.isExpanded) panelState.expandedSize else panelState.collapsedSize,
            SpringSpec(stiffness = Spring.StiffnessLow)
        ).value
    }
    return animatedSize
}

/**
 * Manages and checks the sizes of a panel represented by PanelState object
 */
internal fun checkPanelResize(panelState: PanelState, changeSize: Dp) {
    panelState.expandedSize =
        (panelState.expandedSize + changeSize).coerceAtLeast(panelState.expandedSizeMin)
}


/**
 * Function that displays a SysMDViewModel.
 * It visualizes:
 * - A treeview for navigation in different hierarchies, selectable by tabs (left)
 * - A main editor window with tabs for each file.
 */
@Composable
fun SysMDView(model: SysMDViewModel) {
    // var dropDownMenuState by remember { mutableStateOf("Agenda") }

    val sysMDViewModel = remember { model }

    // States of the window and its components.
    val editorTabsModel = sysMDViewModel.editorTabsViewModel

    // State of the left resizable panel
    val leftPanelState = remember { PanelState(true) }

    // Animation for the left panel
    val leftPanelAnimatedSize = panelSizeAnimation(leftPanelState)

    // State of the right resizable panel
    val rightPanelState = remember { PanelState(false) }

    // Animation for the right panel
    val rightPanelAnimatedSize = panelSizeAnimation(rightPanelState)

    /**
     * Controls the state of the tab list in the editor
     */
    val editorTabListState: LazyListState = rememberLazyListState()

    // The SysMD Notebook View itself.
    Surface {
        Row {
            Column(Modifier.fillMaxWidth()) {

                //the menu line
                MenuLine(sysMDViewModel)

                //TODO find more elegant and clean solution
                VerticalSplittable(Modifier.fillMaxSize(),
                    leftPanelState.splitter, onResizeLeft = { checkPanelResize(leftPanelState, it) },
                    /* Negating the Dp value in 'it' is necessary, as vertical splitter resizing is hard coded
                       depending on the drag direction */
                    rightPanelState.splitter, onResizeRight = { checkPanelResize(rightPanelState, -it) }
                ) {
                    if (sysMDViewModel.showDialogProjectAlreadyExits.value)
                        messageProjectAlreadyExits(sysMDViewModel.showDialogProjectAlreadyExits)

                    // The navigation panel
                    ResizablePanel(
                        sysMDViewModel.agendaIsEmpty,
                        ResizablePanelSide.LEFT_SIDE,
                        Modifier.width(leftPanelAnimatedSize).fillMaxHeight(),
                        leftPanelState
                    ) {
                        NavigationPanel(sysMDViewModel)
                    }

                    // The editor tabs (=all open files) and the active editor (active tab).
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            EditorTabs(editorTabsModel)
                            Box(Modifier.weight(1f).fillMaxSize()) {
                                if (editorTabsModel.active != null) {
                                    (editorTabsModel.active!!).scrollState = editorTabListState
                                    CellList(editorTabsModel.active!!)
                                }
                            }
                        }
                    }

                    // The panel with the agenda
                    ResizablePanel(
                        sysMDViewModel.agendaIsEmpty,
                        ResizablePanelSide.RIGHT_SIDE,
                        Modifier.width(rightPanelAnimatedSize).fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background),
                        rightPanelState
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(0.2.dp))
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(" Issue-Board ", fontSize = AppTheme.fontSize)
                            }
                            Row {
                                Agenda(sysMDViewModel.agenda, editorTabsModel.active)
                            }
                        }
                    }
                }
            }
        }
        // DropBox(window, model.tabsModel::create)
    }
}
