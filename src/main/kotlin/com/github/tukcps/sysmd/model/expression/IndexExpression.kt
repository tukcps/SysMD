package com.github.tukcps.sysmd.model.expression

interface IndexExpression: OperatorExpression
{
    override fun clone(): IndexExpression
}