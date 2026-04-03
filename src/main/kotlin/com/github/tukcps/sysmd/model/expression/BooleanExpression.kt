package com.github.tukcps.sysmd.model.expression

interface BooleanExpression: Expression
{
    override fun clone(): BooleanExpression
}