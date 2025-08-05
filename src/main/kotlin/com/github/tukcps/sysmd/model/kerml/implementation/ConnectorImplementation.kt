@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*

/**
 * Connector / Binary link; usage of an Association
 * connector c: A from f1 to f2;
 */
open class ConnectorImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    elementType: String = "Connector"
) : Connector, Type, FeatureImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    // As Connector only provides interface ...
    final override var owningRelatedElement: Element = UnresolvedElement()
    final override var ownedRelatedElement: MutableList<Element> = mutableListOf()

    override val owner: Element?
        get() = owningRelationship?.owningRelatedElement

    override val association: Association
        get() = generalization.filterIsInstance<Association>().first()

    override var isImplied: Boolean = false
    override var isDirected: Boolean = false

    final override var from: MutableList<Element>
        get() = source
        set(value) { source = value }

    final override var to: MutableList<Element>
        get() = target
        set(value) { target = value }

    final override var source: MutableList<Element> = mutableListOf()
    final override var target: MutableList<Element> = mutableListOf()

    override fun resolveNames(): Boolean {
        updated = updated or super.resolveNames()
        return updated
    }

    override fun clone(): Connector{
        return ConnectorImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
        ).also { klon ->
            klon.model = model
            klon.from = from.toMutableList()
            klon.to = to.toMutableList()
            klon.direction = direction
            klon.updated = updated
        }
    }

    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is Relationship) {
            target = template.target.toMutableList()
            source = template.source.toMutableList()
        }
    }

    override fun toString() = super.toString() +" "+ relationshipString("connects", "to")
}
