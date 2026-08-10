package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Structure
import com.github.tukcps.sysmd.model.sysml.ItemUsage
import com.github.tukcps.sysmd.model.util.MultiplicityRange
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ItemUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : ItemUsage, OccurrenceUsageImplementation(model,elementId = elementId)
{
    override val defaultMultiplicityRange = MultiplicityRange.USAGE_DEFAULT
    override fun clone(): ItemUsage = ItemUsageImplementation(model).also { it.updateFrom(this) }
    override val itemDefinition: MutableList<Structure>
        get() = TODO("Not yet implemented")
}