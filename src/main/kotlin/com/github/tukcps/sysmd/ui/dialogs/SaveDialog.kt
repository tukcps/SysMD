@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.AppTheme

/**
 * Dialog for either saving or dropping some artifact.
 * @param showSaveDialog State to switch dialog on/off.
 * @param itemName Name of the item that will be deleted.
 * @param onDrop lambda that is executed when 'Drop' is selected.
 * @param onSave lambda that is executed when 'Save' is selected.
 */
@Composable
fun SaveDialog(
    showSaveDialog: MutableState<Boolean>,
    itemName: String = "an item",
    onSave: () -> Unit,
    onDrop: () -> Unit = {},
) {
    if (showSaveDialog.value) {
        AlertDialog(
            onDismissRequest = { showSaveDialog.value = false },
            title = {
                Text(
                    text = "Unsaved Changes",
                    style = MaterialTheme.typography.headlineMedium, // Significantly larger title
                    fontWeight = FontWeight.Bold, // Bold title
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "There are unsaved changes in:",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    // Item name separated into its own row
                    Text(
                        text = itemName,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Text(
                        text = "Save all changes?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Unsaved Changes",
                    tint = AppTheme.colors.iconRed,
                    modifier = Modifier.size(50.dp)
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Drop Changes Button
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .maxSize(maxWidth = 90.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = { onDrop(); showSaveDialog.value = false }
                    ) {
                        Text("Drop Changes", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Save Changes Button
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .maxSize(maxWidth = 90.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                        onClick = { onSave(); showSaveDialog.value = false }
                    ) {
                        Text("Save Changes", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            dismissButton = null
        )
    }
}
