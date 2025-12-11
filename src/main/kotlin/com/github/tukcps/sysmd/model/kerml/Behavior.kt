package com.github.tukcps.sysmd.model.kerml

/** ref. 8.3.4.6.2 */
interface Behavior: Class
{
	/** The memberships that produce `parameter` */
	val parameterMembership : List<ParameterMembership>
		get() = membership.filterIsInstance<ParameterMembership>()
			.sortedBy { it.parameterIndex }

	/** The parameters of this Behavior whose values are passed into and/or out of a performance of the Behavior */
	val parameter : List<Feature>
		get() = parameterMembership.map { it.ownedMemberParameter }

	/** The Steps that make up this Behavior */
	val step : List<Step>
		get() = features().filterIsInstance<Step>()
}