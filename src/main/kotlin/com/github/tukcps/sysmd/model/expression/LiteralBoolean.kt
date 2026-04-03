package com.github.tukcps.sysmd.model.expression

interface LiteralBoolean: LiteralExpression
{
	override var value: Boolean?

	override fun clone(): LiteralBoolean
}