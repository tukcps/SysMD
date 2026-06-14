package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.*
import io.github.tukcps.sysmlv2.api.entities.Branch
import io.github.tukcps.sysmlv2.api.entities.Project
import io.github.tukcps.sysmlv2.api.services.ProjectService
import io.github.tukcps.sysmlv2.interchange.InterchangeProject
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.files.Path
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*


/**
 * A simple file-based, local repository
 * Other implementation is the Backend via REST.
 */
open class SysMDProjectService: ProjectService {

    /**
     * A List of all projects; locally we do not implement Project Versioning Service.
     * Only a list of Projects (ProjectData).
     */
    protected val projectDataRepository = mutableListOf<ProjectData>()

    fun reset() {
        projectDataRepository.clear()
        getProjects()
    }


    /**
     * Creates a new project and initializes data structures for it.
     * @param name name of the project and also the folder's name of the project to be created.
     * Creation will fail if the name is null or if a project with the same name already exists.
     * @param description description, optional
     * @param defaultBranch (null)
     */
    override fun createProject(name: String?, description: String?, defaultBranch: Branch?): Project {
        val project = ProjectData(InterchangeProject(
            name = name?:"",
            description = description),
            directory = name?.let { Path(settings.dataFolder, it) }
        )

        projectDataRepository.add(project)

        if (project.data.isNotEmpty()) return project

        val projectFolder = Path(settings.dataFolder, project.name?:project.id.toString())
        val newFile = Path(projectFolder, "${project.name}.md")
        newFile.writeText("""
            ---
            id: ${project.project.id}
            title:
            name: ${project.name}
            maintainer: 
            version: 
            website: 
            usage: 
            description: ${project.description}
            ---
            [toc]
            # Cell with documentation
            Write the documentation in Markdown-Cells.
            - To add a cell double click on (+) above or below an existing cell
            - To add a file (edited in a new tab) add its file name in the configuration on top (requires restart)
            
            ```SysML
            // Write the model in the cells of SysMD Notebook
            package hello {
                attribute world: ScalarValues::Real = 1.0 + oneOf(2.0 .. 3.0); 
            }
            ```
        """.trimIndent())
        project.addIndex(newFile.name, newFile.name)
        project.saveToInterchangeFiles()
        return project
    }


    /**
     * Returns a list with all interchange projects
     */
    override fun getProjects(): List<ProjectData> {
        if (!Path(settings.dataFolder).isDirectory()) return emptyList()
        val projectDirectories = Path(settings.dataFolder).listChildNames()
            .filter { Path(settings.dataFolder, it).isDirectory() }
            .filter { Path(settings.dataFolder, it, ".project.json").isFile() }
            .filter { !it.endsWith(".deleted") }

        projectDirectories.forEach { projectDirectory ->
            val project = ProjectData.fromInterchangeFiles(Path(settings.dataFolder, projectDirectory))
            if (project != null) {
                if (project.id !in projectDataRepository.map { it.id })
                    projectDataRepository.add(project)
                else {
                    projectDataRepository.find { it.id == project.id }.also {
                        it!!.name = project.name
                        it.description = project.description
                    }
                }
            }
        }
        return projectDataRepository
    }

    /**
     * Gets a project by its id.
     * @param projectId the id of the project.
     * @return the project data record.
     */
    override fun getProjectById(projectId: UUID): ProjectData? = projectDataRepository.find { it.id == projectId }

    /**
     * Updates a project
     */
    override fun updateProject(projectId: UUID, name: String?, description: String?, defaultBranch: Branch?): Project {
        val projectFound = projectDataRepository.firstOrNull { it.id == projectId }
        if (projectFound != null) {
            projectFound.name = name?:projectFound.name
            projectFound.description = description?:projectFound.description
            projectFound.saveToInterchangeFiles()
        }
        return projectFound?: TODO()
    }


    /**
     * Deletes a project, both from the files and the project list.
     * @param projectId the ID of the project
     */
    override fun deleteProject(projectId: UUID): ProjectData? {
        try {
            val dateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val dateTimeString = "${dateTime.date}T${dateTime.hour.toString().padStart(2, '0')}_${dateTime.minute.toString().padStart(2, '0')}Z"
            val projectFound = projectDataRepository.firstOrNull { it.id == projectId }
            projectFound?.let {
                it.directory?.moveTo(Path("${it.directory!!}.$dateTimeString.deleted"))
            }
            projectDataRepository.remove(projectFound)
            logger.info("Deleted project '${projectFound?.name}'")
            return projectFound
        } catch (error: Exception) {
            logger.error(error.message)
            return null
        }
    }

    companion object {
        val logger: Logger = LogManager.getLogger(SysMDProjectService::class.java)
    }
}