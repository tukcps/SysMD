package com.github.tukcps.sysmd.services.repositories.local

import io.github.tukcps.sysmlv2.api.services.ProjectUsageService
import io.github.tukcps.sysmlv2.api.entities.*
import java.util.*


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

    override fun deleteProjectUsage(project: Project, branch: Branch?, projectUsageId: UUID): Commit { TODO("Not yet implemented") }
}