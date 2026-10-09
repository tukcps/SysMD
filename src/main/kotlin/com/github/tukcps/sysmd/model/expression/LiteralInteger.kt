package com.github.tukcps.sysmd.model.expression

import io.github.tukcps.aadd.values.bounds.LongBound

interface LiteralInteger: LiteralExpression
{
	override var value: LongBound?

	override fun clone(): LiteralInteger
}