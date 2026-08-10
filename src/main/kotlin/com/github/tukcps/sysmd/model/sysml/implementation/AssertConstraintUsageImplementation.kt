package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.sysml.AssertConstraintUsage
import com.github.tukcps.sysmd.model.sysml.ConstraintUsage
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class AssertConstraintUsageImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) : AssertConstraintUsage, InvariantImplementation(model,elementId = elementId)
{
    val assertedConstraint: ConstraintUsage = TODO()

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
