package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.settings
import io.github.tukcps.sysmlv2.api.entities.Branch
import io.github.tukcps.sysmlv2.api.entities.Project
import io.github.tukcps.sysmlv2.api.services.ProjectService
import io.github.tukcps.sysmlv2.interchange.InterchangeProject
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.*


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
            directory = Path(settings.dataFolder).resolve(name!!)
        )

        projectDataRepository.add(project)

        if (project.data.isNotEmpty()) return project

        val projectFolder = Path(settings.dataFolder)
            .resolve(project.name?:project.id.toString())
            .createDirectories()
        val newFile = projectFolder.resolve("${project.name}.md").createFile()
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
        if (!Path(settings.dataFolder).isDirectory()) return emptyList<ProjectData>()
        val projectDirectories = Path(settings.dataFolder).listDirectoryEntries()
            .filter { it.isDirectory() }
            .filter { it.resolve(".project.json").exists() }
            .filter { !it.name.endsWith(".deleted") }

        projectDirectories.forEach { projectDirectory ->
            val project = ProjectData.fromInterchangeFiles(projectDirectory)
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
            val dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.HH.mm.ss")).toString()
            val projectFound = projectDataRepository.firstOrNull { it.id == projectId }
            projectFound?.let {
                it.directory?.moveTo(Path("${it.directory!!}.${it.name}.$dateTime.deleted"))
            }
            projectDataRepository.remove(projectFound)
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