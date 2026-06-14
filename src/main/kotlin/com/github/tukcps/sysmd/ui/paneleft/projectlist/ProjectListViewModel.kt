package com.github.tukcps.sysmd.ui.paneleft.projectlist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.SessionManager.projectService
import com.github.tukcps.sysmd.ui.viewmodel.EditorTabsViewModel
import kotlin.uuid.Uuid


/**
 * The view model of the project list left.
 * @param sessionIdState the session with the currently edited model.
 * @param editorTabsViewModel the tabs that are currently open.
 * @param reset
 */
class ProjectListViewModel(
    var sessionIdState: MutableState<Uuid>,
    var editorTabsViewModel: () -> EditorTabsViewModel,
    val refreshTrees: () -> Unit,
    val reset: () -> Unit
) {
    /**
     * The project that is edited in the current session. Note that currently only one single session
     * is supported by the UI (but we might supporte more in future).
     */
    var selectedProjectState: MutableState<ProjectViewModel?> = mutableStateOf(null)
    var showNewProjectDialog: MutableState<Boolean> = mutableStateOf(false)
    var projectViewModels:    MutableState<SnapshotStateList<ProjectViewModel>> = mutableStateOf(mutableStateListOf())
    var projectToUpdate:      MutableState<ProjectViewModel?> = mutableStateOf(null)

    init {
        getProjectsFromRepository()
    }

    /**
     * Reads all projects from the project service into the view model.
     */
    fun getProjectsFromRepository() {
        val projects = projectService.getProjects()
        projectViewModels.value = mutableStateListOf()
        projects.forEach {
            val projectViewModel = ProjectViewModel(
                sessionIdState = sessionIdState,
                editorTabsViewModel = editorTabsViewModel,
                project =  it,
                selectedProjectState = selectedProjectState,
                reset = reset,
                refreshTrees = refreshTrees,
            )
            projectViewModels.value.add(projectViewModel)
        }
    }

    /**
     * Creates a new project, based on a view model created by the**NewProjectDialog**.
     * The project is transferred to the view model.
     * - creates suitable project folder
     * - creates main project file in the tabs
     * - closes dialog
     * @param projectViewModel a view model of the project to be created
     */
    fun onCreateProject(projectViewModel: ProjectViewModel) {
        val project = projectService.createProject(
            name = projectViewModel.name,
            description = projectViewModel.description,
            defaultBranch = null)
            .also { projectViewModel.project?.id = it.id
            } as ProjectData

        projectViewModel.updateProject(project = project)
        projectViewModels.value.add(projectViewModel)
        showNewProjectDialog.value = false
    }

    /**
     * Deletes a project by renaming it to name.datetime.deleted.
     * @param projectViewModel the view model of the project to be deleted.
     */
    fun onDeleteProject(projectViewModel: ProjectViewModel) {
        projectViewModel.closeProjectSession()
        projectViewModels.value.remove(projectViewModel)
        projectService.deleteProject(projectViewModel.project?.id!!)
    }

    /**
     * Starts a session of a project
     * @param projectViewModel the project view model that shall be rendered. If necessary, a new session is started.
     */
    fun onOpenProject(projectViewModel: ProjectViewModel) {

        if (selectedProjectState.value == projectViewModel) return

        // If there are changes in the currently open project, show the save project dialog.
        if (selectedProjectState.value != projectViewModel && selectedProjectState.value?.unsavedChangesExist() == true) {
            selectedProjectState.value?.showSaveProjectDialog?.value = true
        } else {
            // switch to selected project
            selectedProjectState.value?.closeProjectSession()
            projectViewModels.value.forEach { project ->
                project.isExpanded.value = projectViewModel == projectViewModel.selectedProjectState
            }
            projectViewModel.isExpanded.value = true
            projectViewModel.createProjectSession()
        }
    }
}