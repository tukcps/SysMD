package com.github.tukcps.sysmd.ui.composables

import androidx.compose.runtime.*


val StatusLabelText = mutableStateOf("Application Status")

fun generateStatusLabelText(){
//    when(AgilaRepository.ConnectionState.value) {
//        AgilaRepositoryConnectionState.OFFLINE ->
//            StatusLabelText.value = "Offline"
//        AgilaRepositoryConnectionState.CONNECTING ->
//            StatusLabelText.value = "Connecting"
//        AgilaRepositoryConnectionState.CONNECTED ->
//            StatusLabelText.value = "Connected to Backend"
//        AgilaRepositoryConnectionState.PROJECT_OPENING ->
//            StatusLabelText.value = "Opening Project"
//        AgilaRepositoryConnectionState.PROJECT_OPENED ->
//            StatusLabelText.value = "Project opened"
//    }
}


/**
 * Since Jetpack Compose currently has no Statusbar, this is an own build for that.
 */
//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//fun StatusBar(viewModel: AgilaViewModel, window:ComposeWindow) {
//    if(showCommitTree.value)
//        TreePreview()

//    var expanded by remember { mutableStateOf(false) }
//    generateStatusLabelText()
//    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundMediumGray)) {
//        Row {
//            Text(text = StatusLabelText.value, fontSize = AppTheme.fontSize)
//            Spacer(modifier = Modifier.width(30.dp))

//            if(AgilaRepository.ConnectionState.value==AgilaRepositoryConnectionState.CONNECTING)
//                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

//            if ((viewModel.tabsModel.active != null) && (viewModel.tabsModel.active is EditorTabModel) && ((viewModel.tabsModel.active as EditorTabModel).commitId != null)) {
//                val branches = AgilaRepository.getBranchesForProject((viewModel.tabsModel.active as EditorTabModel).owningProjectId!!)
//                val branchesStringArray = arrayListOf<String>()
//                var currentBranchIndex = 0
//                var currentBranchFound = false
//                for (branch in branches) {
//                    branchesStringArray.add(branch.name)
//                    currentBranchFound =
//                        currentBranchFound || (viewModel.tabsModel.active as EditorTabModel).branchId == branch.id
//                    if (!currentBranchFound)
//                        currentBranchIndex++
//                }
//                branchesStringArray.add("New Branch")
//                Box(modifier = Modifier.width(130.dp).wrapContentSize(Alignment.TopStart).clickable(onClick = { expanded = true })) {

//                    Row {
//                        val trailingSign = if(expanded) "▼" else "▲"
//                        Text(
//                            text = branchesStringArray[currentBranchIndex],
//                            fontSize = AppTheme.fontSize,
//                            modifier = Modifier.fillMaxSize(0.75f)
//                        )
//                        Text(
//                            text = trailingSign,
//                            fontSize = AppTheme.fontSize,
//                        )
//                    }
//                    DropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = { expanded = false },
//                    ) {
//                        branchesStringArray.forEachIndexed { index, s ->
//                            DropdownMenuItem(onClick = {
//                                if(index >= branches.size) {
//                                    viewModel.chosenProjectName.value =
//                                        (viewModel.tabsModel.active as EditorTabModel).projectName!!

//                                    for (branch in branches) {
//                                        if (branch.id == (viewModel.tabsModel.active as EditorTabModel).branchId!!) {
//                                            viewModel.chosenSourceBranch.value = branch.name
//                                            break
//                                        }
//                                    }
//                                    viewModel.showDialogBranchName.value=true
//                                }else if(index!=currentBranchIndex){
//                                    currentBranchIndex = index
//                                    viewModel.selectBranch(branches[currentBranchIndex].id,(viewModel.tabsModel.active as EditorTabModel).owningProjectId !!)
//                                }
//                                expanded = false
//                            }) {
//                                Text(text = s, fontSize = AppTheme.fontSize)
//                            }
//                        }
//                    }
//                }

//                Icon(Icons.Filled.Merge,"Merge", tint = AppTheme.colors.iconGreen, modifier = Modifier.clickable {
//                    viewModel.chosenProjectName.value =
//                        (viewModel.tabsModel.active as EditorTabModel).projectName!!

//                    for (branch in branches) {
//                        if (branch.id == (viewModel.tabsModel.active as EditorTabModel).branchId!!) {
//                            viewModel.chosenSourceBranch.value = branch.name
//                            break
//                        }
//                    }

//                    viewModel.showDialogMergeBranches.value=true
//                })
//                Icon(Icons.Filled.Commit,"Commit Tree", tint = AppTheme.colors.iconGreen, modifier = Modifier.clickable {
//                    showCommitTree.value = true
//                })

//            }
//        }
//    }
//}