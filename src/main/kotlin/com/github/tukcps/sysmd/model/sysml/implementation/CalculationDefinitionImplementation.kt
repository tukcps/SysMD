package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.sysml.CalculationDefinition
import com.github.tukcps.sysmd.model.sysml.CalculationUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class CalculationDefinitionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) :CalculationDefinition, ActionDefinitionImplementation(model,elementId = elementId)
{
    override var isModelLevelEvaluable: Boolean = false
    override val builtin : Nothing? = null

    override fun clone() = CalculationDefinitionImplementation(model).also {
        it.isModelLevelEvaluable = isModelLevelEvaluable
        it.updateFrom(this)
    }

    override val calculation: MutableList<CalculationUsage>
        get() = TODO("Not yet implemented")
}