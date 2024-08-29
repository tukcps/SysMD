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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.agenda.AgendaView
import com.github.tukcps.sysmd.ui.composables.*
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.VerticalSplittable
import com.github.tukcps.sysmd.ui.viewmodel.AgilaCompositionTree
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel

/**
 * The main window with three sections:
 * - On top, a menu line with items to select actions.
 * - below, two columns:
 *   + A treeview for selection and navigation in hierarchy.
 *   + A notebook with tabs for editing and viewing packages.
 */

fun openDummy(n: TreeViewNodeModel) {
    try {
        if (n is AgilaCompositionTree)
            println(n.elem)
    } catch (e: Exception) {
        println(e)
    }
}

fun create(n: TreeViewNodeModel) = println("onCreate $n")

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
 * Function that displays an AgilaViewModel.
 * It visualizes:
 * - A treeview for navigation in different hierarchies, selectable by tabs (left)
 * - A main editor window with tabs for each file.
 */
@Composable
fun SysMDView(model: SysMDViewModel, window: ComposeWindow, isIndexing: MutableState<Float>) {
    // var dropDownMenuState by remember { mutableStateOf("Agenda") }

    val agilaViewModel = remember { model }

    // States of the window and its components.
    val editorTabsModel = agilaViewModel.tabsModel

    // State of the left resizable panel
    val leftPanelState = remember { PanelState(true) }

    // Animation for left panel
    val leftPanelAnimatedSize = panelSizeAnimation(leftPanelState)

    // State of the right resizable panel
    val rightPanelState = remember { PanelState(false) }

    // Animation for the right panel
    val rightPanelAnimatedSize = panelSizeAnimation(rightPanelState)

    /**
     * Controls the state of the tab list in editor
     */
    val editorTabListState: LazyListState = rememberLazyListState()

    // The SysMD Notebook View itself.
    Surface {
        Row {
            Column(Modifier.fillMaxWidth()) {

                //the menu line
                MenuLine(agilaViewModel)

                //TODO find more elegant and clean solution
                VerticalSplittable(Modifier.fillMaxSize(),
                    leftPanelState.splitter, onResizeLeft = { checkPanelResize(leftPanelState, it) },
                    /* Negating the Dp value in 'it' is necessary, as vertical splitter resizing is hard coded
                       depending on the drag direction */
                    rightPanelState.splitter, onResizeRight = { checkPanelResize(rightPanelState, -it) }
                ) {
                    // if ((agilaViewModel.showDialogFilename.value)||(menuState.createFileClicked.value))  NewFileDialog(agilaViewModel)
                    if (agilaViewModel.showDialogBranchName.value || menuState.createBranchClicked.value) NewBranchDialog(
                        agilaViewModel
                    )
                    if (agilaViewModel.showDialogBranchDeletion.value) DeleteBranchDialog(agilaViewModel)
                    if (agilaViewModel.showDialogProjectAlreadyExits.value) messageProjectAlreadyExits(agilaViewModel.showDialogProjectAlreadyExits)

                    // The navigation panel
                    ResizablePanel(
                        agilaViewModel.agendaIsEmpty,
                        ResizablePanelSide.LEFT_SIDE,
                        Modifier.width(leftPanelAnimatedSize).fillMaxHeight()
                        // .background(AppTheme.colors.backgroundLightGray)
                        , leftPanelState
                    ) {
                        NavigationPanel(agilaViewModel, isIndexing)
                    }

                        // The editor tabs (=all open files) and the active editor (active tab).
                        Box(Modifier.fillMaxSize()) {
                            if (editorTabsModel.active != null) {
                                Column(Modifier.fillMaxSize()) {
                                    EditorTabsView(editorTabsModel)
                                    Box(Modifier.weight(1f).fillMaxSize()) {
                                        if (editorTabsModel.active != null)
                                            (editorTabsModel.active!! as EditorTabModel).scrollState = editorTabListState
                                            DisplayBody(editorTabsModel.active!!)
                                    }
                                }
                            } else {
                                EditorEmptyView()
                            }
                        }

                    // The panel with the agenda
                    ResizablePanel(
                        agilaViewModel.agendaIsEmpty,
                        ResizablePanelSide.RIGHT_SIDE,
                        Modifier.width(rightPanelAnimatedSize).fillMaxHeight()
                            .background(MaterialTheme.colorScheme.background),
                        rightPanelState
                    ) {

                        Column {
                            Row(
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                //dropDownMenuState = DropDownMenu(agilaViewModel.agenda)
                                Text("Agenda", fontSize = AppTheme.fontSize, modifier = Modifier.padding(bottom = 5.dp))
                            }

                            //DropDownMenu()
                            Row {
                                //Row { TreeViewPlus(treeModel, isRefreshing.value) }
                                AgendaView(agilaViewModel.agenda, editorTabsModel.active)
                            }
                        }
                    }
                }
            }
        }
        DropBox(window, model.tabsModel::create)
    }
}
