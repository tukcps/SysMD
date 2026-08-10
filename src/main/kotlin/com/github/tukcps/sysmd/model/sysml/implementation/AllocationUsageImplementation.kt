package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.AssociationStructure
import com.github.tukcps.sysmd.model.sysml.AllocationUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class AllocationUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) : AllocationUsage, ConnectionUsageImplementation(model,elementId = elementId)
{
    override fun clone() = AllocationUsageImplementation(model).also { it.updateFrom(this) }

    override val connectionDefinition: MutableList<AssociationStructure>
        get() = TODO("Not yet implemented")
}