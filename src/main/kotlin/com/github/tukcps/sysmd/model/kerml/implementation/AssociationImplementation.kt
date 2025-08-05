@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature


/**
 * Association inherits from Relationship and Classifier (Classifiable).
 * It owns two End Features that are the Association Ends.
 */
open class AssociationImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    sourceType: MutableList<Element> = mutableListOf(),
    targetType: MutableList<Element> = mutableListOf(),
    elementType: String = "Association"
): Association, RelationshipImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    owningRelatedElement = UnresolvedFeature(elementType),
    source = sourceType,
    target = targetType,
    elementType = elementType
) {
    override var isAbstract: Boolean = false
    override var isSufficient: Boolean = false
    override var isConjugated: Boolean = false

    override val owner: Element?
        get() = owningRelationship?.owningRelatedElement

    override fun resolveNames(): Boolean { return false }

    override fun toString(): String = super.toString() +
            if (isAbstract) ", abstract " else "" +
            if (isSufficient) ", sufficient " else "" +
            if (isConjugated) ", conjugated" else ""

    override var sourceType: Type?
        get() = source.firstOrNull() as Type
        set(value) { source = if (value != null) mutableListOf(value) else mutableListOf() }

    override var targetType: MutableList<Type>
        get() = target as MutableList<Type>
        set(value) { target = value as MutableList<Element> }

    override val associationEnd: List<Type>
        get() = (source + target) as MutableList<Type>

    override fun clone(): Association {
        return AssociationImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            sourceType = source.toMutableList(),
            targetType = target.toMutableList()
        ).also { klon -> klon.updateFrom(this) }
    }

    override val subtypes: MutableSet<Type> = mutableSetOf()
}