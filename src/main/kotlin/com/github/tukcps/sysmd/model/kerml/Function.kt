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
		get() = this.ownedRelationship.filterIsInstance<ReturnParameterMembership>().singleOrNull()?.ownedMemberParameter

	/** If not null, gives a builtin function that defines this model function */
	val builtin : BuiltinFunction?

	/** @return Whether the function accepts the given argument array (with arity mapping) */
	fun accepts(args : List<Expression>) : Boolean
	{
		val parameter = this.parameter
		val result = this.result
		// indices into parameter and args
		var pIx = 0
		var aIx = 0
		// number of arguments assigned to parameter[pIx]
		var curArity = 0

		while(true) when {
			aIx >= args.size -> break // all arguments processed
			pIx >= parameter.size -> return false // too many arguments
			parameter[pIx] === result || curArity >= parameter[pIx].multiplicityRange.max -> {
				++pIx
				curArity = 0
			}
			else -> {
				if(! checkVariance(parameter[pIx].type, args[aIx].type, parameter[pIx].direction))
					return false

				++aIx
				++curArity
			}
		}

		return curArity >= parameter[pIx].multiplicityRange.min &&
				parameter.drop(pIx + 1).all { it === result || it.multiplicityRange.min == 0L }
	}
}

private fun checkVariance(signature : List<Type>, argument : List<Type>, variance : FeatureDirectionKind) : Boolean
= when(variance) {
	// FIXME: Are these correct semantics for non-singleton type lists?
	IN -> signature.all { pt -> argument.all { at -> at.specializes(pt) } }
	OUT -> signature.all { pt -> argument.all { at -> pt.specializes(at) } }
	INOUT -> checkVariance(signature, argument, IN) && checkVariance(signature, argument, OUT)
}