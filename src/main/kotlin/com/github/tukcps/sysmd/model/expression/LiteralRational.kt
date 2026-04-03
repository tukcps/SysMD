package com.github.tukcps.sysmd.model.expression

interface LiteralRational: LiteralExpression
{
	override var value: Double?

	override fun clone(): LiteralRational
}