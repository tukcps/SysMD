@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.tukcps.sysmd.ui.isDirectory
import com.github.tukcps.sysmd.ui.paneleft.projectlist.ProjectViewModel
import kotlinx.io.files.SystemFileSystem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog to display detailed information about a project
 * @param showDialog State to control dialog visibility
 * @param projectViewModel The project whose details are being displayed
 */
@Composable
fun ProjectDetailDialog(
    showDialog: MutableState<Boolean>,
    projectViewModel: ProjectViewModel
) {
    if (showDialog.value) {
        Dialog(onDismissRequest = { showDialog.value = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header with title and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Project Details",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showDialog.value = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // === BASIC INFORMATION SECTION ===
                        SectionHeader("Basic Information")

                        Spacer(modifier = Modifier.height(8.dp))

                        DetailItem(
                            label = "Project Name",
                            value = projectViewModel.name
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        DetailItem(
                            label = "Description",
                            value = projectViewModel.description.ifEmpty { "No description provided" }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // === STATUS SECTION ===
                        SectionHeader("Status")

                        Spacer(modifier = Modifier.height(8.dp))

                        val isActive = projectViewModel == projectViewModel.selectedProjectState.value
                        DetailItem(
                            label = "Project Status",
                            value = if (isActive) "Active" else "Inactive",
                            valueColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        if (isActive) {
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailItem(
                                label = "Unsaved Changes",
                                value = if (projectViewModel.unsavedChangesExistInFiles()) "Yes" else "No",
                                valueColor = if (projectViewModel.unsavedChangesExistInFiles())
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // === FILE INFORMATION SECTION ===
                        SectionHeader("File Information")

                        Spacer(modifier = Modifier.height(8.dp))

                        val nbrOfFiles = projectViewModel.project?.getIndexedFiles()?.size
                        DetailItem(
                            label = "Total Files",
                            value = nbrOfFiles?.toString() ?: "0"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val projectPath =
                            projectViewModel.project?.directory?.let { SystemFileSystem.resolve(it) }?.toString()
                                ?: "N/A"
                        DetailItem(
                            label = "Project Path",
                            value = projectPath
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // === TIMESTAMPS SECTION ===
                        SectionHeader("Timestamps")

                        Spacer(modifier = Modifier.height(8.dp))

                        val createdAt = projectViewModel.project?.created
                        if (createdAt != null) {
                            DetailItem(
                                label = "Created At",
                                value = createdAt.toString()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        val lastModified = try {
                            val directory = projectViewModel.project?.directory
                            if (directory?.isDirectory() == true) {
                                val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                                "N/A" // dateFormat.format(Date(directory.lastModified())) not possible with KMP :-(
                            } else "N/A"
                        } catch (_: Exception) {
                            "N/A"
                        }
                        DetailItem(
                            label = "Last Modified",
                            value = lastModified
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Close button at bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDialog.value = false }
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper composable for section headers
 */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.secondary
    )

}

/**
 * Helper composable for displaying a label-value pair
 */
@Composable
private fun DetailItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}
