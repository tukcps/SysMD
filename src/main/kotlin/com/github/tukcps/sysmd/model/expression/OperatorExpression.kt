package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.model.expression.implementation.OperatorInformation

interface OperatorExpression: InvocationExpression {
    var operatorPrecedence: Array<String>?

    var operator: String? //According to standard
        get() = functionName?.split("::")?.lastOrNull()
        set(value) {
            // FIXME: name escaping?
			val ns = OperatorInformation[value]?.namespace ?: "DataFunctions"
            functionName = "$ns::$value"
        }

    var operatorAst: AstFunction? //Operator as Instance of ASTFunction to execute - necessary?
}