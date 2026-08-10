package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.AllocationDefinition
import com.github.tukcps.sysmd.model.sysml.Usage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class AllocationDefinitionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: String? = null,
): AllocationDefinition, ConnectionDefinitionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
    override fun clone(): AllocationDefinition = AllocationDefinitionImplementation(
        model,
        declaredName = this.declaredName,
        declaredShortName = this.declaredShortName,
    ).also { it.updateFrom(this) }

    override val connectionEnd: MutableList<Usage>
        get() = TODO("Not yet implemented")
}