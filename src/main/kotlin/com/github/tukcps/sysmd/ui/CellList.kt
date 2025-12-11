@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.composables.TooltipInstant
import com.github.tukcps.sysmd.ui.dialogs.DeleteCellDialog
import com.github.tukcps.sysmd.ui.styles.VerticalScrollbar
import com.github.tukcps.sysmd.ui.viewmodel.TabViewModel

enum class MoveRequest { Up, Down }


/**
 * Displays a lazy list of cells in a lazy column.
 * Typically, the list of cells shows the different contents of a SysMD file that can be:
 * - a YAML header that specifies context information on the project
 * - plain vanilla Markdown text
 * - embedded code cells in different languages
 * Note that we break the Markdown text into different cells
 * @param editorTabModel the view model of the displayed cell list
 */
@Composable
fun CellList(
    editorTabModel: TabViewModel,
) {

    LaunchedEffect (scrollToItem.value) {
        referenceModel.editorTabModel?.scrollState?.animateScrollToItem(scrollToItem.value, 0)
    }

    // The Dialog to confirm deletion of an element;
    // this models the state (open/not open) of the dialog.
    val showConfirmDelete = remember { mutableStateOf(false) }

    // The index in the list of elements for iteration.
    val index = remember { mutableStateOf(0) }

    // If the ElementList scrolling is enabled/disabled (disabling can happen when the suggestion dropDown Menu is active)
    val enableElementListScrolling = remember { mutableStateOf(true) }

    /** Action: Delete a single element; opens confirm dialog */
    fun onDeleteRequest(i: Int) {
        index.value = i
        showConfirmDelete.value = true
        editorTabModel.generateTableOfContents()
    }

    /** Optional dialog for confirmation of deletion */
    if (showConfirmDelete.value)
        DeleteCellDialog(showConfirmDelete, editorTabModel, index)

    /** The list with cells */
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxHeight()) {
        Column {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = editorTabModel.scrollState,
                userScrollEnabled = enableElementListScrolling.value
            ) {
                items(editorTabModel.cells.size) { index ->
                    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                        TooltipInstant(
                            tooltipText = "Double-click empty space between two cells to insert a new cell between them",
                            { editorTabModel.onAddRequest(index) }) {
                            Row(// whole row; double click adds a new element.
                                modifier = Modifier
                                    .pointerInput({ editorTabModel.onAddRequest(index) }) {
                                        detectTapGestures(onDoubleTap = { editorTabModel.onAddRequest(index) })
                                    }
                                    .height(18.dp).fillMaxWidth().padding(1.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    modifier = Modifier.absoluteOffset(x = 15.dp),
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        Cell(
                            model = editorTabModel.cells[index],
                            index = index,
                            selectedIndex = editorTabModel.selectedIndex,
                            selectedItem = editorTabModel.editState,
                            collapsedElementIds = editorTabModel.collapsedCellIndices,
                            hiddenElementIds = editorTabModel.hiddenElementIds,
                            onDeleteRequest = ::onDeleteRequest,
                            cellWasChanged = editorTabModel.elementEdited,
                            onMoveRequest = editorTabModel::onMoveRequest,
                            internalRefReference = editorTabModel.references,
                            enableElementListScrolling = enableElementListScrolling,
                        )

                        /* Add after the end */
                        if (index == editorTabModel.cells.size - 1) {
                            TooltipInstant(
                                tooltipText = "Click to add a cell at the end",
                                { editorTabModel.onAddRequest(index + 1) }) {
                                Row(// whole row; double click adds a new element.
                                    modifier = Modifier
                                        .pointerInput({ editorTabModel.onAddRequest(index + 1) }) {
                                            detectTapGestures(onDoubleTap = { editorTabModel.onAddRequest(index + 1) })
                                        }
                                        .height(18.dp).fillMaxWidth().padding(1.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        //scrollbar on the right side
        VerticalScrollbar(Modifier.align(Alignment.CenterEnd), editorTabModel.scrollState)
    }
}
