package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Dependency
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved

class DependencyImplementation(
    declaredName: String? = null,
    declaredShortName: String? = null,
    client: MutableList<Resolved<Element>> = mutableListOf(),
    supplier: MutableList<Resolved<Element>> = mutableListOf(),
    elementType: String = "Dependency"
): Dependency, RelationshipImplementation(
    declaredName=declaredName,
    declaredShortName=declaredShortName,
    elementType = elementType
) {
    override var client: MutableList<Resolved<Element>>
        get() = source
        set(value) { source = value }

    override var supplier: MutableList<Resolved<Element>>
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
            declaredName=declaredName,
            declaredShortName=declaredShortName,
            client = client,
            supplier = supplier,
        ).also{
            it.model = model
        }
    }

    override fun toString(): String =
        "Dependency { name='$declaredName', shortName = '$declaredShortName', clients: ${client.size}, suppliers: ${supplier.size}}"
}