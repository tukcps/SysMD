package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.CalculationUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class CalculationUsageImplementation(model : Session,elementId : Uuid = Uuid.random())
    : CalculationUsage, ActionUsageImplementation(model,elementId = elementId)
{
    override val calculationDefinition: Function?
        get() = TODO("Not yet implemented")

    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clone(): ActionUsage = CalculationUsageImplementation(model).also {
        it.updateFrom(this)
    }
}