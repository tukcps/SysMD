package util.mockup

import com.github.tukcps.sysmd.rest.entities.api.entities.Branch
import com.github.tukcps.sysmd.rest.entities.interchange.InterchangeProject
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.repositories.local.SysMDProjectService
import kotlin.uuid.Uuid

/**
 * Basically a SysMDProjectService that does not persist beyond a (test) session.
 * Only for staging tests.
 */
class MockupSysMDProjectService: SysMDProjectService() {

    /**
     * Only writes to memory, not to file ...
     */
    override fun createProject(name: String?, description: String?, defaultBranch: Branch?): ProjectData {
        val addedProject = ProjectData(InterchangeProject(name = name ?: "", description = description ?: ""))
        projectDataRepository.add(addedProject)
        return addedProject
    }


    /**
     * Returns a list with all interchange projects.
     * Read-only and not mocked.
     * @return A list of all projects
     */
    override fun getProjects(): List<ProjectData> = projectDataRepository

    /**
     * Specific for the test mockup ... allows us to set projects to test-projects.
     */
    fun setProjects(projects: List<ProjectData>) {
        projectDataRepository.clear()
        projectDataRepository.addAll(projects)
    }

    /**
     * Deletes a project only from internal structures
     * @param projectId the ID of the project
     */
    override fun deleteProject(projectId: Uuid): ProjectData? {
        val project = projectDataRepository.find { it.id == projectId }
        projectDataRepository.remove(project)
        return project
    }
}