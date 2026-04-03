package com.github.tukcps.sysmd.model.expression

interface InvocationExpression: InstantiationExpression
{
    override fun clone(): InvocationExpression
}