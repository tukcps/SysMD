package com.github.tukcps.sysmd.model.expression

interface SelectExpression: OperatorExpression
{
    override fun clone(): SelectExpression
}