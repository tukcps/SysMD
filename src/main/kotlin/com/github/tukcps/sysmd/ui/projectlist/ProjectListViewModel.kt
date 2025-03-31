package com.github.tukcps.sysmd.ui.projectlist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.repositories.local.SysMDProjectService
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

/**
 * The view model of the project list left.
 * @param sessionState the session with the currently edited model.
 * @param editorTabsViewModel the tabs that are currently open.
 * @param reset
 */
class ProjectListViewModel(
    var sessionState: MutableState<Session>,
    var editorTabsViewModel: EditorTabsViewModel,
    var reset: () -> Unit
) {
    var showNewProjectDialog: MutableState<Boolean> = mutableStateOf(false)
    var viewModelsOfProjects: MutableState<SnapshotStateList<ProjectViewModel>> = mutableStateOf(mutableStateListOf())
    var projectOfSession:    MutableState<ProjectViewModel?> = mutableStateOf(null)
    var projectToUpdate:      MutableState<ProjectViewModel?> = mutableStateOf(null)

    init {
        getProjects()
    }

    /**
     * Reads all potential folders and files in the data folder into the view model.
     * Each project is assumed to be in its own folder, where the project has the
     * same name as the folder (+suffix .md).
     */
    fun getProjects() {
        val projects = projectService.getProjects()
        viewModelsOfProjects.value = mutableStateListOf()
        projects.forEach {
            val projectViewModel = ProjectViewModel(
                sessionState = sessionState,
                editorTabsViewModel = editorTabsViewModel,
                reset = reset,
                project =  it,
                activeProject = projectOfSession
            )
            viewModelsOfProjects.value.add(projectViewModel)
        }
    }

    /**
     * Creates a new project:
     * - creates suitable project folder
     * - creates main project file in the tabs
     * - closes dialog
     * @param projectViewModel a view model of the project to be created
     */
    fun createProject(projectViewModel: ProjectViewModel) {
        projectViewModel.updateProject(
            projectService.createProject(name = projectViewModel.name, description = projectViewModel.description, defaultBranch = null).also { projectViewModel.project?.id = it.id} as ProjectData
        )
        projectViewModel.directory = Path(settings.dataFolder).resolve(projectViewModel.name)
        projectViewModel.directory.createDirectories()
        viewModelsOfProjects.value.add(projectViewModel)
        showNewProjectDialog.value = false
    }

    /**
     * Deletes a project by renaming it to name.datetime.deleted.
     * @param projectViewModel the view model of the project to be deleted.
     */
    fun deleteProject(projectViewModel: ProjectViewModel) {
        if (projectViewModel != projectOfSession.value) {
            viewModelsOfProjects.value.remove(projectViewModel)
            projectService.deleteProject(projectViewModel.project?.id!!)
            SysMDProjectService.logger.info("Deleted project ${projectViewModel.project?.name}")
        }
    }

    fun updateProject(projectViewModel: ProjectViewModel) {

    }
}