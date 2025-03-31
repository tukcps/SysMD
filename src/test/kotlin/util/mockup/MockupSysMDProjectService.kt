package util.mockup

import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.repositories.local.SysMDProjectService
import io.github.tukcps.sysmlv2.api.entities.Branch
import io.github.tukcps.sysmlv2.api.entities.Project
import io.github.tukcps.sysmlv2.interchange.InterchangeProject
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * Basically a SysMDProjectService that does not persist beyond a (test) session.
 * Only for staging tests.
 */
class MockupSysMDProjectService: SysMDProjectService() {

    /**
     * Only writes to memory, not to file ...
     */
    override fun createProject(name: String?, description: String?, defaultBranch: Branch?): ProjectData {
        val addedProject = ProjectData(InterchangeProject(name = name?:"", description = description?:""))
        projectDataRepository.add(addedProject)
        return addedProject
    }


    /**
     * Returns a list with all interchange projects.
     * Read-only and not mocked.
     * @return A list of all projects
     */
    override fun getProjects(): List<ProjectData> = projectDataRepository
    fun setProjects(projects: List<ProjectData>) { projectDataRepository.clear(); projectDataRepository.addAll(projects) }

    /**
     * Deletes a project only from internal structures
     * @param projectId the ID of the project
     */
    override fun deleteProject(projectId: UUID): ProjectData? {
        val project = projectDataRepository.find { it.id == projectId }
        projectDataRepository.remove(project)
        return project
    }

    private val logger = LogManager.getLogger(SysMDProjectService::class.java)
}