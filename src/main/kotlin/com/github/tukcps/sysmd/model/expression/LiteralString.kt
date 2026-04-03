package com.github.tukcps.sysmd.model.expression

interface LiteralString: LiteralExpression
{
	override val value: String?

	override fun clone(): LiteralString
}