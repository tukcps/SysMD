package com.github.tukcps.sysmd.model.expression

interface OperatorExpression: InvocationExpression {
    var operatorPrecedence: Array<String>?
}