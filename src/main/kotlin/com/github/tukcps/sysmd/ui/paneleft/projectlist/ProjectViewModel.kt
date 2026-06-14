package com.github.tukcps.sysmd.ui.paneleft.projectlist

import androidx.compose.runtime.*
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.ui.syntaxhighlighting.indexerScope
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel
import io.github.tukcps.sysmlv2.interchange.InterchangeProject
import kotlinx.coroutines.cancel
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.*
import kotlin.math.min
import kotlin.uuid.Uuid


/**
 * A project view model that holds all relevant information of a project.
 * @param sessionIdState id of the session in which we work.
 * @param editorTabsViewModel View model of the tabs list.
 * @param project Data record of the project from the session.
 * @param selectedProjectState Currently active project state.
 * @param reset Lambda to be called for reset of session and UI. .
 * @param refreshTrees Lambda to be called for refreshing the UI.
 */
data class ProjectViewModel(
    val sessionIdState: MutableState<Uuid>,
    val editorTabsViewModel: () -> EditorTabsViewModel,
    var project: ProjectData?,
    val selectedProjectState: MutableState<ProjectViewModel?>,
    val reset: () -> Unit,
    val refreshTrees: () -> Unit,
) {
    val fileData: FileData = FileData()
    var isChangedState = mutableStateOf(false)

    var fileToDelete: MutableState<String?> = mutableStateOf(null)
    val isExpanded = mutableStateOf(this == selectedProjectState.value)
    val showDeleteFileDialog = mutableStateOf(false)
    private val nameState: MutableState<String> = mutableStateOf(project?.name ?: "")
    private val descriptionState: MutableState<String> = mutableStateOf(project?.description ?: "")


    /** State of all filenames that are displayed in Project and possibly Tabs */
    val filesState = mutableStateListOf<String>()
    val maintainerState = mutableStateListOf<String>()
    val websiteState: MutableState<URI?> = mutableStateOf(null)

    /** Controls the dialog for deleting a file */
    val showSaveProjectDialog: MutableState<Boolean> = mutableStateOf(false)
    val showChangeIconDialog = mutableStateOf(false)

    var name: String by nameState
    var description: String by descriptionState
    private var website: URI? by websiteState

    init {
        // Get view model's state from project information from repository data
        if (project != null)
            updateProject(project!!)
    }

    /**
     * Updates the view model from a project data record
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
        (project.getIndexedFiles()).forEach { file ->
            filesState.add(file.name)
        }
        this.website = (project.project as InterchangeProject).website
    }

    /**
     * Updates the view model and its data record from a projectViewModel used in crate/update dialog.
     * @param projectViewModel provides the data that will only be copied.
     */
    fun updateProject(projectViewModel: ProjectViewModel) {
        name = projectViewModel.name
        description = projectViewModel.description
        project?.name = projectViewModel.name
        project?.description = projectViewModel.description
        website = (projectViewModel.project?.project as InterchangeProject).website
        isChangedState.value = true
        // this.maintainer = (projectViewModel.project?.project as InterchangeProject).maintainer?:mutableListOf()
    }

    /**
     * Checks whether one or more files have been changed and are not yet saved.
     * @return true if there are unsaved changes
     */
    fun unsavedChangesExistInFiles(): Boolean =
        editorTabsViewModel().editorTabs.any { it.elementEdited.value }

    fun unsavedChangesExist(): Boolean =
        unsavedChangesExistInFiles() || isChangedState.value

    /**
     * Reads the project's data record for editing.
     * Before that, the method closes the open project and starts a new session.
     */
    fun createProjectSession() {
        if (project != null) {
            // Close all tabs that are open from other project
            val closeCalls = editorTabsViewModel().editorTabs.map { it.close }
            closeCalls.forEach { it?.invoke() }

            // Cleanup old maps
            fileData.cellData.clear()
            filesState.clear()

            // Initialize states to be sure
            selectedProjectState.value = this
            isExpanded.value = true
            isChangedState.value = false

            // Start a new session with the project
            sessionIdState.value = sessionService.createSession(project!!).id
            loadProjectFromRepository()

            fileData.cellData.forEach { (name, _) ->
                filesState.add(name)
                editorTabsViewModel().showTab(name)
            }

            editorTabsViewModel().selectedIndex.value = 0
        }
    }

    /**
     * Closes a project session: closes tabs, clears data, initializes states to be sure.
     */
    fun closeProjectSession() {

        // Close all tabs that are open from other project
        val closeCalls = editorTabsViewModel().editorTabs.map { it.close }
        closeCalls.forEach { it?.invoke() }

        // Cleanup old maps
        fileData.cellData.clear()
        filesState.clear()

        // Mark it as inactive, not changed
        isChangedState.value = false
        selectedProjectState.value = null
        isExpanded.value = false
    }

    /**
     * Reads, for each file in .meta.json each file and brings the data into
     * the fileData which acts as local buffer between optionally using it for a tab,
     * and optionally saving it back to the repository.
     */
    fun loadProjectFromRepository() {
        // Open the files in the index, into the model and in tabs, but don't compile
        val cells = sessionService.getCells(sessionIdState.value)
        cells?.let { fileData.cellData = cells }
    }

    /**
     * Saves all files back to the repository.
     */
    fun saveProjectToRepository() {
        project?.name = nameState.value
        project?.description = descriptionState.value
        (project?.project as? InterchangeProject)?.website = websiteState.value
        getChangesFromEditor()
        project?.meta?.index?.clear()
        project?.meta?.index = fileData.cellData.map { (file, _) -> file to file }.toMap(LinkedHashMap())
        project?.meta?.let { sessionService.putMeta(sessionIdState.value, project!!.meta!!) }
        fileData.cellData.forEach { (file, cells) -> saveFileToRepository(file, cells) }
        isChangedState.value = false
    }

    /**
     * Updates the file data of the project with all changes from the editor's view model.
     */
    fun getChangesFromEditor() {
        editorTabsViewModel().editorTabs.forEach { cellList ->
            fileData.cellData[cellList.nameState.value] = cellList.cells .map { cell ->
                ElementData(
                    UUID.randomUUID(),
                    type = "TextualRepresentation",
                    language = Language.languageWithNamespace(cell.language.value, cell.namespace.value),
                    body = cell.body.text
                )
            }
        }
    }

    /**
     * Saves a file/tab with given name back to the repository.
     */
    fun saveFileToRepository(name: String, cells: List<ElementData>) {
        // Put file to repo
        sessionService.putCells(sessionIdState.value, name, cells)
    }

    /**
     * Shows a tab (with a file) of a project in the main area.
     */
    fun showTab(name: String) {
        val foundFile = fileData.cellData[name]

        if (foundFile != null) {
            // Open the file in the tab view, or select it.
            editorTabsViewModel().showTab(name)
        } else {
            logger.error("File '$name' not found in project index.")
        }

        // Restart the indexer for this tab
        indexerScope.cancel()
        // indexerScope.launch { Indexer.initializeIndexes(editorTabsViewModel()) }
    }

    /**
     * Opens the folder containing the given file in the system's file explorer.
     */
    fun openContainingFolder() {

        try {
            val os = System.getProperty("os.name").lowercase()
            val folder = if (project != null) project!!.directory else null
            if (folder == null) return

            if ( SystemFileSystem.metadataOrNull(folder)?.isDirectory == true) {
                when {
                    os.contains("win") -> { // Windows
                        Runtime.getRuntime().exec(arrayOf("explorer, \"${folder}\""))
                    }

                    os.contains("mac") -> { // macOS
                        Runtime.getRuntime().exec(arrayOf("open", folder.toString()))
                    }

                    else -> { // Linux or other Unix-like systems
                        // Try xdg-open (default file manager)
                        Runtime.getRuntime().exec(arrayOf("xdg-open", folder.toString()))
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e.message)
            try {
                // Fallback using Desktop API
                Desktop.getDesktop().open(File(project?.directory?.toString()?:""))
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
            val oldName: String = filesState[index]
            filesState.removeAt(index)
            filesState.add(index, newName)

            fileData.cellData = LinkedHashMap(fileData.cellData.mapKeys { (key, _) -> if (key == oldName) newName else key })

            project?.meta?.index?.clear()
            project?.meta?.index = fileData.cellData.map { (file, _) -> file to file }.toMap(LinkedHashMap())

            editorTabsViewModel().updateTabTitle(index, newName)
            editorTabsViewModel().selectedIndex.value = -1
            editorTabsViewModel().selectedIndex.value = min(index, editorTabsViewModel().editorTabs.size-1)
            isChangedState.value = true
            return true
        }
        return false
    }

    /**
     * Creates new file in a project
     */
    fun createFileInProject() {
        val fileName = "File-${filesState.lastIndex+2}.md"
        val path = project?.directory?.let { Path(it, fileName) }

        // Update the filesState (list of files in Viewmodel)
        filesState.add(fileName)

        // Add some cells
        fileData.cellData[fileName] = mutableListOf(
            ElementData(
                elementId = UUID.randomUUID(),
                type = "TextualRepresentation",
                body = """
            ---
            title: New file "$fileName"
            subtitle:  -- Subtitle -- 
            author: Author's names 
            ---
            """.trimIndent(),
                language = "YAML"
            ),
            ElementData(
                elementId = UUID.randomUUID(),
                type = "TextualRepresentation",
                body = """
                - The file is (unless you use the Web-UI) in the folder `$path`. 
                - You can **rename** or **delete** the file via the left pane in the respective project. 
                - Write your model and documentation here 
                    - Edit a cell by double-clicking on it or left of it, 
                    - Add a cells by clicking on the space between or below cells, or on the "+" left of it.
                    - Choose kind and syntax of a cell you edit by choosing "Language" on top of the cell. 
                """.trimIndent(),
                language = "Markdown"
            )
        )

        // Open Tab with file from session
        path?.let { editorTabsViewModel().showTab(fileName) }

        // Set active model to newly created one
        editorTabsViewModel().selectedIndex.value = editorTabsViewModel().editorTabs.size-1
        isChangedState.value = true
    }

    /**
     * Delete File of a project
     */
    fun deleteFileFromProject() {
        // Close tab if open
        val close = editorTabsViewModel().editorTabs.find { it.nameState.value == fileToDelete.value}
        close?.let { editorTabsViewModel().hideTab(it) }

        // delete file from index
        filesState.removeIf { it == fileToDelete.value }

        // delete in Session
        fileData.cellData.remove(fileToDelete.value)
        isChangedState.value = true
    }

    /**
     * Writes [fileBytes] as icon.png into the project's Files directory,
     * replacing any existing icon.
     */
    fun updateProjectIcon(fileBytes: ByteArray) {
        try {
            sessionService.putFile(sessionIdState.value, "icon.png", fileBytes)
            logger.info("Updated Project $name's icon")
        } catch (e: Exception) {
            logger.error("Failed to update Project $name's icon", e)
        }
    }

    /**
     * Deletes icon.png from the project's Files directory,
     * causing the UI to fall back to the default folder icon.
     */
    fun deleteProjectIcon() {
        try {
            sessionService.deleteFile(sessionIdState.value, "icon.png")
            logger.info("Deleted project $name's icon")
        } catch (e: Exception) {
            logger.error("Failed to delete project $name's icon", e)
        }
    }

    /**
     * Compiles all compilable cells in the project via the session API.
     * @param runLevel To what extent do the compile run, e.g., NONE (compile), MODEL (builds model), ALL (solver)
     */
    fun compile(runLevel: Runlevel) {
        // First, compile all files of the project, no analysis (would report errors)
        fileData.cellData.values.forEach { cellData ->
            cellData.forEach { cell ->
                val language = Language.toLanguage(cell.language?:"")
                if (language?.isCompilable() == true) {
                    val namespace = Language.toNamespace(cell.language ?: "")
                    val body = cell.body ?: return@forEach
                    sessionService.updateModel(sessionIdState.value, body, language = language, namespace = namespace, Runlevel.NONE)
                }
            }
        }
        // Finally, do analysis as requested by runlevel
        sessionService.updateModel(sessionIdState.value, "", language = Language.SYS_ML, namespace = null, runlevel = runLevel)
        // Update the variable to display in shown tabs
        editorTabsViewModel().editorTabs.forEach { tab ->
            tab.cells.forEach { cell ->
                cell.collectVariablesToDisplay() }
        }
        // Enforce update of UI, as session's elements are not states in UI.
        refreshTrees()
    }

    override fun toString() = "ProjectViewModel(name='$name')"
}
