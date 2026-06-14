package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.CellListViewModel


@Composable
fun DeleteCellDialog(
    showConfirmDelete: MutableState<Boolean>,
    editorTabModel: CellListViewModel,
    idx: MutableState<Int>,
){
    if (showConfirmDelete.value) {
        AlertDialog(
            title = { Text("Delete Cell?") },
            text = { Text("Delete the selected cell?") },
            onDismissRequest = { showConfirmDelete.value = false },
            confirmButton = {
                TextButton(
                    border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                    onClick = {
                        if (editorTabModel.cells.isNotEmpty() && idx.value == 0)
                            editorTabModel.cells.removeAt(0)
                        else
                            if (editorTabModel.cells.isNotEmpty() && idx.value in editorTabModel.cells.indices)
                                editorTabModel.cells.removeAt(idx.value)
                        showConfirmDelete.value = false
                    }) { Text("Delete cell") }
            },
            dismissButton = {
                TextButton(
                    border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                    onClick = {
                        showConfirmDelete.value = false
                    }
                ) { Text("Cancel") }
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Deleting a cell cannot be undone",
                    tint = AppTheme.colors.iconRed,
                    modifier = Modifier.size(60.dp)
                )
            },
        )
    }
}