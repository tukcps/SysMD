package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.UnresolvedElement
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class DependencyImplementation(
    model: Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
    owningRelatedElement: Element = UnresolvedElement(model),
    client: MutableList<Element> = mutableListOf(),
    supplier: MutableList<Element> = mutableListOf(),
): Dependency, RelationshipImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    owningRelatedElement = owningRelatedElement,
    source = client,
    target = supplier,
) {
    override var client: MutableList<Element>
        get() = source
        set(value) { source = value }

    override var supplier: MutableList<Element>
        get() = target
        set(value) { target = value }

    init {
        source.addAll(client)
        target.addAll(supplier)
        client.addAll(client)
        supplier.addAll(supplier)
    }

    override fun clone(): Dependency = DependencyImplementation(
        model,
        declaredName =declaredName,
        declaredShortName =declaredShortName,
        owningRelatedElement = owningRelatedElement,
        client = client.toMutableList(),
        supplier = supplier.toMutableList(),
    )
}