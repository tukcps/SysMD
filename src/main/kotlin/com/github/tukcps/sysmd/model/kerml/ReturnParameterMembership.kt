package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.expression.Expression

/** ref. 8.3.4.7.8 */
interface ReturnParameterMembership : ParameterMembership
{
	/** Must always be OUT */
	override val parameterDirection : Feature.FeatureDirectionKind
		get() = Feature.FeatureDirectionKind.OUT

	/** Either this or `owningExpression` must be non-null */
	var owningFunction : Function?
		get() = owningBehavior as Function
		set(value) { owningBehavior = value }

	/** Either this or `owningFunction` must be non-null */
	var owningExpression : Expression?
		get() = owningStep as Expression
		set(value) { owningStep = value }
}