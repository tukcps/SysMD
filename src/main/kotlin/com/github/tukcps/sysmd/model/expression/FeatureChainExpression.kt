package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.ParameterMembership

interface FeatureChainExpression: OperatorExpression
{
	/** The feature chain being applied to 'source'
	 * TODO: Standard specifies this as a feature, unsure what kind of feature
	 */
	var targetFeature : String?

	val source : Expression?
		get() = argument.firstOrNull()

	override fun clone(): FeatureChainExpression
}