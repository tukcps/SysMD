@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.paneleft.projectlist

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondary
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.ui.dialogs.ChangeProjectIconDialog
import com.github.tukcps.sysmd.ui.dialogs.DeleteFileDialog
import com.github.tukcps.sysmd.ui.dialogs.ProjectDetailDialog
import com.github.tukcps.sysmd.ui.dialogs.SaveDialog
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.math.abs
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/**
 * Renders a single project with collapsible details
 * @param projectViewModel The project viewmodel that is rendered.
 * @param projectListViewModel The owning project list view-model.
 */
@OptIn(ExperimentalResourceApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Project(
    projectViewModel: ProjectViewModel,
    projectListViewModel: ProjectListViewModel,
) {
    val showProjectDetailDialog = remember { mutableStateOf(false) }
    val showDeleteProjectDialog = remember { mutableStateOf(false) }
    val showContextMenu = remember { mutableStateOf(false) }
    val showSaveProjectDialog = remember { mutableStateOf(false) }

    val contextMenuOffset = remember { mutableStateOf(IntOffset.Zero) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (projectViewModel.isExpanded.value) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    /** Shows a dialog before deleting a project, and if deleted, resets the (then invalid) session. */
    SaveDialog(projectViewModel.showSaveProjectDialog,
        itemName = "Project '${projectViewModel.name}' and/or its files",
        onSave = { projectViewModel.saveProjectToRepository(); projectViewModel.closeProjectSession() },
        onDrop = { projectViewModel.closeProjectSession(); projectViewModel.loadProjectFromRepository() },
    )

    /** Shows dialog on deleting a file */
    DeleteFileDialog(projectViewModel.showDeleteFileDialog,
        fileToDelete = projectListViewModel.selectedProjectState.value?.fileToDelete?.value?:"",
        onDelete = projectViewModel::deleteFileFromProject
    )

    /** Shows the Save-Dialog that changes the project after save. */
    SaveDialog(showSaveProjectDialog,
        itemName = "Project '${projectViewModel.name} and/or its files",
        onSave = { projectViewModel.saveProjectToRepository() },
        onDrop = { projectViewModel.createProjectSession() }
    )

    /** Shows Dialog with project information. */
    ProjectDetailDialog(showDialog = showProjectDetailDialog, projectViewModel = projectViewModel)

    /** Alert before deleting the project. */
    DeleteProjectDialog(showDialog = showDeleteProjectDialog, onDelete = { projectListViewModel.onDeleteProject(projectViewModel) })

    /** Shows dialog for changing the project icon */
    ChangeProjectIconDialog(showDialog =projectViewModel.showChangeIconDialog,
        hasExistingIcon = sessionService.getFile(projectViewModel.sessionIdState.value, "icon.png") != null,
        onIconSelected = { bytes, _ -> projectViewModel.updateProjectIcon(bytes) },
        onIconRemoved  = { projectViewModel.deleteProjectIcon() }
    )


    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .onPointerEvent(PointerEventType.Press) { event ->
                    if (event.button?.isSecondary == true) {
                        // Check if any change was consumed (by a child element)
                        if (!event.changes.any { it.isConsumed }) {
                            val position = event.changes.first().position
                            contextMenuOffset.value = IntOffset(position.x.toInt(), position.y.toInt())
                            showContextMenu.value = true
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
        ) {
            Column {
                // Header - always visible
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { projectListViewModel.onOpenProject(projectViewModel) }
                        .padding(all = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    val icon: ImageBitmap? = try {
                        sessionService.getIcon(projectViewModel.project?.id?.toKotlinUuid() ?: Uuid.NIL)
                            ?.decodeToImageBitmap()
                    } catch (e: Exception) {
                        logger.info("No 'icon.png' in the folder 'Files' of project '${projectViewModel.name}', using default icon")
                        null
                    }

                    if (icon != null) {
                        Image(
                            modifier = Modifier.size(40.dp),
                            painter = BitmapPainter(image = icon),
                            contentScale = ContentScale.Fit,
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(40.dp),
                            imageVector = if (projectViewModel.isExpanded.value)
                                Icons.Default.FolderOpen
                            else
                                Icons.Default.Folder,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null,
                        )
                    }

                    Column(modifier = Modifier.padding(start = 5.dp).weight(1f)) {
                        Text(
                            minLines = 1,
                            maxLines = 1,
                            text = projectViewModel.name,
                            style = MaterialTheme.typography.labelMedium,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (!projectViewModel.isExpanded.value) {
                            Text(
                                text = projectViewModel.description,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium)
                            )
                        }
                    }

                    // Expand/Collapse icon
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (projectViewModel.isExpanded.value) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle)
                    )
                }

                // Expanded content
                AnimatedVisibility(
                    visible = projectViewModel.isExpanded.value,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 49.dp, end = 8.dp, bottom = 8.dp, top = 4.dp)
                    ) {
                        // Markdown Files
                        Column(modifier = Modifier.fillMaxWidth()) {
                            projectViewModel.filesState.forEachIndexed { index, item ->
                                FileItem(
                                    index = index,
                                    projectViewModel = projectViewModel,
                                    onOpenFile = { projectViewModel.showTab(item) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // The menu after double- or right-click on the project.
        if (showContextMenu.value) {
            Popup(
                offset = contextMenuOffset.value,
                onDismissRequest = { showContextMenu.value = false }
            ) {
                Surface(
                    modifier = Modifier.width(200.dp),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ContextMenuItemWithIcon(
                            text = "Open Project Session",
                            icon = Icons.Default.FolderOpen,
                            onClick = {
                                if (projectViewModel.unsavedChangesExist()) projectViewModel.showSaveProjectDialog.value = true
                                else projectViewModel.createProjectSession()
                                showContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "Close Project Session",
                            icon = Icons.Default.FolderOff,
                            onClick = {
                                if (projectViewModel.unsavedChangesExist()) projectViewModel.showSaveProjectDialog.value = true
                                else projectViewModel.closeProjectSession()
                                showContextMenu.value = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        ContextMenuItemWithIcon(
                            text = "Show Project Info",
                            icon = Icons.Default.Info,
                            onClick = {
                                showProjectDetailDialog.value = true
                                showContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "Edit Project Info",
                            icon = Icons.Default.Edit,
                            onClick = {
                                projectListViewModel.projectToUpdate.value = projectViewModel
                                projectListViewModel.showNewProjectDialog.value = true
                                showContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "Change Project Icon",
                            icon = Icons.Default.Image,
                            onClick = {
                                projectViewModel.showChangeIconDialog.value = true
                                showContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "Add File",
                            icon = Icons.Default.Add,
                            onClick = {
                                projectViewModel.createFileInProject()
                                showContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "Open in Browser",
                            icon = Icons.Default.Folder,
                            onClick = {
                                projectViewModel.openContainingFolder()
                                showContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "Delete Project",
                            icon = Icons.Default.DeleteOutline,
                            onClick = {
                                showDeleteProjectDialog.value = true
                                showContextMenu.value = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable that renders a File name from the project view model.
 * The file name can be changed, the file can be deleted.
 * In current version, these actions on file items can only be done if a project is opened.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FileItem(
    index: Int,
    projectViewModel: ProjectViewModel,
    onOpenFile: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var editedFileName by remember { mutableStateOf(projectViewModel.filesState[index.coerceIn(0, projectViewModel.filesState.lastIndex)]) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val showFileContextMenu = remember { mutableStateOf(false) }
    val fileContextMenuOffset = remember { mutableStateOf(IntOffset.Zero) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isEditMode) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime < 400) {
                        // Double click detected
                        isEditMode = true
                        editedFileName = projectViewModel.filesState[index]
                    } else {
                        onOpenFile()
                    }
                    lastClickTime = currentTime
                }
                .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                .onPointerEvent(PointerEventType.Exit)  { isHovered = false }
                .onPointerEvent(PointerEventType.Press) { event ->
                    if (event.button?.isSecondary == true && !isEditMode) {
                        val position = event.changes.first().position
                        fileContextMenuOffset.value = IntOffset(position.x.toInt(), position.y.toInt())
                        showFileContextMenu.value = true
                        event.changes.forEach { it.consume() }
                    }
                }
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isHovered && !isEditMode)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium)
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (isEditMode) {
                // Edit mode: show text field
                OutlinedTextField(
                    value = editedFileName,
                    onValueChange = { editedFileName = it },
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                            projectViewModel.renameFileInProject(editedFileName, index); isEditMode = false
                        }
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Confirm button
                IconButton(
                    onClick = { projectViewModel.renameFileInProject(editedFileName, index); isEditMode = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm rename",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Cancel button
                IconButton(
                    onClick = {
                        editedFileName = projectViewModel.filesState[index]
                        isEditMode = false
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel rename",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Display only
                Row {
                    val selected = projectViewModel.editorTabsViewModel().selectedCellList?.nameState?.value
                    val edited = projectViewModel.editorTabsViewModel().editorTabs.getOrNull(index)?.elementEdited?.value
                    Text(
                        text = projectViewModel.filesState[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isHovered || selected == projectViewModel.filesState[index])
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        text = if (edited == true) "(edited)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (edited == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (showFileContextMenu.value) {
            Popup(
                offset = fileContextMenuOffset.value,
                onDismissRequest = { showFileContextMenu.value = false }
            ) {
                Surface(
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ContextMenuItemWithIcon(
                            text = "Open File",
                            icon = Icons.AutoMirrored.Filled.Launch,
                            onClick = {
                                onOpenFile()
                                showFileContextMenu.value = false
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        ContextMenuItemWithIcon(
                            text = "Rename File",
                            icon = Icons.Default.Edit,
                            onClick = {
                                isEditMode = true
                                projectViewModel.renameFileInProject(editedFileName, index)
                                showFileContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "Delete File",
                            icon = Icons.Default.Delete,
                            onClick = {
                                projectViewModel.fileToDelete.value = projectViewModel.filesState.getOrNull(index)
                                projectViewModel.showDeleteFileDialog.value = true
                                showFileContextMenu.value = false
                            }
                        )
                        /* Only make it appear if function is there
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        ContextMenuItemWithIcon(
                            text = "Pull",
                            icon = Icons.Default.Download,
                            onClick = {
                                // TODO: Implement pull functionality
                                showFileContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "Push",
                            icon = Icons.Default.Upload,
                            onClick = {
                                // TODO: Implement push functionality
                                showFileContextMenu.value = false
                            }
                        )
                        ContextMenuItemWithIcon(
                            text = "File History",
                            icon = Icons.Default.History,
                            onClick = {
                                // TODO: Implement push functionality
                                showFileContextMenu.value = false
                            }
                        ) */
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItemWithIcon(
    text: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp).padding(end = 4.dp),
            tint = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled)
        )
    }
}



/**
 * Converts a timestamp to a human-readable relative time string
 * @param timestamp The timestamp in milliseconds
 * @return A string like "just now", "5 minutes ago", "3 days ago", etc.
 */
private fun getRelativeTimeString(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = abs(now - timestamp)

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365

    return when {
        seconds < 60 -> "just now"
        minutes < 2 -> "1 minute ago"
        minutes < 60 -> "$minutes minutes ago"
        hours < 2 -> "1 hour ago"
        hours < 24 -> "$hours hours ago"
        days < 2 -> "yesterday"
        days < 7 -> "$days days ago"
        weeks < 2 -> "1 week ago"
        weeks < 4 -> "$weeks weeks ago"
        months < 2 -> "1 month ago"
        months < 12 -> "$months months ago"
        years < 2 -> "1 year ago"
        else -> "$years years ago"
    }
}