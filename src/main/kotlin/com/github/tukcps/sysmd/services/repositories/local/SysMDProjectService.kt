package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.compiler.loadSysMDFromFile
import com.github.tukcps.sysmd.services.session.SessionImplementation
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmlv2.api.ProjectService
import com.github.tukcps.sysmlv2.entities.Project
import java.util.*


/**
 * A simple file-based, local repository
 * Other implementation is the Backend via REST.
 */
object SysMDProjectService: ProjectService {

    /**
     * A List of all projects; locally we do not implement Project Versioning Service.
     * Only a list of Projects (ProjectData).
     */
    private val projectsData = mutableListOf<ProjectData>()

    fun reset() = projectsData.clear()

    /**
     * Creates a new project and returns the created project data
     * @param project the project to be created
     */
    override fun createProject(project: Project): ProjectData {
        if (project is ProjectData) {
            projectsData.add(project)
            return project
        } else {
            val added = ProjectData(name = project.name, id = project.id, description = project.description)
            projectsData.add(added)
            return added
        }
    }

    override fun getProjects(): List<ProjectData> = projectsData

    override fun getProjectById(id: UUID): ProjectData = projectsData.first { it.id == id }

    /**
     * Implementation that searches for a project in
     * - The local (cache) repository (first); maintains consistent UUID over a session
     * - The local file system under the SysMD home directory (in a project-named directory)
     */
    override fun getProjectByName(projectName: String): ProjectData {
        var project = projectsData.firstOrNull { it.name == projectName }
        if ( project != null ) {
            return project
        }

        println("    Project $projectName not in local repository, loading file: $projectName.md")
        val sandbox = SessionImplementation()
        try {
            sandbox.loadSysMDFromFile("$projectName.md", false)
        } catch (error: Exception) {
            sandbox.report(error)
        }
        if (sandbox.status.exceptions.isNotEmpty())
            println(sandbox.status.exceptions)
        project = ProjectData(name = projectName)
        project.data = sandbox.export().data
        projectsData.add(project)
        return project
    }

    override fun updateProject(project: Project) = TODO("Not yet implemented")
    override fun deleteProject(id: UUID): Boolean = projectsData.removeIf { it.id == id }
}

