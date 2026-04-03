package com.github.tukcps.sysmd.model.expression

interface LiteralExpression: Expression
{
	val value : Any?

	override fun clone(): LiteralExpression
}