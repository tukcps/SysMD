package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.expression.InvocationExpression
import com.github.tukcps.sysmd.model.kerml.Type

interface TriggerInvocationExpression : InvocationExpression {

    enum class TriggerKind { When, At, After}
    var kind: TriggerKind?

    override fun instantiatedType(): Type

}
