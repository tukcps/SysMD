package com.github.tukcps.sysmd.model.kerml.implementation

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import java.util.*


/**
 * Base class for the repository entities.
 * Elements can be of the special kind Package.
 * Elements are
 * - identified by a unique id field,
 * - have a user-given identification following the KerML v2 rules,
 * - a list of imports following SysML rules,
 * - a collection of owned elements that may not be identified by its names (then we would need a namespace)
 *   @param elementId a UUID v4 that is valid for the overall life cycle (versions) of an element
 *   @param declaredName a simple name
 *   @param declaredShortName a simple name that abbreviates the name
 *   @param aliasIds a tool-specific set of alias ids
 *   @param ownedElement a list of owned element's identifies; identities are reference, uuid, and/or qualified name
 *   @param owner the owner identity
 */
open class ElementImplementation(
    final override var elementId: UUID = UUID.randomUUID(),
    final override var declaredName: SimpleName? = null,
    final override var declaredShortName: SimpleName? = null,
    final override var aliasIds: Collection<String> = emptyList(),
    final override var ownedRelationship: MutableList<Resolved<Relationship>> = mutableListOf(),
    final override var ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    final override var owningRelationship: Resolved<Relationship> = Resolved(),
    final override var owner: Resolved<Element> = Resolved(null, null, null),
    final override var textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    final override var documentation: MutableList<Documentation> = mutableListOf(),
    final override var isImpliedIncluded: Boolean = false,
    final override val elementType: String = "Element"
 ) : Element {

    /** Dependency injected by call of model.create() */
    final override var model: Session? = null

    final override val name: SimpleName?
        get() = declaredName

    final override val shortName: SimpleName?
        get() = declaredShortName

    /** Field-less property that gets the owning namespace. */
    final override val owningNamespace: Namespace?
        get() = if (owner.ref is Namespace) owner.ref as Namespace else owner.ref?.owningNamespace

    /** Field-less property that gets the owning standard-namespace or null if not standard. */
    final override val standardNamespace: Namespace?
        get() = if (owner.ref is Namespace && owner.ref!!.isStandard) owner.ref as Namespace else owner.ref?.standardNamespace

    /** Field that is true for a library element, then all owned elements are library elements */
    final override var isLibraryElement: Boolean = false
        get() = if (field) field else (owningNamespace?.isLibraryElement ?: false) or isStandard

    /** Field that is true if the element is owned by a standard library */
    final override var isStandard: Boolean = false
        get() = if (field) field else owningNamespace?.isStandard?:false

    final override var isTransient: Boolean = false

    init {
        if (isStandard || isLibraryElement)
            this.elementId = Generators.nameBasedGenerator().generate(qualifiedName)
    }


    /** The textual representation by which it was generated; could also be retrieved via generated relationship */
    final override var indices: IntRange? = null
    
    /**
     * Flag that is set if some non-transient information was changed.
     * Then, a new id is needed prior to persisting the element in the backend.
     */
    final override var hasBeenChanged: Boolean = false

    /**
     * Whether value has been changed by constraint propagation.
     * As this affects only non-transient information, no new id is needed.
     */
    final override var updated: Boolean = true


    /** Field-less property; name + owner's name determines qualified name */
    final override val qualifiedName: QualifiedName
        get() = if (owner.ref != null && owner.ref!!.declaredName != "Global")
                    "${owner.ref!!.qualifiedName}::${name?:shortName?:elementType}"
                else
                    declaredName?:elementType

    /**
     * The method searches for (qualified) names in the element and
     * adds the id and reference to identifications, where the search
     * was successful or reports an error where not.
     */
    override fun resolveNames(): Boolean {
        return false
    }

    /**
     * Establishes correct owner relationship between two elements, even if unowned.
     * A call of the model's function 'create' must add then all owned elements
     * to Collection/Map of elements & give the ID.
     * DON'T USE IF YOU WANT TO ADD AN ELEMENT TO THE MODEL!
     * @param element Element to be added
     */
    final override fun addOwnedElement(element: Element) {
        ownedElement.add(Resolved(element))
        element.owner.ref = this
        element.owner.id = this.elementId
    }

    override fun setOwner(owningElement: Element) {
        model = owningElement.model
        owner.id = owningElement.elementId
        owner.ref = owningElement
        owningElement.ownedElement.add(Resolved(this))
    }

    override fun toString(): String =
        "Element { name='$declaredName', shortName = '$declaredShortName', #owned = ${ownedElement.size}, id='$elementId'}"

    /**
     * Clone creates a copy of all fields, but NOT of the owned elements;
     * only some well-understood owned elements from the metamodel are copied (e.g., Multiplicity, ...).
     * Hence, the consistency of the model is NOT by itself preserved.
     * Function should NOT be used to copy parts of the model;
     * ONLY to create a backup or in really internal logic when there are no or well-understood owned elements.
     * !!! TAKE CARE !!!
     * Also, the clone is given a new id.
     */
    override fun clone(): Element {
        return ElementImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            // ownedElement = Resolved.copyOfIdentityList(ownedElement),
            // owner = Resolved(owner)
        ).also {
            it.updated = updated
            it.isTransient = isTransient
        }
    }

    /**
     * Simply copies all fields from a template into the current element.
     * Note that only a shallow copy is made, no deep copy.
     * Also note that the id of the original element is NOT copied.
     * @param template the template whose fields will be copied.
     */
    override fun updateFrom(template: Element) {
        if (declaredName != template.declaredName) {
            declaredName = template.declaredName
            updated = true
        }
        if (declaredShortName != template.declaredShortName) {
            declaredShortName= template.declaredShortName
            updated = true
        }
        if (aliasIds != template.aliasIds) {
            aliasIds = template.aliasIds
            updated = true
        }
        isTransient = template.isTransient
        hasBeenChanged = template.hasBeenChanged
    }

    /** A useful name, lexically correct, generated from name or short name */
    final override fun escapedName(): String? = name?:shortName

    override fun effectiveName(): String? = declaredName
    override fun effectiveShortName(): String? = declaredShortName

}
