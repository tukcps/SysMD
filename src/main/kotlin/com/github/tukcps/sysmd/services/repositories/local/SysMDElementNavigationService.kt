package com.github.tukcps.sysmd.services.repositories.local

import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.elementNavigationService
import com.github.tukcps.sysmd.services.session.loadProject
import io.github.tukcps.sysmlv2.api.entities.Commit
import io.github.tukcps.sysmlv2.api.entities.CommitDataObject
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import io.github.tukcps.sysmlv2.api.entities.Project
import io.github.tukcps.sysmlv2.api.services.ElementNavigationService
import java.util.*


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
    override fun getElements(project: Project, commit: Commit?): List<ElementDAO> {
        var session = SessionManager.getSession(project.id)
        if (session == null) {
            session = SessionManager.startSession(project.id)
            session.loadProject(project.name?:"")
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
        elementId: UUID,
        direction: String
    ): Collection<ElementDAO> {
        TODO("Not yet implemented")
    }

    override fun getElementById(project: Project, commit: Commit?, elementId: UUID): ElementDAO? {
        return getElements(project, commit)
            .firstOrNull { it.elementId == elementId }
            ?.toElementData()    }


    /**
     * Gets the members of the root namespace.
     * @param project the project; in SysMD equal to the active session id.
     * @param commit SysMD is a frontend; no commits, no version management. Shall be null.
     * @return A collection of the  root namespace's members
     */
    override fun getRootElements(project: Project, commit: Commit): Collection<ElementDAO> =
        elementNavigationService.getElements(project, commit).filter {
            it.owner?.id == null
        }

}
