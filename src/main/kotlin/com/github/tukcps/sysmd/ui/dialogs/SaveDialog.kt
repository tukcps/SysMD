@file:Suppress("FunctionName")

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
 * @param showSaveDialog State to switch dialog on/off
 * @param onDrop lambda that is executed when 'Drop' is selected.
 * @param onSave lambda that is executed when 'Save' is selected.
 */
@Composable
fun SaveDialog(
    showSaveDialog: MutableState<Boolean>,
    onSave: () -> Unit,
    onDrop: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = { showSaveDialog.value = false },
        title = { Text("Unsaved Changes") },
        text = { Text(text = "There are unsaved changes. Save them?") },
        confirmButton = {
            TextButton(
                modifier = Modifier.padding(horizontal = 16.dp),
                border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                onClick = { onSave(); showSaveDialog.value = false }
            ) { Text("Save Changes") } },
        dismissButton = {
            TextButton(
                modifier = Modifier.padding(horizontal = 16.dp),
                border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                onClick = { onDrop(); showSaveDialog.value = false }
            )  { Text("Drop Changes") } },
        icon = { Icon(Icons.Default.Warning,
            contentDescription = "Unsaved modifications",
            tint = AppTheme.colors.iconRed,
            modifier = Modifier.size(60.dp))},
    )
}