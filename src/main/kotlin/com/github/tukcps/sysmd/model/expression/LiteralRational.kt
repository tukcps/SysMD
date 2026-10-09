package com.github.tukcps.sysmd.model.expression

import io.github.tukcps.aadd.values.real.ia.RealRange

interface LiteralRational: LiteralExpression
{
	override var value: RealRange?

	override fun clone(): LiteralRational
}