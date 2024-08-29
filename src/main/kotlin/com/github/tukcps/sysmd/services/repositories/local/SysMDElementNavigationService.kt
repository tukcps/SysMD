package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmlv2.api.ElementNavigationService
import com.github.tukcps.sysmlv2.entities.CommitDataObject
import com.github.tukcps.sysmlv2.entities.ElementDAO
import java.util.*


/**
 * Simple implementation of an Element Navigation Service in memory;
 * we implement currently only functions that are needed without version management:
 * - getRootElements
 * - getElements by project name
 *
 */

object SysMDElementNavigationService: ElementNavigationService {
    override fun getElements(projectId: UUID, commitId: UUID?): List<ElementDAO> {
        require(commitId == null) // Only head of default in local repo
        val project = SysMDProjectService.getProjectById(projectId)
        return project.data.filter {
            it.type != CommitDataObject.DataVersionType.ProjectUsage
                    && it.type != CommitDataObject.DataVersionType.DeletedInCommit
        }.mapNotNull { it.payloadElementSnapshot }
    }

    override fun getElementById(projectId: UUID, commitId: UUID, elementId: UUID): ElementData {
        TODO("Not yet implemented")
    }

    override fun getRelationshipsByRelatedElement(
        projectId: UUID,
        commitId: UUID,
        relatedElementId: UUID,
        direction: String
    ): List<ElementDAO> {
        TODO("Not yet implemented")
    }

    override fun getRootElements(projectId: UUID, commitId: UUID?): Collection<ElementDAO> {
        require(commitId == null) // Only head of default in local repo
        val project = SysMDProjectService.getProjectById(projectId)
        return project.data.filter {
            it.type != CommitDataObject.DataVersionType.ProjectUsage
                    && it.type != CommitDataObject.DataVersionType.DeletedInCommit
                    && it.payloadElementSnapshot?.owner?.id == null
        }.mapNotNull { it.payloadElementSnapshot }
    }

    /**
     * Tries to get a document from the repository; if there is
     * no connection to the backend repository, it will be retrieved from
     * 1) local repository used as cache;
     * 2) the local SysMD folder, if not in the cache.
     */
    override fun getElements(projectName: String): List<ElementDAO> {
        val project = SysMDProjectService.getProjectByName(projectName)
        return project.data.mapNotNull { it.payloadElementSnapshot }
    }
}
