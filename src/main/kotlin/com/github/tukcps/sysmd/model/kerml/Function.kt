package com.github.tukcps.sysmd.model.kerml

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.implementation.BuiltinFunction
import com.github.tukcps.sysmd.model.kerml.Feature.FeatureDirectionKind
import com.github.tukcps.sysmd.model.kerml.Feature.FeatureDirectionKind.*

/** ref. 8.3.4.7.4 */
interface Function: Behavior
{
	/** Whether this Function can be used as the function of a model-level evaluable InvocationExpression */
	val isModelLevelEvaluable : Boolean

	val expression : List<Expression>
		get() = step.filterIsInstance<Expression>()

	val result : Feature?
		// FIXME: Standard specifies arity 1 for this, but then sets it to null?
		get() = ownedMembership.filterIsInstance<ReturnParameterMembership>().singleOrNull()?.ownedMemberParameter

	/** If not null, gives a builtin function that defines this model function */
	val builtin : BuiltinFunction?

	/** @return Whether the function accepts the given argument array (with arity mapping) */
	fun accepts(args : List<Expression>) : Boolean
	{
		val parameter = this.parameter
		val result = this.result

		for((ix, arg) in args.withIndex())
		{
			if(ix >= parameter.size)
				return false // too many args

			val par = parameter[ix]

			if(par == result)
				return false // argument is not accepted for result

			if(! checkVariance(par.type, arg.type, par.direction!!))
				return false // type mismatch
		}

		val x = parameter.drop(args.size).none { it != result && it.featureValue === null && 0 !in it.multiplicityRange } // too few arguments

		return x
	}
}

private fun checkVariance(signature : List<Type>, argument : List<Type>, variance : FeatureDirectionKind) : Boolean
= when(variance) {
	// FIXME: Are these correct semantics for non-singleton type lists? Also empty list probably shouldn't be wildcard
	IN -> signature.all { pt -> argument.isEmpty() || argument.any { at -> at.specializes(pt) } }
	OUT -> signature.all { pt -> argument.all { at -> pt.specializes(at) } }
	INOUT -> checkVariance(signature, argument, IN) && checkVariance(signature, argument, OUT)
}