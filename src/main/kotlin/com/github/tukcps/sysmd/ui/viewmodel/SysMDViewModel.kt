package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.loadSysMDFromFile
import com.github.tukcps.sysmd.ui.composables.TreeViewModel
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import com.github.tukcps.sysmd.ui.paneright.BoardViewModel


/**
 * For debug, can be selected with SHIFT + Primary Pointer / Left Mouse Click
 */
fun display(node: TreeViewNodeModel) {
    val element = (node as HasATree).element
    println(element.toString())
    println(" - indices = ${element.indices}")
    println(" - token   = ${element.input?.substring(element.indices!!)}")
}


/**
 * View model for overall application
 * @param sessionParam Session in which the KerML model is edited, computed, ...
 */
class SysMDViewModel(
    sessionParam: Session
) {
    // The main window with editable files
    var sessionState = mutableStateOf(sessionParam)
    var session: Session by sessionState
    val tabsViewModel = TabsViewModel(sessionState, ::refreshTrees)
    val agenda = BoardViewModel(sessionState)
    var agendaIsEmpty = mutableStateOf(agenda.isEmpty())

    // The selectable tree views
    val composition = mutableStateOf(TreeViewModel(HasATree(mutableStateOf(session.global)), null, null, ::display, false))
    val inheritance = mutableStateOf(TreeViewModel(IsATree(mutableStateOf(session.anything)), null, null, ::display, false))

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
        val project = session.project
        projectService.reset()

        // start a new session.
        session.endSession()

        if (project != null) {
            // Re-start project
            session = SessionManager.startSession(project = project)
            session.project!!.getIndex().forEach { file ->
                session.loadSysMDFromFile(file, compile = false, 0)
            }
        }
        // reset the UI
        agenda.clear()
        tabsViewModel.reset()
        refreshTrees()
    }

    /**
     * Redraws all tree-views by opening/closing them and also updates the agenda.
     * This function should be called after each change in the KerML model of a session.
     */
    fun refreshTrees() {
        composition.value = TreeViewModel(HasATree(mutableStateOf(session.global)), null, null, ::display, false)
        inheritance.value = TreeViewModel(IsATree(mutableStateOf(session.anything)), sort = false)
        agenda.clear()
        agenda.update()
        agendaIsEmpty.value = agenda.isEmpty()
    }

    /**
     * Compiles all tabs.
     */
    fun compile(solve: Boolean = true) {
        agenda.clear()
        session.status.reset()
        tabsViewModel.editorTabs.forEach {
            it.cells.forEach { cell ->
                if (cell.language.value == TextualRepresentationViewModel.Companion.Language.YAML) {
                    session.importMD(cell.body.text, null)
                }
            }
        }
        session.loadUsages()
        tabsViewModel.editorTabs.forEach { tab ->
            tab.cells.forEach { cell ->
                cell.compile(propagate = false)
            }
        }
        session.initialize()
        if (solve) session.solver.propagate()
        tabsViewModel.editorTabs.forEach { tab ->
            tab.cells.forEach { cell ->
                cell.collectVariablesToDisplay() }
        }
        refreshTrees()  // refreshes tree-views and agenda
    }
}
