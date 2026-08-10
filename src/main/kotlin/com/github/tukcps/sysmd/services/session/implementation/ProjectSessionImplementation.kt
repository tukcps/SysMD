package com.github.tukcps.sysmd.services.session.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.repositories.local.ProjectUsageData
import com.github.tukcps.sysmd.services.session.ProjectSession
import com.github.tukcps.sysmd.services.session.SessionSettings
import com.github.tukcps.sysmd.services.session.SessionStatus
import com.github.tukcps.sysmd.services.session.loadProject

class ProjectSessionImplementation(
    override val project: ProjectData,
    vararg libraries: String,
    settings: SessionSettings = SessionSettings(),
    status: SessionStatus = SessionStatus(),
    runlevel: Runlevel
): ProjectSession, SessionImplementation(
    libraries = libraries,
    settings = settings,
    status = status,
    runlevel = runlevel
) {

    /**
     * Loads all usages of other projects into the current project.
     * For this purpose, the method gets the usages and calls the method loadProject.
     */
    override fun loadUsages() {
        project.getUsages().forEach {
            if(it is ProjectUsageData) {
                try {
                    loadProject(it.resource.toString(), maxRunlevel = Runlevel.NONE)
                } catch (error: Exception) {
                    status.fatal("Error in usage ${it.resource}: " + (error.message ?: "unknown error"))
                }
            }
        }
    }

    /**
     * Deletes all owned relationships that satisfy a condition
     * @param owner the element that owns the relationships to be deleted.
     * @param condition a lambda expression; if it is satisfied, an owned relationship will be deleted
     */
    override fun deleteOwnedRelationship(
        owner: Element,
        condition: (Relationship) -> Boolean
    ) {
        owner.ownedRelationship.forEach { relationship ->
            if ( condition(relationship) )
                owner.ownedRelationship.remove(relationship)
        }
    }

    /**
     * Deletes an element with a given reference and all owned elements.
     * Also updates ownedElements of the owner.
     * @param element reference to the element to be deleted
     */
    override fun delete(element: Element): Element? {

        when (element) {
            global -> return null
            is Relationship if (element !is Namespace) -> {
                for (r in element.ownedRelatedElement.toMutableList()) {
                    delete(r)
                }
            }
            else -> {
                for (e in element.ownedRelationship.toMutableList()) {
                    delete(e)
                }
            }
        }
        element.owningRelationship?.ownedElement?.remove(element)
        element.owningNamespace?.ownedRelationship?.remove(element)
        repo.remove(element.elementId)
        return null
    }

    override fun toString(): String {
        return "ProjectSession ${project.name}, status $status }"
    }
}