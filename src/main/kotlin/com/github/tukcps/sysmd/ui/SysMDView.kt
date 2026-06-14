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
import com.github.tukcps.sysmd.ui.composables.PaneState
import com.github.tukcps.sysmd.ui.composables.ResizablePane
import com.github.tukcps.sysmd.ui.composables.ResizablePanelSide
import com.github.tukcps.sysmd.ui.dialogs.messageProjectAlreadyExits
import com.github.tukcps.sysmd.ui.paneleft.NavigationPane
import com.github.tukcps.sysmd.ui.paneright.Board
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
internal fun paneSizeAnimation(paneState: PaneState): Dp {
    val animatedSize = if (paneState.splitter.isResizing) {
        if (paneState.isExpanded) paneState.expandedSize else paneState.collapsedSize
    } else {
        animateDpAsState(
            if (paneState.isExpanded) paneState.expandedSize else paneState.collapsedSize,
            SpringSpec(stiffness = Spring.StiffnessLow)
        ).value
    }
    return animatedSize
}

/**
 * Manages and checks the sizes of a panel represented by PanelState object
 */
internal fun checkPanelResize(paneState: PaneState, changeSize: Dp) {
    paneState.expandedSize =
        (paneState.expandedSize + changeSize).coerceAtLeast(paneState.expandedSizeMin)
}


/**
 * Function that displays a SysMDViewModel.
 * It visualizes:
 * - A treeview for navigation in different hierarchies, selectable by tabs (left)
 * - A main editor window with tabs for each file.
 */
@Composable
fun SysMDView(
    sysMDViewModel: SysMDViewModel
) {
    // State of the left resizable pane
    val leftPaneState = remember { PaneState(true) }
    val leftPanelAnimatedSize = paneSizeAnimation(leftPaneState)

    // State of the right resizable pane
    val rightPaneState = remember { PaneState(false) }
    val rightPaneAnimatedSize = paneSizeAnimation(rightPaneState)

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
                    leftPaneState.splitter, onResizeLeft = { checkPanelResize(leftPaneState, it) },
                    /* Negating the Dp value in 'it' is necessary, as vertical splitter resizing is hard coded
                       depending on the drag direction */
                    rightPaneState.splitter, onResizeRight = { checkPanelResize(rightPaneState, -it) }
                ) {
                    if (sysMDViewModel.showDialogProjectAlreadyExits.value)
                        messageProjectAlreadyExits(sysMDViewModel.showDialogProjectAlreadyExits)

                    // The navigation panel
                    ResizablePane(
                        sysMDViewModel.boardIsEmpty,
                        ResizablePanelSide.LEFT_SIDE,
                        Modifier.width(leftPanelAnimatedSize).fillMaxHeight(),
                        leftPaneState
                    ) {
                        NavigationPane(sysMDViewModel)
                    }

                    // The editor tabs (=all open files) and the active editor (active tab).
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            EditorTabs(sysMDViewModel.editorTabsViewModel)
                            Box(Modifier.weight(1f).fillMaxSize()) {
                                if (sysMDViewModel.editorTabsViewModel.selectedCellList != null) {
                                    (sysMDViewModel.editorTabsViewModel.selectedCellList!!).scrollState = editorTabListState
                                    CellList(sysMDViewModel.editorTabsViewModel.selectedCellList!!)
                                }
                            }
                        }
                    }

                    // The pane with the board, right
                    ResizablePane(
                        sysMDViewModel.boardIsEmpty,
                        ResizablePanelSide.RIGHT_SIDE,
                        Modifier.width(rightPaneAnimatedSize).fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background),
                        rightPaneState
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
                                Board(sysMDViewModel.boardViewModel, sysMDViewModel.editorTabsViewModel)
                            }
                        }
                    }
                }
            }
        }
        // Here, we could add some drag and drop functionality
        // DropBox(window, model.tabsModel::create)
    }
}
