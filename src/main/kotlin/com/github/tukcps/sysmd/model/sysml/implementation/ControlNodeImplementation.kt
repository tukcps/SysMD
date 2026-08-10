package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.sysml.ControlNode
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class ControlNodeImplementation(model : Session,elementId : Uuid = Uuid.random())
    : ControlNode, ActionUsageImplementation(model,elementId = elementId)
{
    
    override fun multiplicityHasBounds(mult: Multiplicity, lower: Int, upper: Int): Boolean = TODO()
    
}
