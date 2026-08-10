package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.expression.implementation.InstantiationExpressionImplementation

interface ConstructorExpression: InstantiationExpression
{
    override fun clone(): InstantiationExpressionImplementation
}