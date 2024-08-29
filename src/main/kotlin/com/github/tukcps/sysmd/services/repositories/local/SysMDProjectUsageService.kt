package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmlv2.api.ProjectUsageService
import com.github.tukcps.sysmlv2.entities.CommitDataObject
import com.github.tukcps.sysmlv2.entities.ProjectUsage
import java.util.*


object SysMDProjectUsageService: ProjectUsageService {
    override fun getProjectUsage(projectName: String): Collection<ProjectUsage> {
        val project = SysMDProjectService.getProjectByName(projectName)
        val usages = project.data.filter { it.type == CommitDataObject.DataVersionType.ProjectUsage }
            .mapNotNull { it.projectUsage }
        return usages
    }

    override fun getProjectUsage(projectId: UUID, commitId: UUID): Collection<ProjectUsage> {
        val project = SysMDProjectService.getProjectById(projectId)
        val usages = project.data.filter { it.type == CommitDataObject.DataVersionType.ProjectUsage }
            .mapNotNull { it.projectUsage }
        return usages
    }

    override fun deleteProjectUsage(projectId: UUID, commitId: UUID?): Boolean =
        TODO("Not yet implemented")
}