@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel

/**
 * Adds the main pull-down menu in to top window line.
 *
 *  SysMD   Backend   Help
 *  ----------------------
 *  Quit    Login     Settings
 *  ...     ------    Help
 */
@Composable
fun FrameWindowScope.MenuBar(sysMDViewModel: SysMDViewModel) = MenuBar {
    fun showSettingsDialog() {
        sysMDViewModel.showSettingsDialog.value = true
        sysMDViewModel.reconnectionRequired.value = true
    }

    fun createProject() {
        sysMDViewModel.showDialogProjectName.value=true
    }

    fun deleteProject() {

    }

    fun createBranch() {
        sysMDViewModel.showDialogBranchName.value = true
    }

    fun deleteBranch() {
        sysMDViewModel.showDialogBranchDeletion.value = true
    }

    fun mergeBranch() {
        //model.showDialogMergeBranches.value = true
    }

    fun createCommit() {
        isCommitDialogOpen.value = true
    }


    Menu("Backend") {
        Item("Login", null) { showSettingsDialog() }
        Separator()
        Item("Create Project") { createProject() } // Icons.Default.NewLabel
        Item("Delete Project") { deleteProject() } // Icons.Default.Delete
        Item("Create Branch") { createBranch() }
        Item("Delete Branch") { deleteBranch() }
        Item("Merge Branch") { mergeBranch() }
        Item("Commit") { createCommit() } // , Icons.Default.Save
        Item("Push") {} // , Icons.Default.Upload
    }

    Menu("Help") {
        Item("Settings") { showSettingsDialog() }
        Item("Help") {
            val project = sysMDViewModel.projectListViewModel.projectViewModels.value.find { it.name == "SysMD Kickstart" }
            project?.let { sysMDViewModel.projectListViewModel.onOpenProject(project) }
        }
    }
}
