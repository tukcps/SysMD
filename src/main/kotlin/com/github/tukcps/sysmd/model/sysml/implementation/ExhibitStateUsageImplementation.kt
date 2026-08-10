package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.ExhibitStateUsage
import com.github.tukcps.sysmd.model.sysml.StateUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ExhibitStateUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) :
    ExhibitStateUsage, StateUsageImplementation(model,elementId = elementId)
{
    override val exhibitedState: StateUsage = TODO()
    override val performedAction: ActionUsage
        get() = TODO("Not yet implemented")

    override fun namingFeature(): Feature? {
        TODO("Not yet implemented")
    }
}
