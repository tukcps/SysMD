package com.github.tukcps.sysmd.rest.entities.api.services

import com.github.tukcps.sysmd.rest.entities.api.entities.Branch
import com.github.tukcps.sysmd.rest.entities.api.entities.Commit
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import com.github.tukcps.sysmd.rest.entities.api.entities.ProjectUsage
import kotlin.uuid.Uuid


/**
 * Status: Complete and compliant 1-2025.
 */
interface ProjectUsageService {
    fun getProjectUsage(project: Project, commit: Commit): Collection<ProjectUsage>
    fun createProjectUsage(project: Project, branch: Branch? = null, projectUsage: ProjectUsage): ProjectUsage
    fun deleteProjectUsage(project: Project, branch: Branch? = null, projectUsageId: Uuid): Commit
}