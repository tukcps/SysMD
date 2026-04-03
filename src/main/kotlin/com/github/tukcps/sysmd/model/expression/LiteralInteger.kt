package com.github.tukcps.sysmd.model.expression

interface LiteralInteger: LiteralExpression
{
	override var value: Long?

	override fun clone(): LiteralInteger
}