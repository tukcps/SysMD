package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ParameterMembership
import com.github.tukcps.sysmd.model.util.QualifiedName

/** ref. 8.3.4.8.5 */
interface FeatureReferenceExpression: Expression
{
	/** The referenced feature */
	val referent : Feature?
		get() = ownedMembership.firstOrNull { it !is ParameterMembership }?.target?.firstOrNull() as? Feature

	override fun clone(): FeatureReferenceExpression
}