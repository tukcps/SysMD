@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "FunctionName")

package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.github.tukcps.sysmd.ui.isCommitDialogOpen
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel


@Composable
fun NewCommitDialog(agilaViewModel: SysMDViewModel) {

    var chosenCommitName by agilaViewModel.chosenCommitName
    var chosenCommitDescription by agilaViewModel.chosenCommitDescription

    val editorTab =  agilaViewModel.editorTabsViewModel.active

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
            TODO()
            /*
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
            }*/
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
fun messageProjectAlreadyExits(showDialog:MutableState<Boolean>){
    ConfirmAlertDialog(
        showCommitAlertDialog = showDialog.value,
        text = "A Project with that name already exists, use a different name.",
        onCloseRequest = { showDialog.value = false },
    )
}