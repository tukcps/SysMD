package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.cspsolver.Solver
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.sysmlv2.api.entities.CommitDataObject
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import java.util.*


/**
 * The Session is a container in which we work with a Project with a concrete model.
 * A model consists of instances of the elements that are a kind of Element.
 * The instances are saved in a repository 'repo' that implements fast access via
 * id and along relationships by hashmaps.
 */
interface Session {

    /**
     * Each session has a unique id in the session manager.
     */
    val id: UUID

    /**
     * A session from the user's perspective is combined with a modeling project.
     * Then, the project references to a project with its associated data.
     * However, a session can also be used independently.
     * Then, a project can be null.
     */
    var project: ProjectData?

    /**
     * The libraries loaded; can also be a Scenario that implies multiple libraries,
     * e.g., SysMLLibraries
     */
    val libraries: List<String>

    /** Status and reports */
    val status: SessionStatus

    /** Settings of the session */
    val settings: SessionSettings

    /** global is an imaginary package that holds all root elements */
    val global: Namespace

    /** anything is the superclass of all non-classified things */
    val anything: Anything

    /** data contains data structures that represent the model and support efficient access, i.e. caches */
    val repo: Repository

    /** The variables and the solver */
    var solver: Solver
    val builder: DDBuilder

    /**
     * Adds new elements to the session.
     * The existing root namespace is maintained.
     * @param newElements collection of elements that will be added.
     */
    fun import(newElements: Collection<ElementDAO>)

    /**
     * @return Returns the element of the session, and the root of the ownership tree (global)
     * as a Pair, but not the temporary elements.
     */
    fun export(): Collection<CommitDataObject>

    /** @return Returns all elements in the session, including temporary elements, Any, Global. */
    fun get(): Collection<Element>

    /** Gets an element by its id */
    operator fun get(elementId: UUID): Element?

    /**
     * Creates a new element in the model.
     * If an element with the same id or name in namespace exists, its fields will be updated.
     * @param element The element to be added.
     * @param namespace The namespace to which the element will be added.
     * @return the created element with the id field set.
     * Note that it is not necessarily the same as the element passed as argument.
     */
    fun <T: Element> addOwnedMember(element: T, namespace: Namespace, visibility: Import.VisibilityKind = Import.VisibilityKind.Public): T
    /**
     * Adds a relationship to the model.
     * Properly updates ownership when given an OwningMembership.
     * @param owningElement The element that owns the relationship. Defaults to the relationship's source.
     * @return The created relationship. Not necessarily the same relationship that was passed as argument.
     */
    fun <T: Relationship> addOwnedRelationship(relationship: T, owningElement: Element?=null): T

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

    /**
     * Ends a session without saving it.
     */
    fun endSession()

}
