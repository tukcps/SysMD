package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.rest.entities.api.entities.*
import com.github.tukcps.sysmd.rest.entities.api.services.ProjectUsageService
import kotlin.uuid.Uuid


object SysMDProjectUsageService: ProjectUsageService {
    override fun createProjectUsage(project: Project, branch: Branch?, projectUsage: ProjectUsage): ProjectUsage { TODO("Not yet implemented") }

    override fun getProjectUsage(project: Project, commit: Commit): Collection<ProjectUsage> {
        if (project is ProjectData) {
            val usages = project.data.filter { it.type == CommitDataObject.DataVersionType.ProjectUsage }
                .mapNotNull { it.projectUsage }
            return usages
        }
        return emptyList()
    }

    override fun deleteProjectUsage(project: Project, branch: Branch?, projectUsageId: Uuid): Commit {
        TODO("Not yet implemented")
    }
}