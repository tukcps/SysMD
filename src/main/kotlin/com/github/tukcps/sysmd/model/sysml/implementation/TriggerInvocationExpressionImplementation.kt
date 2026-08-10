package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.implementation.InvocationExpressionImplementation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.sysml.TriggerInvocationExpression
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** Suggested implementation superclass: InvocationExpressionImplementation. */
class TriggerInvocationExpressionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random()
) : TriggerInvocationExpression, InvocationExpressionImplementation(model,elementId = elementId)
{
    
    override var kind: TriggerInvocationExpression.TriggerKind? = TODO()
    
    override fun instantiatedType(): Type = TODO()

    override fun evaluate(target: Element): Set<Element> {
        TODO("Not yet implemented")
    }

    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean {
        TODO("Not yet implemented")
    }

}
