package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.AssociationImplementation
import com.github.tukcps.sysmd.model.sysml.ConnectionDefinition
import com.github.tukcps.sysmd.model.sysml.Usage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ConnectionDefinitionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
) : ConnectionDefinition, AssociationImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override fun clone(): ConnectionDefinition = ConnectionDefinitionImplementation(
        model,
        declaredName = this.declaredName,
        declaredShortName = this.declaredShortName,
    ).also {
        source = source.toMutableList()
        target = target.toMutableList()
    }

    override val connectionEnd: MutableList<Usage>
        get() = TODO("Not yet implemented")
}