package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.expression.Expression

/** ref. 8.3.4.6.4
 * Relates a step (expression) or behavior (function) to its parameters (features)
 */
interface ParameterMembership : FeatureMembership
{
	/**
	 * (non-standard; reordering ownedElement is non-trivial due to path-based UUIDs)
	 * The parameter index for positional arguments only.
	 * -1 used as placeholder for named arguments.
	 * Set to correct values after typing pass.
	 */
	var parameterIndex : Int

	/** Either this or `owningBehavior` must be non-null */
	var owningStep : Step?
		get() = owningType as? Expression
		set(value) {
			if(value !== null)
				owningType = value
		}

	/** Either this or `owningStep` must be non-null */
	var owningBehavior : Behavior?
		get() = owningType as? Function
		set(value) {
			if(value !== null)
				owningType = value
		}

	/** The feature that is identified as a parameter by this ParameterMembership */
	var ownedMemberParameter : Feature
		get() = ownedMemberFeature
		set(value) {
			if(value.direction != parameterDirection)
				throw IllegalArgumentException("ParameterMembership direction (${parameterDirection}) must match " +
						"direction of the owned member parameter (${value.direction})")
			ownedMemberFeature = value
		}

	val parameterDirection : Feature.FeatureDirectionKind

	override fun clone() : ParameterMembership
}