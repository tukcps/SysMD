package com.github.tukcps.sysmd.model.kerml

/** ref. 8.3.4.6.2 */
interface Behavior: Class
{
	/** The parameters of this Behavior whose values are passed into and/or out of a performance of the Behavior */
	val parameter : List<Feature>
		get() = feature // not to be confused with Expression::parameter, which is completely different

	/** The Steps that make up this Behavior */
	val step : List<Step>
		get() = feature.filterIsInstance<Step>()
}