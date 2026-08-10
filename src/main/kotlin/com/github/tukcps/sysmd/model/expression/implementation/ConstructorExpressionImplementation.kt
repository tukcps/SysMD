package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.ConstructorExpression
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class ConstructorExpressionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
) : ConstructorExpression,InstantiationExpressionImplementation(model,elementId = elementId)
{

    override fun clone(): InstantiationExpressionImplementation {
        TODO("Not yet implemented")
    }

    override fun toAstString(b: StringBuilder, precedence: Int) {
        TODO("Not yet implemented")
    }

}
