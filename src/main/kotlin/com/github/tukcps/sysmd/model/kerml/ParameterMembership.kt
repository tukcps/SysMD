package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.expression.Expression

/** ref. 8.3.4.6.4
 * Relates a step (expression) or behavior (function) to its parameters (features)
 */
interface ParameterMembership : FeatureMembership, OwningMembership
{
	// standard uses Step and Behavior, but we flatten those into Expression/Function

	var parameterIndex : Int

	/** Either this or `owningBehavior` must be non-null */
	var owningStep : Step?
		get() = membershipOwningNamespace as? Expression
		set(value) {
			if(value !== null)
				membershipOwningNamespace = value
		}

	/** Either this or `owningStep` must be non-null */
	var owningBehavior : Behavior?
		get() = membershipOwningNamespace as? Function
		set(value) {
			if(value !== null)
				membershipOwningNamespace = value
		}

	/** The feature that is identified as a parameter by this ParameterMembership */
	var ownedMemberParameter : Feature
		get() = memberElement as Feature
		set(value) {
			if(value.direction != parameterDirection)
				throw IllegalArgumentException("ParameterMembership direction (${parameterDirection}) must match " +
						"direction of the owned member parameter (${value.direction})")
			memberElement = value
		}

	val parameterDirection : Feature.FeatureDirectionKind
}