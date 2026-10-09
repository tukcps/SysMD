package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.tukcps.sysmd.compiler.importMD
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.ProjectSession
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.SYSML_LIBRARIES
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.services.session.loadSysMDFromFile
import com.github.tukcps.sysmd.ui.composables.TreeViewModel
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import com.github.tukcps.sysmd.ui.paneleft.projectlist.ProjectListViewModel
import com.github.tukcps.sysmd.ui.paneright.BoardViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
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
        ProjectListViewModel(sessionIdState, { editorTabsViewModel }, reset = ::reset, refreshTrees = ::refreshTrees)
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
            sessionId = createFreshSession(project).id
        }
        // reset the UI
        boardViewModel.clear()
        editorTabsViewModel.reset()
        refreshTrees()
    }

    /**
     * Starts a new session with the libraries and the files of a project; nothing is compiled yet.
     * The method does not touch the UI.
     */
    private fun createFreshSession(project: ProjectData): ProjectSession {
        val session = SessionManager.createSession(project = project, libraries = SYSML_LIBRARIES)
        session.settings.includeOwningRelationshipsToRoot = false
        session.project.getIndexedFiles().forEach { file -> session.loadSysMDFromFile(file) }
        return session
    }

    /**
     * What a running [compileProject] currently does; null if none is running.
     * It is a flow, not a state, as it is updated from the background thread.
     */
    val compileProgress: StateFlow<CompileProgress?> get() = _compileProgress
    private val _compileProgress = MutableStateFlow<CompileProgress?>(null)

    /**
     * Compiles all files of the selected project into a fresh session and analyzes the model up to a runlevel;
     * this is what "Compile" and "Solve" do.
     *
     * The model is built on a background thread so that the UI stays responsive; [compileProgress] tells what
     * is going on. Meanwhile, the UI keeps working with the previous session. When the new model is complete,
     * the UI switches to the new session and shows its results. If the user has switched to another project or
     * session in the meantime, the new session is discarded.
     *
     * Must be called from the UI thread. A call while another one is running does nothing.
     * @param runlevel To what extent do the compile run, e.g., MODEL (builds model), ALL (solver)
     */
    suspend fun compileProject(runlevel: Runlevel) {
        if (_compileProgress.value != null) return
        val projectViewModel = projectListViewModel.selectedProjectState.value ?: return
        val previousSessionId = sessionId
        val project = sessionService.getSession(previousSessionId)?.project ?: return

        _compileProgress.value = CompileProgress.preparing
        var newSession: ProjectSession? = null
        try {
            // On the UI thread: clear the old results, and take the code as it is in the editor.
            projectService.reset()
            boardViewModel.clear()
            boardIsEmpty.value = true
            editorTabsViewModel.reset()
            projectViewModel.getChangesFromEditor()
            val cells = projectViewModel.compilableCells()

            // In the background: build the model in a session that the UI does not know yet.
            withContext(Dispatchers.Default) {
                val session = createFreshSession(project).also { newSession = it }
                projectViewModel.compileCells(session.id, cells, runlevel) { _compileProgress.value = it }
            }

            // Back on the UI thread: switch to the new session, unless the user has moved on.
            val session = newSession
            if (session != null && sessionId == previousSessionId && projectListViewModel.selectedProjectState.value === projectViewModel) {
                _compileProgress.value = CompileProgress.showingResults
                sessionId = session.id
                newSession = null
                SessionManager.kill(previousSessionId)
                projectViewModel.showResults()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // The UI stays with the previous session.
            logger.error("Compiling the project failed: ${exception.message}", exception)
        } finally {
            newSession?.let { SessionManager.kill(it.id) } // not taken over by the UI
            _compileProgress.value = null
        }
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
