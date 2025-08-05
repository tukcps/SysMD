package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.UnresolvedElement

class DependencyImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    owningRelatedElement: Element = UnresolvedElement(),
    client: MutableList<Element> = mutableListOf(),
    supplier: MutableList<Element> = mutableListOf(),
    elementType: String = "Dependency"
): Dependency, RelationshipImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    owningRelatedElement = owningRelatedElement,
    source = client,
    target = supplier,
    elementType = elementType
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

    override fun clone(): Dependency{
        return DependencyImplementation(
            declaredName =declaredName,
            declaredShortName =declaredShortName,
            owningRelatedElement = owningRelatedElement,
            client = client.toMutableList(),
            supplier = supplier.toMutableList(),
        ).also{
            it.model = model
        }
    }
}