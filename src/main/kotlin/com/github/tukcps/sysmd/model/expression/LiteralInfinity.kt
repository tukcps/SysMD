package com.github.tukcps.sysmd.model.expression

interface LiteralInfinity: LiteralExpression
{
    override fun clone(): LiteralInfinity
}