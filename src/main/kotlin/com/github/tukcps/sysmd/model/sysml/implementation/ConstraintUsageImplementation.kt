package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.implementation.BooleanExpressionImplementation
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Predicate
import com.github.tukcps.sysmd.model.sysml.ConstraintUsage
import com.github.tukcps.sysmd.model.sysml.OccurrenceDefinition
import com.github.tukcps.sysmd.model.sysml.OccurrenceUsage
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ConstraintUsageImplementation(
    model: Session,
    elementId: Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null,
) : BooleanExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression,
), ConstraintUsage {
    override fun clone(): ConstraintUsageImplementation = ConstraintUsageImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        expression = expression,
    ).also {
        it.updateFrom(this)
        it.isIndividual = this.isIndividual
        it.portionKind = this.portionKind
        it.occurrenceDefinition.addAll(this.occurrenceDefinition)
    }

    val constraintDefinition: Predicate?
        get() = null
    fun namingFeature(): Feature? = null
    override val individualDefinition: OccurrenceDefinition?
        get() = null
    override var isIndividual: Boolean? = false
    override val occurrenceDefinition: MutableList<Class> = mutableListOf()
    override var portionKind: OccurrenceUsage.PortionKind? = null
}
