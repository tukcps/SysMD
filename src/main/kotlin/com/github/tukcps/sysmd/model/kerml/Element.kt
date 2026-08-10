package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.ModelServices
import kotlin.uuid.Uuid

interface Element: ModelServices {

    /**
     * A unique id of the Element that remains unchained over the whole lifecycle of the element.
     * We use UUIDv4 as suggested in SysMLv2 Std, except for library elements for which we use UUIDv5.
     */
    val elementId: Uuid

    /** Tool-specific ids; not used */
    var aliasIds: Collection<String>

    /**
     * fields for identification by name, shortName.
     */
    var declaredName: SimpleName?
    var declaredShortName: SimpleName?

    /**
     * Derived properties via getters of the field: name, shortName, effectiveName
     * They consider the effective name and re-definition of the declared name
     */
    val name: SimpleName?
    val shortName: SimpleName?

    fun effectiveName(): String?
    fun effectiveShortName(): String?

    /** The name, or short name, if necessary in ticks and with escape sequences */
    fun escapedName(): String?

    /** Path from root namespace to this element */
    val qualifiedName: QualifiedName?

    /**
     * Path including unnamed elements to this element.
     */
    fun path(): String

    /**
     * Needed for the path
     */
    fun positionOf(element: Element): Int?


    /**
     * Reified Relationships from which owner, owningNamespace, etc. are derived.
     * Contains reified relationships that relate the element with its owned elements.
     */
    var ownedRelationship: MutableList<Relationship>

    /** Reified Relationship from which owner and the below properties are derived. */
    var owningRelationship: OwningMembership?

    /**
     * The ownership is modeled by a set of owned elements.
     * This assumes that we have an Element that is not a Relationship.
     * Relationship overloads ownedElement.
     */
    val ownedElement: List<Element>
        get() = ownedRelationship.map {
            if (it is OwningMembership)
                it.memberElement
            else
                it
        }


    /** The owning element, skipping relationships unless they are in the element hierarchy */
    val owner: Element?
        get() = owningRelationship?.owner

    val owningNamespace: Namespace?
    val standardNamespace: Namespace?

    /** Whether the element is from SysML or KerML libraries, these have UUID type 5, not 4 */
    val isLibraryElement: Boolean
    var isStandard: Boolean

    /** Whether (all) implied relationships are included or not */
    var isImpliedIncluded: Boolean

    /**
     * Is true for elements that are added, but are not subject to persistence and/or exchange of
     * entities.
     */
    var isTransient: Boolean

    /**
     * The textual representation that created this element
     */
    var textualRepresentation: MutableList<TextualRepresentation>

    /**
     * Related documentation, i.e., the notebook cells before the elements textual representation.
     */
    var documentation: MutableList<Documentation>

    /**
     * Function that updates this element from a given template.
     * @param template element with updates for this element.
     */
    fun updateFrom(template: Element)
}


/**
 * Returns the first element of type T from the owned elements.
 * @param T The subclass of Element for which we search.
 */
inline fun <reified T: Element> Element.getOwnedElementOfType(): T? {
    val found = ownedElement.find { it is T }
    return found as T?
}


/**
 * Returns a mutable list of all owned elements of type T.
 * @param T The subclass of Element for which we search.
 */
inline fun <reified T: Element> Element.getOwnedElementsOfType(): List<T> =
    ownedElement.filterIsInstance<T>()

/**
 * Returns the owned element with a given name.
 * @param id An identification to search for
 */
fun Element.getOwnedElement(id : Identification): Element? {
    ownedElement.forEach {
        if(Identification(it) == id)
            return it
    }
    return null
}

fun Element.getOwnedElement(id : String) = getOwnedElement(Identification(null, id))