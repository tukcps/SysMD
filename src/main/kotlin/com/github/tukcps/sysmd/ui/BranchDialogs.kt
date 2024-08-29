package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.github.tukcps.sysmd.rest.AgilaRepository
import com.github.tukcps.sysmd.ui.composables.menuState
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel


@Composable
fun NewBranchDialog(agilaViewModel: SysMDViewModel) {

    var chosenProjectName by agilaViewModel.chosenProjectName
    var chosenBranchName by agilaViewModel.chosenBranchName
    val projects = AgilaRepository.projectsState
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    var selectedBranchIndex by remember { mutableStateOf(0) }
    var branchesExpanded by remember { mutableStateOf(false) }

    chosenProjectName = projects[selectedIndex].name
    val branches = AgilaRepository.getBranches(projects[selectedIndex].id).toMutableList()

    val confirmCreatedProject= remember { mutableStateOf(false) }
    val confirmEmptyNameOfProject = remember { mutableStateOf(false) }

    /** Optional dialog for confirmation of commit */
    ConfirmInfoDialog(
        showCommitConfirmDialog = confirmCreatedProject.value,
        text = "The Branch \"$chosenBranchName\" was successfully created",
        onCloseRequest = {
            confirmCreatedProject.value = false
            agilaViewModel.showDialogProjectName.value = false
        }
    )

    /**  dialog for confirmation of empty Commit name */
    ConfirmAlertDialog(
        showCommitAlertDialog = confirmEmptyNameOfProject.value,
        text = "Please make sure to enter a Branch name",
        onCloseRequest = {
            confirmEmptyNameOfProject.value = false
            agilaViewModel.chosenProjectName.value = ""
            agilaViewModel.chosenCommitDescription.value = ""
        },
    )
//
    /** Validate and make a commit */
    fun validateBranchCreation() {
        if (chosenBranchName !== "") {
            agilaViewModel.createBranch(projects[selectedIndex],branches[selectedBranchIndex],chosenBranchName)
            if (agilaViewModel.showDialogBranchName.value || menuState.createBranchClicked.value) {
//                println("Successfully : $chosenProjectName + $chosenProjectDescription ")
//
                confirmCreatedProject.value = true
                agilaViewModel.refreshTrees()
                menuState.createBranchClicked.value = false
                agilaViewModel.showDialogBranchName.value = false
            }
        } else {
            confirmEmptyNameOfProject.value = true
        }
    }

    DialogWindow(onCloseRequest = {
        agilaViewModel.showDialogBranchName.value = false
        menuState.createBranchClicked.value = false
    },
        state = rememberDialogState(position = WindowPosition(Alignment.Center)),
        title = "Create a new Branch $chosenBranchName in $chosenProjectName ",
        resizable = false)
    {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(all = 5.dp)
        ) {
            Text(
                "Select Project to branch.",
                style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
            )
            Text(
                projects[selectedIndex].name,
                modifier = Modifier.fillMaxWidth().clickable(onClick = { expanded = true }).background(
                    MaterialTheme.colorScheme.background
                )
            )
            DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    projects.forEachIndexed { index, project ->
                        DropdownMenuItem(
                            { Text(text = project.name) },
                            onClick = { selectedIndex = index; expanded = false })
                    }
                }

                Text(
                    "Select branch to branch from.",
                    style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
                )
                Text(
                    branches[selectedBranchIndex].name,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = { branchesExpanded = true }).background(
                        MaterialTheme.colorScheme.background
                    )
                )
                DropdownMenu(
                    expanded = branchesExpanded,
                    onDismissRequest = { branchesExpanded = false }
                ) {
                    branches.forEachIndexed { index, project ->
                        DropdownMenuItem(
                            text ={ Text(text = project.name) },
                            onClick = { selectedBranchIndex = index; branchesExpanded = false })
                    }
                }

                Text("Enter the name of the Branch.",
                    style = TextStyle(lineHeight = 24.sp),
                )
                Text(
                    text = "Branch Name: ",
                    style = TextStyle(lineHeight = 24.sp),
                )
                TextField(
                    value = chosenBranchName,
                    placeholder = { Text(text = "Enter Branch name") },
                    leadingIcon = { Icon(Icons.Default.Create, null) },
                    onValueChange = { chosenBranchName = it; },
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
                            validateBranchCreation()
                        }) {
                        Text(
                            "Create ",
                            style = TextStyle(lineHeight = 24.sp),
                        )
                    }
                    Button(
                        onClick = {
                            agilaViewModel.showDialogBranchName.value = false
                            menuState.createBranchClicked.value = false
                            agilaViewModel.chosenProjectName.value = ""
                            agilaViewModel.chosenProjectDescription.value = ""
                            agilaViewModel.chosenBranchName.value = ""
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
fun DeleteBranchDialog(agilaViewModel: SysMDViewModel) {
    var chosenProjectName by agilaViewModel.chosenProjectName
    val chosenBranchName by agilaViewModel.chosenBranchName
    val projects = AgilaRepository.projectsState
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }
    var selectedBranchIndex by remember { mutableStateOf(0) }
    var branchesExpanded by remember { mutableStateOf(false) }

    chosenProjectName = projects[selectedIndex].name
    val branches = AgilaRepository.getBranches(projects[selectedIndex].id).toMutableList()

    // val confirmCreatedProject= remember { mutableStateOf(false) }
    // val confirmEmptyNameOfProject = remember { mutableStateOf(false) }

    DialogWindow(
        onCloseRequest = {
            agilaViewModel.showDialogBranchName.value = false
            menuState.createBranchClicked.value = false },
        state = rememberDialogState(position = WindowPosition(Alignment.Center)),
        title = "Delete Branch $chosenBranchName of $chosenProjectName ",
        resizable = false)
        {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(all = 5.dp)
            ) {
                Text(
                    "Select Project.",
                    style = TextStyle(lineHeight = 24.sp),
                )
                Text(
                    projects[selectedIndex].name,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = { expanded = true })
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    projects.forEachIndexed{ index, project ->
                        DropdownMenuItem(
                            text = { Text(text = project.name) },
                            onClick = { selectedIndex = index; expanded = false })
                    }
                }

                Text(
                    "Select branch to delete",
                    style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
                )
                Text(
                    branches[selectedBranchIndex].name,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = { branchesExpanded = true })
                )
                DropdownMenu(
                    expanded = branchesExpanded,
                    onDismissRequest = { branchesExpanded = false }
                ) {
                    branches.forEachIndexed { index, project ->
                        DropdownMenuItem(
                            text = { Text(text = project.name) },
                            onClick = { selectedBranchIndex = index; branchesExpanded = false })
                    }
                }

                Text("Enter the name of the Branch.", style = TextStyle(lineHeight = 24.sp))

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    Modifier.fillMaxHeight().fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            agilaViewModel.showDialogBranchDeletion.value = false
                            agilaViewModel.deleteBranch(projects[selectedIndex], branches[selectedBranchIndex])
                        }) {
                        Text(
                            "Delete",
                            style = TextStyle(lineHeight = 24.sp, fontSize = AppTheme.fontSize),
                        )
                    }
                    Button(
                        onClick = {
                            agilaViewModel.showDialogBranchDeletion.value = false
                            agilaViewModel.chosenProjectName.value = ""
                            agilaViewModel.chosenProjectDescription.value = ""
                            agilaViewModel.chosenBranchName.value = ""
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
