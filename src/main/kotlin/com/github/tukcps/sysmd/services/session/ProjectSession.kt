package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.services.repositories.local.ProjectData

interface ProjectSession: Session {

    /**
     * A session from the user's perspective is combined with a modeling project.
     * Then, the project references to a project with its associated data.
     * However, a session can also be used independently.
     * Then, a project can be null.
     */
    val project: ProjectData

    /**
     * Deletes all owned relationships that satisfy a condition
     * @param owner the element that owns the relationships to be deleted.
     * @param condition a lambda expression; if it is satisfied, an owned relationship will be deleted
     */
    fun deleteOwnedRelationship(owner: Element, condition: (relationship: Relationship) -> Boolean)

    /**
     * Loads the usages into the model
     */
    fun loadUsages()

    /**
     * Deletes an element by reference. It also removes the relationship in the according
     * parent element, and also all owned elements.
     * @param element the element to be deleted.
     */
    fun delete(element: Element): Element?
}