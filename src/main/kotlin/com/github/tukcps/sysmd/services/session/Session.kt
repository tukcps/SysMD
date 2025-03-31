package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.cspsolver.DiscreteSolver
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Anything
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Type
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
     * Then, project references to project with its associated data.
     * However, a session can also be used to process a single unit of compilation,
     * e.g., to find certain elements. Then, project can be null.
     */
    var project: ProjectData?

    /**
     * The libraries loaded; can also be a Scenario that implies multiple libraries,
     * e.g. SysMLLibraries
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

    /** specific information from the discrete solver */
    var dSolver: DiscreteSolver
    val builder: DDBuilder

    /** The AST subtrees for all subexpressions */
    val astNodes : MutableMap<UUID, AstNode>

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
     * Creates a new element in a namespace that becomes owner of the element.
     * The new element can be of arbitrary subtype of ElementBase; i.e., ValueFeature, Namespace, Feature, etc.
     * If an element with the same id or name in namespace exists, its fields will be updated.
     * @param element The property to be created.
     * @param owner The element in which the property will be created.
     * @return the created element with the id field set. Note that it is not necessarily the same as the
     * element passed as argument.
     */
    fun <T: Element> create(element: T, owner: Element): T

    /**
     * Like 'create', but the created element is marked as transient.
     * The new element can be of arbitrary subtype of ElementBase; i.e., ValueFeature, Namespace, Feature, etc.
     * If an element with the same id or name in namespace exists, its fields will be updated.
     * @param element The property to be created.
     * @param owner The element in which the property will be created.
     * @return the created element with the id field set. Note that it is not necessarily the same as the
     * element passed as argument.
     */
    @Deprecated("Use transient and mark it manually as transient.")
    fun <T: Element> createTransient(element: T, owner: Namespace): T

    /**
     * Creates a new element in a namespace that becomes owner of the element.
     * The new element can be of arbitrary subtype of ElementBase; i.e., Expression, Namespace, Feature, etc.
     * If an element with the same id or name in namespace exists, the existing one will
     * be deleted, including all its owned elements, and be replaced with the element given as argument.
     * @param element The property to be created.
     * @param owner The element in which the property will be created.
     * @return the created element with the id field set. Note that it is not necessarily the same as the
     * element passed as argument.
     */
    fun <T: Element> createOrReplace(element: T, owner: Element): T

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
     * Gets the subtypes of an element. If the element is an instance, it will also consider
     * subclasses of the instance's class.
     */
    fun getSubtypes(element: Type): Collection<Type>

    /**
     * Ends a session without saving it.
     */
    fun endSession()

    /**
     * Removes all elements that are not related to textual models.
     */
    fun reset()

    /**
     * Used to model the path to a not-yet-included or not yet existing owner of an Element.
     * The reference consists of two parts: first, a namespace, and
     * second, relative to the namespace, a path in line with QualifiedName
     * conventions.
     * @param startOfPath the reference to a namespace where a path starts.
     * @param path the path, relative to the namespace to the element.
     */
    data class UnresolvedElement(
        var element:     Element,
        var path:        String? = null,
        var startOfPath: Element,
    )


    /**
     * Adds an element where the ownership is not yet identified by an ID.
     * It is identified by a qualified name and/or feature chain or a reverence or mix thereof.
     * These can in some cases only be identified correctly after all features and
     * inheritance information are initialized.
     * @param element the element to be added to an owner
     * @param startOfOwnerPath an element
     * @param path a qualified name relative to startOfPath
     */
    fun addUnownedElement(element: Element, path: String? = null, startOfOwnerPath: Element=global)
    fun getNumberOfOwnedElements(path: String): Int
    fun getUnownedElements(): List< UnresolvedElement >
    fun dropUnownedElement(element: Element)
    fun updateUnownedElements(unownedElement: Element, existingElement: Element)
}
