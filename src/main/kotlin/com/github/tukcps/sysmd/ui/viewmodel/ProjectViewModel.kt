package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.tukcps.sysmd.compiler.getYaml
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.settings
import java.io.File
import java.net.URI
import java.util.*

/**
 * A project view model that holds all relevant information of a project.
 * @param directory the directory in which the project data is
 */
class ProjectViewModel(
    directory: File? = null,
    name: String = "",
    val tabsModel: TabsModel,
    val reset: () -> Unit
): ProjectData(name = name, directory = directory) {
    private var showDialogState: MutableState<Boolean> = mutableStateOf(false)
    private val nameState: MutableState<String> = mutableStateOf(name)
    private val maintainerState: MutableState<MutableList<String>> = mutableStateOf(mutableListOf())
    private val descriptionState: MutableState<String> = mutableStateOf("")
    private val websiteState: MutableState<URI?> = mutableStateOf(null)
    private val filesState: MutableState<MutableList<File>> = mutableStateOf(mutableListOf())
    val warnDialog: MutableState<Boolean> = mutableStateOf(false)

    var showDialog: Boolean by showDialogState
    override var id: UUID = UUID.randomUUID()
    override var name: String by nameState
    override var description: String by descriptionState
    override var maintainer: MutableList<String> by maintainerState
    override var website: URI? by websiteState
    override var files: MutableList<File> by filesState

    val defaultBranchName: MutableState<String> = mutableStateOf("Main")
    var isProject: Boolean = false
    var selected: MutableState<Boolean> = mutableStateOf(false)
    var logo: MutableState<String> = mutableStateOf("")
    var session: Session? = null

    /**
     * Opens a project in the main area
     */
    fun openProject() {
        val closeCalls = tabsModel.editorTabs.map {
            if ( (it is EditorTabModel) && it.elementEdited.value) {
                warnDialog.value = true
                return
            }
            it.close
        }
        closeCalls.forEach {
            if (it != null) { it() }
        }
        reset()

        settings.projectFolder = directory!!.canonicalPath
        files.forEach {
            tabsModel.open(it, false)
        }
        tabsModel.selectedIndex.value = 0
    }

    /**
     * Creates a new project:
     * - creates suitable project folder
     * - creates main project file in the tabs
     * - closes dialog
     */
    fun createProject() {
        File(settings.dataFolder, name).mkdir()
        settings.projectFolder = settings.dataFolder + "/$name"
        tabsModel.create(settings.projectFolder!!, "$name.md", description)
        showDialog = false
    }
}


class ProjectListViewModel(
    var tabsModel: TabsModel,
    var reset: () -> Unit
) {
    var viewModelsOfProjects: MutableState<SnapshotStateList<ProjectViewModel>> = mutableStateOf(mutableStateListOf())

    init {
        readProjects()
    }

    /**
     * Reads all potential folders and files in the data folder into the view model.
     * Each project is assumed to be in its own folder, where the project has the
     * same name as the folder (+suffix .md).
     */
    fun readProjects() {
        viewModelsOfProjects.value = mutableStateListOf()
        val files = File(settings.dataFolder).listFiles()?.toList() ?: emptyList<File>()
        files.forEach { file ->
            // Directly a file; deprecated
            if (file.isFile && file.name.endsWith(".md", ignoreCase = true)) {
                val yaml = getYaml(file)
                if (yaml != null) {
                    val projectModel = ProjectViewModel(file, yaml["name"] ?: "", tabsModel, reset = reset)
                    projectModel.id = UUID.fromString(yaml["id"]?:UUID.randomUUID().toString())
                    projectModel.maintainer = yaml["maintainer"]?.split(",")?.toMutableList() ?: mutableListOf("")
                    projectModel.description = yaml["description"] ?: ""
                    projectModel.isProject = projectModel.name.isNotBlank()
                    projectModel.logo.value = yaml["logo"] ?: ""
                    projectModel.defaultBranchName.value = yaml["defaultBranchName"] ?: ""
                    projectModel.files.add(file)
                    viewModelsOfProjects.value.add(projectModel)
                }
            }

            // A directory following the rule:
            // - directory name == project file name
            // - In directory we have: project file, other md files, Files directory with pictures
            if (file.isDirectory) {
                val projectFile = file.listFiles()?.filter {
                    it.name == (file.name + ".md") && it.isFile
                }
                if (projectFile?.size == 1) {
                    val yaml = getYaml(projectFile.first())
                    if (yaml != null) {
                        val project = ProjectViewModel(file, yaml["name"]?:"", tabsModel, reset = reset)
                        project.id = UUID.fromString(yaml["id"]?:UUID.randomUUID().toString())
                        project.maintainer = yaml["maintainer"]?.split(",")?.toMutableList() ?: mutableListOf("")
                        project.description = yaml["description"] ?: ""
                        project.isProject = true
                        project.logo.value = yaml["logo"] ?: ""
                        project.defaultBranchName.value = yaml["defaultBranchName"] ?: ""
                        viewModelsOfProjects.value.add(project)
                        project.files.add(File(file.path, file.name+".md"))
                        yaml["files"]?.split(",")?.forEach {
                            val tabfile = File(project.directory?.canonicalPath+"/"+it.trim())
                            if (tabfile.isFile)
                                project.files.add(tabfile)
                        }
                    }
                }
            }
        }
    }
}