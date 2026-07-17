package com.github.tukcps.sysmd.ui.dialogs


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.ui.styles.AppTheme

/**
 * Dialog for deleting a file from the project.
 * @param showDialog State to switch dialog on/off.
 * @param fileToDelete Name of the file that will be deleted.
 * @param onDelete lambda that is executed when 'Delete' is selected.
 */
@Composable
fun DeleteFileDialog(
    showDialog: MutableState<Boolean>,
    fileToDelete: String,
    onDelete: () -> Unit = {},
) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = {
                Text(
                    text = "Delete File Alert",
                    style = MaterialTheme.typography.headlineMedium, // Large bold title
                    fontWeight = FontWeight.Bold,
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
                        text = "This will delete the file:",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    // File name separated into its own row
                    Text(
                        text = fileToDelete,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Text(
                        text = "Really?",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
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
                    // Cancel Button
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .maxSize(maxWidth = 90.dp), // 90.dp limit
                        shape = CircleShape, // Pill shape
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                        onClick = { showDialog.value = false }
                    ) {
                        Text("Cancel", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Delete Button
                    TextButton(
                        modifier = Modifier
                            .weight(1f)
                            .maxSize(maxWidth = 90.dp), // 90.dp limit
                        shape = CircleShape, // Pill shape
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = { onDelete(); showDialog.value = false }
                    ) {
                        // Shortened to fit the 90.dp width
                        Text("Delete", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            dismissButton = null
        )
    }
}

// Helper modifier to restrict the maximum width when using weight(1f)
fun Modifier.maxSize(maxWidth: Dp): Modifier = this.then(Modifier.widthIn(max = maxWidth))
