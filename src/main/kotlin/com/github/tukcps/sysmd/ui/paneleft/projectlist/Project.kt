@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.paneleft.projectlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.ui.composables.SysMDTooltipArea
import com.github.tukcps.sysmd.ui.dialogs.SaveDialog
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Renders a single project
 * @param projectViewModel The project viewmodel that is rendered.
 * @param projectListViewModel The owning project list view-model.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun Project(
    projectViewModel: ProjectViewModel,
    projectListViewModel: ProjectListViewModel,
) {

    if (projectViewModel.showSaveDialog.value) {
        SaveDialog(projectViewModel.showSaveDialog,
            onSave = { projectListViewModel.editorTabsViewModel.save(); projectViewModel.openProject(); projectViewModel.showSaveDialog.value = false },
            onDrop = { projectViewModel.openProject();  projectViewModel.showSaveDialog.value = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 5.dp)
            .selectable(selected = false) {
                if (projectViewModel.unsavedChangesExist()) projectViewModel.showSaveDialog.value = true
                else projectViewModel.openProject() },
        shape = RoundedCornerShape(12.dp),
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                val icon: ImageBitmap? = try {
                    val file = projectViewModel.project?.directory?.resolve("Files")?.resolve("icon.png")?.toFile()
                    file?.inputStream()?.readAllBytes()?.decodeToImageBitmap()
                } catch (e: Exception) {
                    logger.info("No 'icon.png' in the folder 'Files' of project ${projectViewModel.name}': $e")
                    null
                }

                if (icon != null) {
                    Image(
                        modifier = Modifier.size(40.dp),
                        painter = BitmapPainter(image = icon),
                        contentScale = ContentScale.Fit,
                        contentDescription = null
                    )
                } else
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.FilePresent,
                        contentDescription = null,
                    )

                Column(modifier = Modifier.padding(start = 5.dp).weight(1f)) {
                    Text(
                        minLines = 1,
                        maxLines = 1,
                        text = projectViewModel.name,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        maxLines = 2,
                        minLines = 2,
                        text = projectViewModel.description,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                Column(modifier = Modifier.padding(start = 5.dp)) {

                    IconButton(
                        onClick = {  projectListViewModel.projectToUpdate.value = projectViewModel; projectListViewModel.showNewProjectDialog.value = true },
                        modifier = Modifier.size(24.dp).padding(all = 5.dp)
                    ) {
                        SysMDTooltipArea(tooltipText = "Edit the project's data record") {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Project Data",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium),
                            )
                        }
                    }

                    val showDeleteProjectDialog = remember { mutableStateOf(false) }
                    if (showDeleteProjectDialog.value) DeleteProjectDialog(showDialog = showDeleteProjectDialog, onDelete = { projectListViewModel.deleteProject(projectViewModel)})
                    IconButton(
                        onClick = { if (projectViewModel!= projectViewModel.activeProject.value) showDeleteProjectDialog.value = true },
                        modifier = Modifier.size(24.dp).padding(all = 5.dp)
                    ) {
                        SysMDTooltipArea(tooltipText = "Delete project") {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Project",
                                tint =
                                    if (projectViewModel != projectViewModel.activeProject.value) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled),                                )
                        }
                    }
                }
            }
        }
    }
}