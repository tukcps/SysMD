package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.expression.Expression

/** ref. 8.3.4.7.7
 * Relates expressions to their results
 */
interface ResultExpressionMembership : FeatureMembership
{
	/** Either this or `owningExpression` must be non-null */
	var owningFunction : Function?
		get() = membershipOwningNamespace as? Function
		set(value) {
			if(value !== null)
				membershipOwningNamespace = value
		}

	/** Either this or `owningFunction` must be non-null */
	var owningExpression : Expression?
		get() = membershipOwningNamespace as? Expression
		set(value) {
			if(value !== null)
				membershipOwningNamespace = value
		}

	var ownedResultExpression : Expression
		get() = memberElement as Expression
		set(value) { memberElement = value }

	override fun clone() : ResultExpressionMembership
}