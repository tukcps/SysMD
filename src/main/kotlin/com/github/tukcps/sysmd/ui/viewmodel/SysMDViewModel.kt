package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.loadSysMDFromFile
import com.github.tukcps.sysmd.ui.agenda.AgendaViewModel
import com.github.tukcps.sysmd.ui.composables.TreeViewModel


/**
 * View model for overall application
 * @param session Session in which the KerML model is edited, computed, ...
 */
class SysMDViewModel(
    session: Session
) {
    // The main window with editable files
    var kerMlModel = mutableStateOf(session)
    val editorTabsViewModel = EditorTabsViewModel(kerMlModel, ::refreshTrees)
    val agenda = AgendaViewModel(kerMlModel)
    var agendaIsEmpty = mutableStateOf(agenda.isEmpty())

    // The selectable tree views
    val composition = mutableStateOf(TreeViewModel(HasATree(mutableStateOf(kerMlModel.value.global)), null, null, null, null, false))
    val inheritance = mutableStateOf(TreeViewModel(IsATree(mutableStateOf(kerMlModel.value.anything)), null, null, null, null, false))

    val showSettingsDialog: MutableState<Boolean> = mutableStateOf(false)
    val reconnectionRequired:MutableState<Boolean> = mutableStateOf(false)

    //Manage the commit process
    val showDialogProjectName: MutableState<Boolean> = mutableStateOf(false)
    var chosenCommitName: MutableState<String> = mutableStateOf("")
    var chosenCommitDescription: MutableState<String> = mutableStateOf("")
    var showDialogBranchName:MutableState<Boolean> = mutableStateOf(false)
    var showDialogBranchDeletion:MutableState<Boolean> = mutableStateOf(false)
    var showDialogProjectAlreadyExits:MutableState<Boolean> = mutableStateOf(false)

    /**
     * Resets the KerML model and re-loads the default libraries.
     */
    fun reset() {
        // clean repo, brute force ...
        val project = kerMlModel.value.project
        projectService.reset()

        // start a new session.
        kerMlModel.value.endSession()

        if (project != null) {
            // Re-start project
            kerMlModel.value = SessionManager.startSession(project = project)
            kerMlModel.value.project!!.getIndex().forEach { file ->
                kerMlModel.value.loadSysMDFromFile(file, compile = false, 0)
            }
        }
        // reset the UI
        agenda.clear()
        editorTabsViewModel.reset()
        refreshTrees()
    }

    /**
     * Redraws all tree-views by opening/closing them and also updates the agenda.
     * This function should be called after each change in the KerML model of a session.
     */
    fun refreshTrees() {
        composition.value = TreeViewModel(HasATree(mutableStateOf(kerMlModel.value.global)), sort = false)
        inheritance.value = TreeViewModel(IsATree(mutableStateOf(kerMlModel.value.anything)), sort = false)
        agenda.clear()
        agenda.update()
        agendaIsEmpty.value = agenda.isEmpty()
    }

    /**
     * Compiles all tabs.
     */
    fun compile(solve: Boolean = true) {
        agenda.clear()
        kerMlModel.value.status.exceptions.clear()
        editorTabsViewModel.editorTabs.forEach {
            it.cells.forEach { cell ->
                if (cell.language.value == TextualRepresentationViewModel.Companion.Language.YAML) {
                    kerMlModel.value.importMD(cell.body.value.text, null)
                }
            }
        }
        kerMlModel.value.loadUsages()
        editorTabsViewModel.editorTabs.forEach {
            it.cells.forEach { cell ->
                cell.compile(propagate = false)
            }
        }
        kerMlModel.value.initialize()
        if (solve) kerMlModel.value.propagate()
        editorTabsViewModel.editorTabs.forEach { editorTabModel ->
            editorTabModel.cells.forEach { cell -> cell.display() }
        }
        refreshTrees()  // refreshes tree-views and agenda
    }
}
