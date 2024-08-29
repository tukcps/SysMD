@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.composables.TooltipInstant
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.VerticalScrollbar
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TabModel

enum class MoveRequest { Up, Down }

@Composable
fun DisplayBody(model: TabModel) {
    if (model is EditorTabModel) {
        ElementList(model)
    } else {
        //model is a DisplayTabModel
        // graphView(model as DisplayTabModel)
    }
}

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
fun ElementList(
    editorTabModel: EditorTabModel,
) {
    // The Dialog to confirm deletion of an element;
    // this models the state (open/not open) of the dialog.
    val confirmDeleteShown = remember { mutableStateOf(false) }

    // The index in the list of elements for iteration.
    val idx = remember { mutableStateOf(0) }

    // If the ElementList scrolling is enabled/disabled (disabling can happen when the suggestion dropDown Menu is active)
    val enableElementListScrolling = remember { mutableStateOf(true) }

    val sysMDViewModel = remember { mutableStateOf(SysMDViewModel(session = editorTabModel.kerMlModel.value)) }

    /** Action: Delete a single element; opens confirm dialog */
    fun onDeleteRequest(index: Int) {
        idx.value = index
        confirmDeleteShown.value = true
        editorTabModel.generateTableOfContents()
    }

    /** Optional dialog for confirmation of deletion */
    if (confirmDeleteShown.value) AlertDialog(
        title = { Text("Delete Cell?") },
        text = { Text("Delete the selected cell?") },
        onDismissRequest = { confirmDeleteShown.value = false },
        confirmButton = {
            Button(onClick = {
                if (editorTabModel.cells.size > 0 && idx.value == 0)
                    editorTabModel.cells.removeAt(0)
                else
                    if (editorTabModel.cells.size >= 1 && idx.value in editorTabModel.cells.indices)
                        editorTabModel.cells.removeAt(idx.value)
                confirmDeleteShown.value = false
            }) { Text("Delete cell") }
        },
        dismissButton = { Button(onClick = { confirmDeleteShown.value = false }) { Text("Cancel") } },
        icon = {
            androidx.compose.material.Icon(
                Icons.Default.Warning,
                contentDescription = "Deleting a cell cannot be undone",
                tint = AppTheme.colors.iconRed,
                modifier = Modifier.size(60.dp)
            )
        },
    )

    /** The list with elements */
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxHeight()) {
        Column {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = editorTabModel.scrollState,
                userScrollEnabled = enableElementListScrolling.value
            ) {
                items(editorTabModel.cells.size) { index ->
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Element(
                            model = editorTabModel.cells[index],
                            index = index,
                            selectedIndex = editorTabModel.selectedIndex,
                            selectedItem = editorTabModel.editState,
                            showInfo = editorTabModel.showInfo[index] ?: mutableStateOf(true),
                            collapsedElementIds = editorTabModel.collapsedElementIds,
                            hiddenElementIds = editorTabModel.hiddenElementIds,
                            onDeleteRequest = ::onDeleteRequest,
                            elementEdited = editorTabModel.elementEdited,
                            onMoveRequest = editorTabModel::onMoveRequest,
                            internalRefReference = editorTabModel.references,
                            enableElementListScrolling = enableElementListScrolling,
                            sysMDViewModel = sysMDViewModel
                        )

                        if (index != editorTabModel.cells.size - 1) {
                            TooltipInstant(
                                tooltipText = "Click empty space between two cells to insert a new cell between them",
                                { editorTabModel.onAddRequest(index + 1) }) {
                                Row(// whole row ; double click adds a new element.
                                    modifier = Modifier
                                        .pointerInput({ editorTabModel.onAddRequest(index + 1) }) {
                                            detectTapGestures(onDoubleTap = { editorTabModel.onAddRequest(index + 1) })
                                        }

                                        .height(18.dp).fillMaxWidth().padding(1.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        modifier = Modifier.absoluteOffset(x = 10.dp),
                                        imageVector = Icons.Default.AddCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        } else {
                            TooltipInstant(
                                tooltipText = "Click to add a cell at the end",
                                { editorTabModel.onAddRequest(index + 1) }) {
                                Row(// whole row ; double click adds a new element.
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
