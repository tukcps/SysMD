@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * Connector / Binary link; usage of an Association
 * connector c: A from f1 to f2;
 */
open class ConnectorImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
) : Connector, Type, FeatureImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    // As Connector only provides interface ...
    final override var owningRelatedElement: Element = UnresolvedElement(model)
    final override var ownedRelatedElement: MutableList<Element> = mutableListOf()

    override val owner: Element?
        get() = owningRelationship?.owningRelatedElement

    override val owningNamespace: Namespace?
        get() = owningRelationship?.owningNamespace

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

    override fun clone(): Connector = ConnectorImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
    ).also { klon ->
        klon.from = from.toMutableList()
        klon.to = to.toMutableList()
        klon.direction = direction
        klon.updated = updated
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
