package com.github.tukcps.sysmd.model.expression

interface ConstructorExpression: InstantiationExpression
{
    override fun clone(): ConstructorExpression
}