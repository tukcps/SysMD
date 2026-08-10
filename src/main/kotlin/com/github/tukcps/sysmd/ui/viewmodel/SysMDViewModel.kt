package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.SYSML_LIBRARIES
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.services.session.loadSysMDFromFile
import com.github.tukcps.sysmd.ui.composables.TreeViewModel
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import com.github.tukcps.sysmd.ui.paneleft.projectlist.ProjectListViewModel
import com.github.tukcps.sysmd.ui.paneright.BoardViewModel
import kotlin.uuid.Uuid


/**
 * For debug, can be selected with SHIFT + Primary Pointer / Left Mouse Click
 */
fun display(node: TreeViewNodeModel) {
    val element = (node as HasATree).element
    println(element.toString())
 }


/**
 * View model for overall application.
 */
class SysMDViewModel {
    val sessionIdState: MutableState<Uuid> = mutableStateOf(Uuid.NIL)

    // The main window with editable files
    var sessionId by sessionIdState

    val projectListViewModel: ProjectListViewModel by lazy {
        ProjectListViewModel(sessionIdState, { editorTabsViewModel }, reset = ::reset, refreshTrees = ::refreshTrees,)
    }

    val editorTabsViewModel: EditorTabsViewModel by lazy {
        EditorTabsViewModel({ projectListViewModel }, ::refreshTrees)
    }

    val boardViewModel = BoardViewModel(sessionIdState)
    var boardIsEmpty = mutableStateOf(boardViewModel.isEmpty())

    // The selectable tree views
    val composition = mutableStateOf(TreeViewModel(
        HasATree(sessionIdState, mutableStateOf(sessionService.getSession(sessionId)?.global?.toElementData()
            ?: ElementData(elementId = Uuid.random(), ElementType.Package))), null, null, ::display, false))
    val inheritance = mutableStateOf(TreeViewModel(
        IsATree(sessionIdState, mutableStateOf(sessionService.getSession(sessionId)?.repo?.anything?.toElementData()
            ?: ElementData(elementId = Uuid.random(), ElementType.Package))), null, null, ::display, false))

    val showSaveBeforeExitDialog = mutableStateOf(false)
    val showSettingsDialog = mutableStateOf(false)
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
        val project = sessionService.getSession(sessionIdState.value)?.project
        projectService.reset()

        // start a new session.
        SessionManager.kill(sessionId)

        if (project != null) {
            // Re-start project
            val session = SessionManager.createSession(project = project, libraries = SYSML_LIBRARIES)
            session.settings.includeOwningRelationshipsToRoot = false
            sessionId = session.id
            sessionService.getSession(sessionId)?.project?.getIndexedFiles()?.forEach { file ->
                sessionService.getSession(sessionId)?.loadSysMDFromFile(file)
            }
        }
        // reset the UI
        boardViewModel.clear()
        editorTabsViewModel.reset()
        refreshTrees()
    }

    /**
     * Redraws all tree-views by opening/closing them and also updates the agenda.
     * This function should be called after each change in the KerML model of a session.
     */
    fun refreshTrees() {
        composition.value = TreeViewModel(HasATree(sessionIdState, mutableStateOf(sessionService.getSession(sessionId)?.global?.toElementData())), null, null, ::display, false)
        inheritance.value = TreeViewModel(IsATree(sessionIdState, mutableStateOf(sessionService.getSession(sessionId)?.repo?.anything?.toElementData())), sort = false)
        boardViewModel.clear()
        boardViewModel.update()
        boardIsEmpty.value = boardViewModel.isEmpty()
    }

    /**
     * Compiles all tabs.
     */
    fun compile(solve: Boolean = true) {
        boardViewModel.clear()
        sessionService.getSession(sessionId)?.status?.reset()
        editorTabsViewModel.editorTabs.forEach {
            it.cells.forEach { cell ->
                if (cell.language.value == Language.YAML) {
                    sessionService.getSession(sessionId)?.importMD(cell.body.text, null)
                }
            }
        }
        sessionService.getSession(sessionId)?.loadUsages()
        editorTabsViewModel.editorTabs.forEach { tab ->
            tab.cells.forEach { cell ->
                cell.compile(propagate = false)
            }
        }
        if (solve)
            sessionService.getSession(sessionId)?.solver?.propagate()
        editorTabsViewModel.editorTabs.forEach { tab ->
            tab.cells.forEach { cell ->
                cell.collectVariablesToDisplay() }
        }
        refreshTrees()  // refreshes tree-views and agenda
    }
}
