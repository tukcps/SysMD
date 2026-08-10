@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.UnresolvedNamespace
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


/**
 * Association inherits from Relationship and Classifier (Classifiable).
 * It owns two End Features that are the Association Ends.
 */
open class AssociationImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): Association, ClassifierImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override var isImplied: Boolean = false
    override var owningRelatedElement: Element = UnresolvedNamespace(model)
    override var ownedRelatedElement: MutableList<Element> = ownedElement

    override var isAbstract: Boolean = false
    override var isSufficient: Boolean = false
    override var isConjugated: Boolean = false
    final override var source: MutableList<Element> = mutableListOf()
    final override var target: MutableList<Element> = mutableListOf()

    override val owner: Element?
        get() = owningRelationship?.owningRelatedElement

    override val owningNamespace: Namespace?
        get() = (owner as? Namespace) ?: owner?.owningNamespace

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

    override fun clone(): Association = AssociationImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
    ).also { klon -> klon.updateFrom(this) }

    override val subtypes: MutableSet<Type> = mutableSetOf()
}