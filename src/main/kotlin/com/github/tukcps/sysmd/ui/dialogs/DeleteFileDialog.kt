package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
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


/**
 * Dialog for either saving or dropping some artifact.
 * @param showDialog State to switch dialog on/off
 * @param onDelete lambda that is executed when 'Delete' is selected.
 */
@Composable
fun DeleteFileDialog(
    showDialog: MutableState<Boolean>,
    onDelete: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text("Remove File") },
        text = { Text(text = "This will remove the file from the project.") },
        dismissButton = {
            TextButton(
                modifier = Modifier.padding(horizontal = 32.dp),
                border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                onClick = { showDialog.value = false }
            ) { Text(" Cancel ") } },
        confirmButton = {
            TextButton(
                modifier = Modifier.padding(horizontal = 32.dp),
                border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                onClick = { onDelete(); showDialog.value = false }
            ) { Text("Remove") } },
        icon = { Icon(Icons.Default.Warning,
            contentDescription = "Warning",
            tint = AppTheme.colors.iconRed,
            modifier = Modifier.size(60.dp))},
    )
}