package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ChangeProjectIconDialog(
    showDialog: MutableState<Boolean>,
    hasExistingIcon: Boolean,
    onIconSelected: (ByteArray, String) -> Unit,
    onIconRemoved: () -> Unit
) {
    if (showDialog.value) {
        var selectedFileName by remember { mutableStateOf<String?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isFileSelected by remember { mutableStateOf(false) }
        var pendingBytes by remember { mutableStateOf<ByteArray?>(null) }
        var showRemoveConfirm by remember { mutableStateOf(false) }

        // Remove confirmation sub-dialog
        if (showRemoveConfirm) {
            AlertDialog(
                onDismissRequest = { showRemoveConfirm = false },
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Remove Project Icon?") },
                text = {
                    Text(
                        "The project icon will be removed and the default folder icon will be shown instead.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onIconRemoved()
                            showRemoveConfirm = false
                            showDialog.value = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Remove", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showRemoveConfirm = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Change Project Icon",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Info banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "PNG format required",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Only .png files are accepted. Recommended size: 256×256px or larger.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // File picker button
                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Select PNG Icon")
                            fileDialog.filenameFilter = java.io.FilenameFilter { _, name ->
                                name.lowercase().endsWith(".png")
                            }
                            fileDialog.isVisible = true

                            val fileName = fileDialog.file
                            val directory = fileDialog.directory

                            when {
                                fileName == null -> { /* cancelled */
                                }

                                !fileName.lowercase().endsWith(".png") -> {
                                    errorMessage = "\"$fileName\" is not a PNG file. Please select a valid .png image."
                                    selectedFileName = null
                                    isFileSelected = false
                                    pendingBytes = null
                                }

                                else -> {
                                    val file = java.io.File(directory, fileName)
                                    when {
                                        !file.exists() -> errorMessage = "File not found. Please try again."
                                        file.length() == 0L -> errorMessage =
                                            "The selected file is empty. Please choose a valid PNG."

                                        else -> {
                                            pendingBytes = file.readBytes()
                                            selectedFileName = file.name
                                            isFileSelected = true
                                            errorMessage = null
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.5.dp,
                            color = if (isFileSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isFileSelected) Icons.Default.CheckCircle else Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = if (isFileSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                            Text(
                                text = if (isFileSelected) "Change file…" else "Choose PNG File…",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Selected file chip
                    AnimatedVisibility(
                        visible = isFileSelected && selectedFileName != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        selectedFileName?.let { name ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Remove current icon — only shown when an icon.png already exists
                    AnimatedVisibility(
                        visible = hasExistingIcon,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Remove current icon",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Revert to the default folder icon",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { showRemoveConfirm = true },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove icon",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Remove",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    // Error message
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        errorMessage?.let { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bytes = pendingBytes
                        val name = selectedFileName
                        if (bytes != null && name != null) {
                            onIconSelected(bytes, name)
                            showDialog.value = false
                        }
                    },
                    enabled = isFileSelected && pendingBytes != null,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply Icon", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog.value = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}