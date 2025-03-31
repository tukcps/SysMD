@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui.projectlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.dialogs.ConfirmAlertDialog
import com.github.tukcps.sysmd.ui.styles.AppTheme
import java.io.File

/**
 * Adds a new project to the list of all projects.
 */
@Composable
fun UpdateProjectDialog(
    projectListViewModel: ProjectListViewModel
) {
    var projectNameBlank by remember { mutableStateOf(false) }
    var projectExists by remember { mutableStateOf(false) }

    /**  Dialog for confirmation of empty Commit name */
    ConfirmAlertDialog(
        showCommitAlertDialog = projectNameBlank,
        text = "Project name must consist of only letters, numbers, spaces, _, -.",
        onCloseRequest = { projectNameBlank = false },
    )

    ConfirmAlertDialog(
        showCommitAlertDialog = projectExists,
        text = "A project with this name already exists; choose a different name.",
        onCloseRequest = { projectExists = false },
    )

    /** We collect new data here and add/update after validation */
    val newProjectViewModel = projectListViewModel.projectToUpdate.value?.copy()?:ProjectViewModel(
        sessionState = projectListViewModel.sessionState,
        projectListViewModel.editorTabsViewModel,
        reset = projectListViewModel.reset,
        project = null,
        activeProject = projectListViewModel.projectOfSession
    )

    /** Validate and save if ok */
    fun validateAndUpdateProject(){
        when {
            !newProjectViewModel.name.all { it.isLetterOrDigit() || it in setOf(' ', '-') } || newProjectViewModel.name.isBlank()
                -> projectNameBlank = true

            // Create & file exists?
            projectListViewModel.projectToUpdate.value == null && File(settings.dataFolder+"/${newProjectViewModel.name}").exists()
                -> projectExists = true

            // OK -> create
            projectListViewModel.projectToUpdate.value == null -> {
                projectListViewModel.createProject(newProjectViewModel)
                projectListViewModel.showNewProjectDialog.value = false
            }

            // OK -> update
            else -> {
                projectListViewModel.showNewProjectDialog.value = false
                projectListViewModel.projectToUpdate.value?.updateProject(newProjectViewModel)
                projectListViewModel.projectToUpdate.value = null
            }
        }
    }

    /** The dialog itself */
    DialogWindow(
        onCloseRequest = { projectListViewModel.showNewProjectDialog.value = false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center), size = DpSize(600.dp, 400.dp)),
        title = "Create and/or update project data",
        resizable = false
    ) {
        Surface(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
            Column {
                Row {
                    Column(
                        modifier = Modifier
                            .padding(all = 24.dp)
                            .fillMaxHeight(0.7f)
                    ) {
                        TextField(
                            label = { Text("Project name") },
                            modifier = Modifier.scale(scale = 0.9F).fillMaxWidth(),
                            value = newProjectViewModel.name,
                            placeholder = { Text(text = "Enter project name") },
                            leadingIcon = { Icon(Icons.Default.Create, null) },
                            onValueChange = { newProjectViewModel.name = it },
                            singleLine = true,
                        )
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            label = { Text("Project description") },
                            modifier = Modifier.scale(scale = 0.9F).fillMaxWidth(),
                            value = newProjectViewModel.description,
                            placeholder = { Text(text = "Enter project description ") },
                            leadingIcon = { Icon(Icons.Default.Create, null) },
                            minLines = 3, maxLines = 3,
                            onValueChange = { newProjectViewModel.description = it; },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "You can add or remove files or update their names in the tab riders of an open project.")
                        // Icon here
                    }
                }
                // Last line: Update or drop changes.
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        modifier = Modifier.padding(end = 15.dp),
                        border = BorderStroke(1.dp, AppTheme.colors.iconRed),
                        onClick = { projectListViewModel.showNewProjectDialog.value = false }
                    ) { Text("Cancel") }
                    TextButton(
                        modifier = Modifier.padding(start = 15.dp, end = 20.dp),
                        border = BorderStroke(1.dp, AppTheme.colors.iconGreen),
                        onClick = { validateAndUpdateProject() }
                    ) { Text("Save") }
                }
            }
        }
    }
}