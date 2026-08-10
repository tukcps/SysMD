package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.model.datamodel.createFrom
import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.rest.entities.api.entities.Commit
import com.github.tukcps.sysmd.rest.entities.api.entities.CommitDataObject
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.ElementResponse
import com.github.tukcps.sysmd.rest.entities.api.services.ElementNavigationService
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.elementNavigationService
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import kotlin.uuid.Uuid


/**
 * This class implements the platform independent model API.
 * SysMD is a "frontend" and hence does not implement version management.
 * Hence, only the methods
 * - getProjects
 * - getProjectById
 * - getElements
 * - getElementById
 * - getRootElements
 * - getRelationshipByRelatedElement
 *
 * are implemented.
 * As there are no commits, the commit id can be null or any value.
*/
object SysMDElementNavigationService: ElementNavigationService {

    /**
     * Gets all elements of the session determined by the project id.
     * This shall be the "active" session edited in SysMD.
     * @param project the session id, equal to the project id
     * @param commit ignored and can be null
     */
    override fun getElements(project: Project, commit: Commit?): List<ElementDataIF> {
        var session = SessionManager.getAllSessions().firstOrNull { it.project.id == project.id}
        if (session == null) {
            if (project is ProjectData)
                session = sessionService.createSession(project)
            else
                TODO("Project that is no (file-based) interchange project is not yet implemented")
        }

        val elements = session.export().filter {
                it.type != CommitDataObject.DataVersionType.ProjectUsage &&
                        it.type != CommitDataObject.DataVersionType.DeletedInCommit
            }.mapNotNull { it.payloadElementSnapshot }
       return elements
    }

    /**
     * TODO!
     */
    override fun getRelationshipsByRelatedElement(
        project: Project,
        commit: Commit,
        elementId: Uuid,
        direction: String
    ): Collection<ElementDataIF> {
        TODO("Not yet implemented")
    }

    override fun getElementById(project: Project, commit: Commit?, elementId: Uuid): ElementDataIF? =
        getElements(project, commit)
            .firstOrNull { it.elementId == elementId }
            ?.createFrom<ElementResponse>()

    /**
     * Gets the members of the root namespace.
     * @param project the project; in SysMD equal to the active session id.
     * @param commit SysMD is a frontend; no commits, no version management. Shall be null.
     * @return A collection of the  root namespace's member elements.
     */
    override fun getRootElements(project: Project, commit: Commit): Collection<ElementDataIF> =
        elementNavigationService.getElements(project, commit).filter {
            it.owner?.id == null && it.owningRelationship != null
        }
}