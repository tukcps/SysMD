@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.projectlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.AppTheme

@Composable
fun DeleteProjectDialog(
    showDialog: MutableState<Boolean>,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("Delete Project?") },
        text = { Text("Do you really want to delete the project? (The project can be restored by removing the extension .deleted manually)") },
        confirmButton = {
            TextButton(
                modifier = Modifier.fillMaxWidth(0.49F),
                border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                onClick = { showDialog.value = false; onDelete() }
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(
                modifier = Modifier.fillMaxWidth(0.49F),
                border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                onClick = { showDialog.value = false;  }
            ) { Text("Cancel") }
                        },
        icon = { Icon(Icons.Default.Warning,
            contentDescription = "Warning",
            tint = AppTheme.colors.iconRed,
            modifier = Modifier.size(60.dp))},
    )
}