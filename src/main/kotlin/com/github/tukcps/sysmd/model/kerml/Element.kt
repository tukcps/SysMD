package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.ModelServices
import java.util.*

interface Element: ModelServices {
    /**
     * The name of the respective KerML class; usually the same as the name of the interface.
     */
    val elementType: String

    /**
     * A unique id of the Element that remains unchained over the whole lifecycle of the element.
     * We use UUIDv4 as suggested in SysMLv2 Std, except for library elements for which we use UUIDv5.
     */
    var elementId: UUID?

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
    val ownedRelationship: List<Relationship>
        get() = getOwnedElementsOfType<Relationship>()

    /** The ownership is modeled by a set of owned elements.*/
    var ownedElement: MutableList<Resolved<Element>>

    /** Reified Relationship from which owner and the below properties are derived. */
    var owningRelationship: Resolved<Relationship>

    /** The owning element; can be an Identity with "null" entries in case of the root element.*/
    var owner: Resolved<Element>                      // --> to be replaced by derived property
    val owningNamespace: Namespace?                   // finds the owning namespace
    val standardNamespace: Namespace?                 // null, if not standard

    /** Whether the element is from SysML or KerML libraries, these have UUID type 5, not 4 */
    var isLibraryElement: Boolean
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

    fun addOwnedElement(element: Element)
    fun setOwner(owningElement: Element)
    fun getOwner(): Element? = owner.ref

    fun updateFrom(template: Element)
}


/**
 * Returns the first element of type T from the owned elements.
 * @param T The subclass of Element for which we search.
 */
inline fun <reified T: Element> Element.getOwnedElementOfType(): T? {
    val found = ownedElement.find { (it.ref != null) && it.ref is T }
    return found?.ref as T?
}


/**
 * Returns a mutable list of all owned elements of type T.
 * @param T The subclass of Element for which we search.
 */
inline fun <reified T: Element> Element.getOwnedElementsOfType(): MutableList<T>  {
    val result = mutableListOf<T>()
    ownedElement.forEach { if (it.ref is T) result.add(it.ref as T) }
    return result
}


/**
 * Returns the owned element with a given name.
 * @param name A SimpleName that is searched for
 */
fun Element.getOwnedElement(name: SimpleName?, shortName: SimpleName? = null): Element? {
    ownedElement.forEach {
        if (name != null && it.ref?.name == name
            || (shortName == null) && it.ref?.declaredShortName == name
            || (shortName!= null) && it.ref?.declaredShortName == shortName)
            return it.ref
    }
    return null
}

/**
 * Returns the owned element with a given name.
 * @param name A SimpleName that is searched for
 */
inline fun <reified T> Element.getOwned(name: SimpleName): T? {
    ownedElement.forEach {
        if (it.ref?.declaredName == name)
            return if (it.ref is T) it.ref as T else null
        if (it.ref?.declaredShortName == name)
            return if (it.ref is T) it.ref as T else null
    }
    return null
}

/**
 * Returns the owned element with an index.
 * If the type is not the template parameter type T, null is returned.
 * @param i index of the element list
 */
inline fun <reified T> Element.getOwnedByIndex(i: Int): T? {
    val element = ownedElement.getOrNull(i)?.ref
    return element as? T
}
