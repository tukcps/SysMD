package com.github.tukcps.sysmd.model.kerml

/** ref. 8.3.4.6.3 */
interface Step: Feature
{
	/** The behaviors of a Step are all its types that are Behaviors */
	val behavior : List<Behavior>
		get() = type.filterIsInstance<Behavior>()

	val parameterMembership : List<ParameterMembership>
		get() = membership.filterIsInstance<ParameterMembership>()
			.sortedBy { it.parameterIndex }

	// FIXME: parameter redefined directedFeature, but feature has no directedFeature
	val parameter : List<Feature>
		get() = parameterMembership.map { it.ownedMemberParameter }
}