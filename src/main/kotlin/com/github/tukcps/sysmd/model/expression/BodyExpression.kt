package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Feature

/** Non-standard */
interface BodyExpression : Expression
{
	/** The resulting expressions */
	val returnExpression : Expression? get() = features().lastOrNull() as? Expression
	/** The input arguments */
	val arguments : List<Feature> get() = features().filter { it !== returnExpression }

	override fun clone(): BodyExpression
}