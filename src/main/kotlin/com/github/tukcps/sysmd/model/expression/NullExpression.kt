package com.github.tukcps.sysmd.model.expression

interface NullExpression: Expression
{
    override fun clone(): NullExpression
}