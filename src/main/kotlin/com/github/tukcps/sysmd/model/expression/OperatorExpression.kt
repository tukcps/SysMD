package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.model.expression.implementation.OperatorInformation

interface OperatorExpression: InvocationExpression {
    var operator: String? //According to standard
        get() = functionName?.split("::")?.lastOrNull()
        set(value) {
            // FIXME: name escaping?
			val ns = OperatorInformation[value]?.namespace ?: "DataFunctions"
            functionName = "$ns::$value"
        }

    /** Non-Standard. Mapping to legacy AST representation */
    var operatorAst: AstFunction? //Operator as Instance of ASTFunction to execute - necessary?

    override fun clone(): OperatorExpression
}