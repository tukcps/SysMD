@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabModel
import com.github.tukcps.sysmd.ui.viewmodel.ProjectViewModel
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import java.io.File


@Composable
fun NewCommitDialog(agilaViewModel: SysMDViewModel) {

    var chosenCommitName by agilaViewModel.chosenCommitName
    var chosenCommitDescription by agilaViewModel.chosenCommitDescription

    val editorTab =  agilaViewModel.tabsModel.active

    val confirmCreatedCommit = remember { mutableStateOf(false) }
    val confirmNoChangesToCommit = remember { mutableStateOf(false) }
    val confirmEmptyNameOfCommit = remember { mutableStateOf(false) }

    /** Optional dialog for confirmation of commit */
    ConfirmInfoDialog(
        showCommitConfirmDialog = confirmCreatedCommit.value,
        text = "The commit \"$chosenCommitName.md\" was successfully created\n Please Reload the App",
        onCloseRequest = {
            confirmCreatedCommit.value = false
            isCommitDialogOpen.value = false
        }
    )

    /** dialog for confirmation of No changes in the file */
    ConfirmAlertDialog(
        showCommitAlertDialog = confirmNoChangesToCommit.value,
        text = "Please make sure there are changes in the file.",
        onCloseRequest = {
            confirmNoChangesToCommit.value = false
            isCommitDialogOpen.value = false
            agilaViewModel.chosenCommitName.value = ""
            agilaViewModel.chosenCommitDescription.value = ""
        },
    )

    /**  dialog for confirmation of empty Commit name */
    ConfirmAlertDialog(
        showCommitAlertDialog = confirmEmptyNameOfCommit.value,
        text = "Please make sure to enter a commit name",
        onCloseRequest = {
            confirmEmptyNameOfCommit.value = false
            agilaViewModel.chosenCommitName.value = ""
            agilaViewModel.chosenCommitDescription.value = ""
        },
    )

    /** Validate and make a commit */
    fun validateCommitCreation(){
        if (chosenCommitName.isNotEmpty()) {
            editorTab?.setUpCommit(chosenCommitName,chosenCommitDescription)
            editorTab?.checkBeforeCommit(editorTab  as EditorTabModel)
            if (editorTab?.doPostCommit?.value == false) {
                confirmNoChangesToCommit.value = true
            } else {
                agilaViewModel.createCommit(chosenCommitName, chosenCommitDescription)
                println("Successfully : $chosenCommitName.md + $chosenCommitDescription ")
                agilaViewModel.refreshTrees()

                confirmCreatedCommit.value = true
                // TODO(we need some how to reload the app to load data from Data base
                //  So the task is to automate the update of the screen //  the refresh in the trees rendering)
                agilaViewModel.chosenCommitName.value = " "
                agilaViewModel.chosenCommitDescription.value = " "
            }
        } else{
            confirmEmptyNameOfCommit.value = true
        }
    }

    DialogWindow(onCloseRequest = {
    isCommitDialogOpen.value = false
        agilaViewModel.chosenCommitName.value = ""
        agilaViewModel.chosenCommitDescription.value = ""
    },
        state = rememberDialogState(position = WindowPosition(Alignment.Center)),
        title = "Create a new Commit ",
        resizable = false) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(all = 5.dp)
            ) {

                Text(
                    "Enter the name of the commit.",
                    style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
                )
                Text(
                    text = "Commit Name : ",
                    style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
                )
                TextField(
                    value = chosenCommitName,
                    placeholder = { Text(text = "Enter commit name") },
                    leadingIcon = { Icon(Icons.Default.Create, null) },
                    onValueChange = { chosenCommitName = it; },
                    singleLine = true,
                    textStyle = TextStyle(lineHeight = 24.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Commit Description : ",
                    style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
                )
                TextField(
                    value = chosenCommitDescription,
                    placeholder = { Text(text = "Enter a commit description ") },
                    leadingIcon = { Icon(Icons.Default.Create, null) },
                    onValueChange = { chosenCommitDescription = it; },
                    singleLine = true,
                    textStyle = TextStyle(lineHeight = 24.sp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    Modifier.fillMaxHeight().fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            validateCommitCreation()
                        }) {
                        Text(
                            "Commit ",
                            style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
                        )
                    }
                    Button(
                        onClick = {
                            isCommitDialogOpen.value = false
                            agilaViewModel.chosenCommitName.value = ""
                            agilaViewModel.chosenCommitDescription.value = ""
                        }) {

                        Text(
                            "Cancel",
                            style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
                        )
                    }
                }
            }
        }
}

@Composable
fun NewProjectDialog(
    projectViewModel: ProjectViewModel
) {
    var projectNameBlank by remember { mutableStateOf(false) }
    var projectExists by remember { mutableStateOf(false) }

    /**  dialog for confirmation of empty Commit name */
    ConfirmAlertDialog(
        showCommitAlertDialog = projectNameBlank,
        text = "A non-blank project name is required.",
        onCloseRequest = { projectNameBlank = false },
    )

    ConfirmAlertDialog(
        showCommitAlertDialog = projectExists,
        text = "A project with the same name exists; choose a different name.",
        onCloseRequest = { projectExists = false },
    )

    /** Validate and make a commit */
    fun validateProjectCreation(){
        if (projectViewModel.name.isBlank()) {
            projectNameBlank = true
        } else if (File(settings.dataFolder+"/${projectViewModel.name}").exists()){
            projectExists = true
        } else {
            projectViewModel.createProject()
            projectViewModel.showDialog = false
            projectViewModel.name = ""
            projectViewModel.description = ""
        }
    }

    /** The dialog itself */
    DialogWindow(
        onCloseRequest = { projectViewModel.showDialog = false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center)),
        title = "Create a New Project",
        resizable = false) {
        Column(modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxHeight().padding(all = 5.dp)) {
            Text(text = "Project name:")
            TextField(
                value = projectViewModel.name,
                placeholder = { Text(text = "Enter project name") },
                leadingIcon = { Icon(Icons.Default.Create, null) },
                onValueChange = { projectViewModel.name = it; },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(text = "Project description:")
            TextField(
                value = projectViewModel.description,
                placeholder = { Text(text = "Enter a project description ") },
                leadingIcon = { Icon(Icons.Default.Create, null) },
                maxLines = 3,
                onValueChange = { projectViewModel.description = it; },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                Modifier.fillMaxHeight().fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { validateProjectCreation() }) {
                    Text("Create ", style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize))
                }
                Button(onClick = { projectViewModel.showDialog = false }) {
                    Text("Cancel", style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize))
                }
            }
        }
    }
}

@Composable
fun messageProjectAlreadyExits(showDialog:MutableState<Boolean>){
    ConfirmAlertDialog(
        showCommitAlertDialog = showDialog.value,
        text = "A Project with that name already exists, use a different name.",
        onCloseRequest = { showDialog.value = false },
    )
}