@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*


/**
 * Association inherits from Relationship and Classifier (Classifiable).
 * It owns two End Features that are the Association Ends.
 */
open class AssociationImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Association"
): Association, ClassifierImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override var isImplied: Boolean = false
    override var owningRelatedElement: Element = UnresolvedNamespace()
    override var ownedRelatedElement: MutableList<Element> = ownedElement

    override var isAbstract: Boolean = false
    override var isSufficient: Boolean = false
    override var isConjugated: Boolean = false
    final override var source: MutableList<Element> = mutableListOf()
    final override var target: MutableList<Element> = mutableListOf()

    override val owner: Element?
        get() = owningRelationship?.owningRelatedElement

    override fun toString(): String = super.toString() +
            if (isAbstract) ", abstract " else "" +
            if (isSufficient) ", sufficient " else "" +
            if (isConjugated) ", conjugated" else ""

    override var sourceType: Type?
        get() = source.firstOrNull() as Type?
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
        ).also { klon -> klon.updateFrom(this) }
    }

    override val subtypes: MutableSet<Type> = mutableSetOf()
}