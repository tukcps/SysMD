package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Metaclass
import com.github.tukcps.sysmd.model.kerml.Structure
import com.github.tukcps.sysmd.model.kerml.implementation.MetadataFeatureImplementation
import com.github.tukcps.sysmd.model.sysml.MetadataUsage
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class MetadataUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : MetadataUsage, MetadataFeatureImplementation(model,elementId = elementId)
{
    
    override val metadataDefinition: Metaclass? = TODO()
    override val itemDefinition: MutableList<Structure>
        get() = TODO("Not yet implemented")
    override val individualDefinition: OccurrenceDefinition?
        get() = TODO("Not yet implemented")
    override var isIndividual: Boolean?
        get() = TODO("Not yet implemented")
        set(value) {}
    override val occurrenceDefinition: MutableList<Class>
        get() = TODO("Not yet implemented")
    override var portionKind: OccurrenceUsage.PortionKind?
        get() = TODO("Not yet implemented")
        set(value) {}

}
