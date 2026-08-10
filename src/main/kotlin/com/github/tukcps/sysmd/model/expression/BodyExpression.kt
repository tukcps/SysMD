package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ResultExpressionMembership
import com.github.tukcps.sysmd.model.kerml.ReturnParameterMembership

/** Non-standard */
interface BodyExpression : Expression
{
	/** The expression that provides the result, if any */
	val resultExpression : Expression? get() = membership.filterIsInstance<ResultExpressionMembership>().singleOrNull()?.ownedResultExpression
	/** The declaration of the return parameter, if any */
	val returnParameter : Feature? get() = membership.filterIsInstance<ReturnParameterMembership>().singleOrNull()?.ownedMemberParameter
	/** The input parameters */
	val inputParameters : List<Feature> get() {
		val re = resultExpression
		val rp = returnParameter

		return feature.filter { it !== re && it !== rp }
	}

	override fun clone(): BodyExpression
}