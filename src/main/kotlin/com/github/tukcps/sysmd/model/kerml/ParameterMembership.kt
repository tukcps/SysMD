package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.expression.Expression

/** ref. 8.3.4.6.4
 * Relates a step (expression) or behavior (function) to its parameters (features)
 */
interface ParameterMembership : FeatureMembership, OwningMembership
{
	// standard uses Step and Behavior, but we flatten those into Expression/Function

	/** Either this or `owningBehavior` must be non-null */
	var owningStep : Expression?
		get() = membershipOwningNamespace as? Expression
		set(value) {
			if(value !== null)
				membershipOwningNamespace = value
		}

	/** Either this or `owningStep` must be non-null */
	var owningBehavior : Function?
		get() = membershipOwningNamespace as? Function
		set(value) {
			if(value !== null)
				membershipOwningNamespace = value
		}

	/** The feature that is identified as a parameter by this ParameterMembership */
	var ownedMemberParameter : Feature
		get() = memberElement as Feature
		set(value) {
			require(value.direction == parameterDirection)
			memberElement = value
		}

	val parameterDirection : Feature.FeatureDirectionKind
}