package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
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
 *   @param elementId a UUID v4 that is valid for the overall life cycle (versions) of an element.
 *                    Default is UUID4.
 *   @param declaredName a simple name
 *   @param declaredShortName a simple name that abbreviates the name
 *   @param aliasIds a tool-specific set of alias ids
 *   @param owningRelationship the memberships; must be resolved in case of standard libraries to get UUID5 based on qualified name
 */
open class ElementImplementation(
    final override var elementId: UUID? = null,
    final override var declaredName: SimpleName? = null,
    final override var declaredShortName: SimpleName? = null,
    final override var aliasIds: Collection<String> = emptyList(),
    final override var ownedRelationship: MutableList<Relationship> = mutableListOf(),
    final override var owningRelationship: Relationship? = null,
    final override var textualRepresentation: MutableList<TextualRepresentation> = mutableListOf(),
    final override var documentation: MutableList<Documentation> = mutableListOf(),
    final override var isImpliedIncluded: Boolean = false,
    final override val elementType: String = "Element"
 ) : Element {

    /** Dependency injected by call of model.create() */
    final override var model: Session? = null

    override val name: SimpleName?
        get() = declaredName

    override val shortName: SimpleName?
        get() = declaredShortName

    /** Field-less property that gets the owning namespace. */
    final override val owningNamespace: Namespace?
        get() = if (owner is Namespace?) owner as Namespace? else owner?.owningNamespace

    /** Field-less property that gets the owning standard-namespace or null if not standard. */
    final override val standardNamespace: Namespace?
        get() = if (owningRelationship?.owner is Namespace && owningRelationship?.owner!!.isStandard) owningRelationship?.owner as Namespace else owningRelationship?.owner?.standardNamespace

    /** Field that is true for a library element, then all owned elements are library elements */
    final override var isLibraryElement: Boolean = false
        get() = if (field) field else (owningNamespace?.isLibraryElement == true) or (this is Membership && this.memberElement.isLibraryElement) or isStandard

    /** Field that is true if the element is owned by a standard library */
    final override var isStandard: Boolean = false
        get() = if (field) field else owningNamespace?.isStandard == true

    final override var isTransient: Boolean = false

    /** The input of the compiler that generated the element */
    final override var input: CharSequence? = null

    /** The range of the input by which it was generated */
    final override var indices: IntRange? = null

    /**
     * Flag that is set if some non-transient information was changed.
     * Then, a new id is needed before persisting the element in the backend.
     */
    final override var hasBeenChanged: Boolean = false

    /**
     * Whether value has been changed by constraint propagation.
     * As this affects only non-transient information, no new id is needed.
     */
    final override var updated: Boolean = true


    /** Field-less property; name + owner's name determines qualified name. */
    final override val qualifiedName: QualifiedName?
        get() = when {
            this == model?.global -> null
            this.owner == model?.global -> escapedName()
            escapedName() != null -> "${owner?.qualifiedName}::${escapedName()}"
            else -> null
        }

    /** Complementary to qualified name; also considers the case that an element has no name. */
    final override fun path(): String {
        val path: String
        if (qualifiedName != null)
            path = qualifiedName!!
        else if (this is Relationship && this !is Association && this !is Connector ) // Relationship
            path = owningRelatedElement.path() +
                    "/" + if (this is OwningMembership) (this.target.first().escapedName()?:this.owningRelatedElement.positionOf(this)) else
                     this.owningRelatedElement.positionOf(this)
        else // Element
            if (this.owningRelationship != null)
                path = owningRelationship!!.path() + "/"+ (this.escapedName() ?: this.owningRelationship!!.positionOf(this))
            else
                return ""
        return path
    }

    /**
     * The index of the element in the list of owned elements resp. relationships
     */
    final override fun positionOf(element: Element): Int {
        var position = 0
        if (this !is Relationship || this is Association || this is Connector)
            while(ownedRelationship.getOrNull(position) != null) {
                if (ownedRelationship.getOrNull(position) == element)
                    return position
                position ++
            }
        else
            while(ownedElement.getOrNull(position) != null) {
                if (ownedElement.getOrNull(position) == element)
                    return position
                position ++
            }
        return position
    }

     /**
     * The method searches for (qualified) names in the element and
     * adds the id and reference to identifications, where the search
     * was successful or reports an error where not.
     */
    override fun resolveNames(): Boolean {
        return false
    }

    override fun toString(): String = "[$elementType] " +
            if (escapedName() == null) "" else "'${escapedName()}'"

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
        ).also {
            it.updateFrom(this)
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
        isStandard = template.isStandard
        isLibraryElement = template.isLibraryElement
        input = template.input?:input
        indices = template.indices?:indices
    }

    /** A useful name, lexically correct, generated from name or short name */
    override fun escapedName(): String? = name?:shortName
    override fun effectiveName(): String? = declaredName
    override fun effectiveShortName(): String? = declaredShortName
}
