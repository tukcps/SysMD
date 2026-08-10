package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.implementation.BooleanExpressionImplementation
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Predicate
import com.github.tukcps.sysmd.model.sysml.ConstraintUsage
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ConstraintUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) : ConstraintUsage, BooleanExpressionImplementation(model,elementId = elementId)
{
    val constraintDefinition: Predicate?
        get() = TODO()
    fun namingFeature(): Feature? = TODO()
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
