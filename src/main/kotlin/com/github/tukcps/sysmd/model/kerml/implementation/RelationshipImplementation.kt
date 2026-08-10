package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.Unresolved
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.model.util.mapInPlace
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * Following KerML Specification, 7.2.2.2.2 Relationships;
 * Represents the SysMD relationship source relation and inherits ElementImplementation
 */
open class RelationshipImplementation(
    model: Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    override var owningRelatedElement: Element = UnresolvedElement(model),
    final override var source: MutableList<Element> = mutableListOf(),
    final override var target: MutableList<Element> = mutableListOf(),
    override var isImplied: Boolean = false,
): Relationship, ElementImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    final override var ownedRelatedElement: MutableList<Element> = mutableListOf()

    override fun toString() = super.toString() + " from "+
            source.joinToString(", ") { it.escapedName()?:"[${it.elementType().name}]" }  + " to " +
            target.joinToString(", ") { it.escapedName()?:"(no name)" }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Relationship) {
            /*
                This prevents inserting unresolved items for ownership-relevant relationships.
                If e.g. the owningRelatedElement is turned into an unresolved element,
                    because the import code assumes an unresolved element implies a newly discovered ownership,
                    that would create a duplicate entry in the `ownedRelationships` list of its owningRelatedElement.
            */
            val cache = relatedElements.associateBy { it.elementId }

            target = template.target.toMutableList()
            source = template.source.toMutableList()

            fun fix(list : MutableList<Element>) = list.mapInPlace { e ->
                (e as? Unresolved)?.id?.let { cache[it] } ?: e
            }

            fix(target)
            fix(source)
        }
    }

    override fun clone(): Relationship = RelationshipImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        owningRelatedElement = owningRelatedElement,
        source = source,
        target = target,
        isImplied = isImplied,
    ).also {
        it.updateFrom(this)
    }
}
