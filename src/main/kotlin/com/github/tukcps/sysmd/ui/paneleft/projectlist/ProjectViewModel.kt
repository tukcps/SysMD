package com.github.tukcps.sysmd.ui.paneleft.projectlist

import androidx.compose.runtime.*
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.loadSysMDFromFile
import com.github.tukcps.sysmd.ui.syntaxhighlighting.Indexer
import com.github.tukcps.sysmd.ui.syntaxhighlighting.indexerScope
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel
import io.github.tukcps.sysmlv2.interchange.InterchangeProject
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.URI
import java.nio.file.Path

/**
 * A project view model that holds all relevant information of a project.
 */
data class ProjectViewModel(
    val sessionState: MutableState<Session>,
    val editorTabsViewModel: EditorTabsViewModel,
    val reset: () -> Unit,
    var project: ProjectData?,
    val activeProject: MutableState<ProjectViewModel?>,
) {
    private val nameState: MutableState<String> = mutableStateOf(project?.name?:"")
    private val descriptionState: MutableState<String> = mutableStateOf(project?.description?:"")
    private val filesState = mutableStateListOf<String>()
    private val maintainerState = mutableStateListOf<String>()
    private val websiteState: MutableState<URI?> = mutableStateOf(null)

    val showSaveDialog: MutableState<Boolean> = mutableStateOf(false)

    var name: String by nameState
    var description: String by descriptionState
    private var website: URI? by websiteState
    var directory: Path
        get() = project?.directory!!
        set(value) { project?.directory = value }

    init {
        if (project != null) updateProject(project!!)
    }

    /**
     * updates this view model from a project data record
     */
    fun updateProject(project: ProjectData) {
        this.project = project
        this.name = project.name?:""
        this.description = project.description
        maintainerState.clear()
        (project.project as InterchangeProject).maintainer?.forEach {
            maintainerState.add(it)
        }
        filesState.clear()
        (project.getIndex()).forEach { file ->
            filesState.add(file.name)
        }
        this.website = (project.project as InterchangeProject).website
    }

    /**
     * Updates the view model and its data record.
     * @param projectViewModel provides the data that will only be copied.
     */
    fun updateProject(projectViewModel: ProjectViewModel) {
        this.name = projectViewModel.name
        this.description = projectViewModel.description
        this.project?.name = projectViewModel.name
        this.project?.description  = projectViewModel.description
        this.project?.clearIndex()
        projectViewModel.filesState.forEach { file ->
            this.project?.addIndex(file, file)
        }
        // this.maintainer = (projectViewModel.project?.project as InterchangeProject).maintainer?:mutableListOf()
        this.website = (projectViewModel.project?.project as InterchangeProject).website
        project?.saveToInterchangeFiles()
    }


    fun unsavedChangesExist(): Boolean {
        // First, close all open tabs from the open project.
        // This also checks that changes are saved ...
        editorTabsViewModel.editorTabs.forEach {
            if (it.elementEdited.value) {
                return true
            }
        }
        return false
    }

    /**
     * Opens a project in the main area.
     * Before that, the method closes the open project and starts a new session.
     */
    fun openProject() {
        if (project != null) {
            val closeCalls = editorTabsViewModel.editorTabs.map { it.close }
            closeCalls.forEach { if (it != null) { it() } }

            activeProject.value = this

            // Start a new session with the project
            sessionState.value = SessionManager.startSession(project!!)

            // Open the tabs, but don't compile
            project!!.getIndex().forEach { file ->
                sessionState.value.loadSysMDFromFile(file, compile = false, 0)
                editorTabsViewModel.open(file, false)
            }
            editorTabsViewModel.selectedIndex.value = 0

            //Start Coroutine to initialize the Indexes
            indexerScope.cancel()
            indexerScope.launch { Indexer.initializeIndexes(editorTabsViewModel) }
        }
    }
}