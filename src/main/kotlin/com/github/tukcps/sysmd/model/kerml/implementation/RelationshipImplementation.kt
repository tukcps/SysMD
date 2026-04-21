package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Relationship
import com.github.tukcps.sysmd.model.util.SimpleName


/**
 * Following KerML Specification, 7.2.2.2.2 Relationships;
 * Represents the SysMD relationship source relation and inherits ElementImplementation
 */
open class RelationshipImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    override var owningRelatedElement: Element,
    final override var source: MutableList<Element> = mutableListOf(),
    final override var target: MutableList<Element> = mutableListOf(),
    override var isImplied: Boolean = false,
    elementType: String = "Relationship"
): Relationship, ElementImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    final override var ownedRelatedElement: MutableList<Element> = mutableListOf()

    override fun toString() = super.toString() + " from "+
            source.joinToString(", ") { it.escapedName()?:"[${it.elementType}]" }  + " to " +
            target.joinToString(", ") { it.escapedName()?:"(no name)" }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Relationship) {
          target = template.target.toMutableList()
          source = template.source.toMutableList()
        }
    }

    override fun clone(): Relationship = RelationshipImplementation(
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
