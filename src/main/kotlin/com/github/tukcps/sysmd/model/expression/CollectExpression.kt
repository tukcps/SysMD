package com.github.tukcps.sysmd.model.expression

interface CollectExpression: OperatorExpression
{
    override fun clone(): CollectExpression
}