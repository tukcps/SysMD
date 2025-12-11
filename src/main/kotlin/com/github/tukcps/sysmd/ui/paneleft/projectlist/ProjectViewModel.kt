package com.github.tukcps.sysmd.ui.paneleft.projectlist

import androidx.compose.runtime.*
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.loadSysMDFromFile
import com.github.tukcps.sysmd.ui.syntaxhighlighting.Indexer
import com.github.tukcps.sysmd.ui.syntaxhighlighting.indexerScope
import com.github.tukcps.sysmd.ui.viewmodel.TabsViewModel
import io.github.tukcps.sysmlv2.interchange.InterchangeProject
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

/**
 * A project view model that holds all relevant information of a project.
 */
data class ProjectViewModel(
    val sessionState: MutableState<Session>,
    val tabsViewModel: TabsViewModel,
    val reset: () -> Unit,
    var project: ProjectData?,
    val activeProject: MutableState<ProjectViewModel?>,
) {
    val isExpanded: MutableState<Boolean> = mutableStateOf(this == activeProject.value)
    private val nameState: MutableState<String> = mutableStateOf(project?.name ?: "")
    private val descriptionState: MutableState<String> = mutableStateOf(project?.description ?: "")
    private val filesState = mutableStateListOf<String>()
    private val maintainerState = mutableStateListOf<String>()
    private val websiteState: MutableState<URI?> = mutableStateOf(null)

    val showSaveDialog: MutableState<Boolean> = mutableStateOf(false)

    var name: String by nameState
    var description: String by descriptionState
    private var website: URI? by websiteState
    var directory: Path
        get() = project?.directory!!
        set(value) {
            project?.directory = value
        }

    init {
        if (project != null) updateProject(project!!)
    }

    /**
     * updates this view model from a project data record
     */
    fun updateProject(project: ProjectData) {
        this.project = project
        this.name = project.name ?: ""
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
        this.project?.description = projectViewModel.description
        this.project?.clearIndex()
        projectViewModel.filesState.forEach { file ->
            this.project?.addIndex(file, file)
        }
        // this.maintainer = (projectViewModel.project?.project as InterchangeProject).maintainer?:mutableListOf()
        this.website = (projectViewModel.project?.project as InterchangeProject).website
        project?.saveToInterchangeFiles()
    }

    /**
     * Checks whether one or more files have been changed and are not yet saved.
     * @return true if there are unsaved changes
     */
    fun unsavedChangesExist(): Boolean {
        // First, close all open tabs from the open project.
        // This also checks that changes are saved ...
        tabsViewModel.editorTabs.forEach {
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
            val closeCalls = tabsViewModel.editorTabs.map { it.close }
            closeCalls.forEach {
                if (it != null) {
                    it()
                }
            }

            activeProject.value = this

            // Start a new session with the project
            sessionState.value = SessionManager.startSession(project!!)

            // Open the tabs, but don't compile
            project!!.getIndex().forEach { file ->
                sessionState.value.loadSysMDFromFile(file, compile = false, 0)
                tabsViewModel.open(file, false)
            }
            tabsViewModel.selectedIndex.value = 0

            //Start Coroutine to initialize the Indexes
            // indexerScope.cancel()
            // indexerScope.launch { Indexer.initializeIndexes(tabsViewModel) }
        }
    }

    /**
     * Opens a file of a project in the main area.
     * Before that, the method closes the open project if a file is from another project and starts a new session otherwise keeps the session.
     */
    fun openProjectFile(filename: String, selectedProjectViewModel: ProjectViewModel) {

        // Find the requested file in the project index
        val foundFile = project!!.getIndex().find { file ->
            file.name == filename || file.path.endsWith(filename)
        }

        if (foundFile != null) {
            // Load the file into the current session (without compiling)
            sessionState.value.loadSysMDFromFile(foundFile, compile = false, 0)

            // Open the file in the tab view
            tabsViewModel.open(foundFile, false)

            // Select the opened tab
            tabsViewModel.selectedIndex.value = tabsViewModel.editorTabs.indexOfFirst {
                it.file == foundFile
            }.takeIf { it >= 0 } ?: 0
        } else {
            logger.error("File '$filename' not found in project index.")
        }

        // Restart the indexer for this project
        indexerScope.cancel()
        indexerScope.launch {
            Indexer.initializeIndexes(tabsViewModel)
        }
    }

    /**
     * Opens the folder containing the given file in the system's file explorer.
     */
    fun openContainingFolder() {

        try {
            val os = System.getProperty("os.name").lowercase()
            val folder = project?.directory

            if (folder?.exists()?:false) {
                when {
                    os.contains("win") -> {
                        // Windows
                        Runtime.getRuntime().exec(arrayOf("explorer, \"${folder.absolutePathString()}\""))
                    }

                    os.contains("mac") -> {
                        // macOS
                        Runtime.getRuntime().exec(arrayOf("open", folder.absolutePathString()))
                    }

                    else -> {
                        // Linux or other Unix-like systems
                        // Try xdg-open (default file manager)
                        Runtime.getRuntime().exec(arrayOf("xdg-open", folder.absolutePathString()))
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
            try {
                // Fallback using Desktop API
                Desktop.getDesktop().open(project?.directory?.toFile())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Handles the renaming of a file --> ViewModel!!!
     */
    fun renameFileInProject(newName: String, index: Int): Boolean {
        if (newName.isNotBlank()) {
            tabsViewModel.rename(index, newName)
            return true
        }
        return false
    }

    /**
     * Creates new file in a project
     */
    fun createNewFileInProject() {
        tabsViewModel.addNewFile()
    }

    /**
     * Delete File of a project
     */
    fun deleteProjectFile(filename: String) {
        val tab =  tabsViewModel.editorTabs.indexOfFirst { it.file?.name == filename }
        tabsViewModel.removeFile.value = tab
        tabsViewModel.removeFileDialog.value = true
    }
}
