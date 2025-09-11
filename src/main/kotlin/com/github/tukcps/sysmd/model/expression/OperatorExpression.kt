package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.expression.functions.AstFunction

interface OperatorExpression: InvocationExpression {
    var operatorPrecedence: Array<String>?

    var operator: String? //According to standard
    var operatorAst: AstFunction? //Operator as Instance of ASTFunction to execute - necessary?
}