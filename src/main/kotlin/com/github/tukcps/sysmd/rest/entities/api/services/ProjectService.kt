package com.github.tukcps.sysmd.rest.entities.api.services

import com.github.tukcps.sysmd.rest.entities.api.entities.Branch
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import kotlin.uuid.Uuid


/**
 * SysML v2 API platform independent project services.
 * Status: complete and compliant w/ 1-2025.
 */
interface ProjectService {
    /**
     * Gets all projects
     * @return A collection of all projects
     */
    fun getProjects(): Collection<Project>

    /**
     * Returns Project with the given projectId or null, if not found.
     */
    fun getProjectById(projectId: Uuid): Project?
    fun createProject(name: String?, description: String? = null, defaultBranch: Branch? = null): Project
    fun updateProject(projectId: Uuid, name: String? = null, description: String? = null, defaultBranch: Branch? = null): Project

    /**
     * Deletes a project.
     * @param projectId the id of the project
     * @return the project that was deleted, or null if not deleted.
     */
    fun deleteProject(projectId: Uuid): Project?
}