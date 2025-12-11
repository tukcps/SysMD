package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.util.*

class OperatorExpressionImplementation(
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
	isEnd: Boolean = false,
	typeConstraint: MutableList<String> = mutableListOf(),
	expression: String? = null,
	elementType: String = "OperatorExpression"
) : OperatorExpression, InvocationExpressionImplementation(
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	direction = direction,
	isEnd = isEnd,
	typeConstraint = typeConstraint,
	expression = expression,
	elementType = elementType
)
{
	//Non-standard
	override var operatorPrecedence: Array<String>? = null

	override var operatorAst: AstFunction? = null

	override fun clone() = OperatorExpressionImplementation(
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		direction = direction,
		isEnd = isEnd,
		typeConstraint = typeConstraint,
		expression = expression,
		elementType = elementType,
	).also(::postClone).also {
		it.operator = operator
		it.operatorPrecedence = operatorPrecedence
		it.operatorAst = operatorAst
	}

	//InstantiatedType = Resolution of its operator
	@Deprecated("BaseFunction, DataFunctions, ControlFunctions not yet implemented")
	override fun instantiatedType() : Type? {
		operator?.let {
			return model!!.global.resolve(operator!!)?.member<Function>() //returns null if not initialized/not resolved
		}
		return null
	}

	override fun toAstString(b : StringBuilder, precedence : Int)
	{
		val argument = this.argument
		val operator = this.operator
		val tern = TernaryOperatorInformation[operator]
		val bin = BinaryOperatorInformation[operator]
		val un = UnaryOperatorInformation[operator]

		when(argument.size)
		{
			3 if tern !== null -> {
				if(precedence > 0)
					b.append('(')

				b.append(tern.prefix)
				argument[0].toAstString(b, 0)
				b.append(tern.leftInfix)
				argument[1].toAstString(b, 0)
				b.append(tern.rightInfix)
				argument[2].toAstString(b, 0)

				if(precedence > 0)
					b.append(')')
			}
			2 if bin !== null -> {
				val pars = bin.precedence < precedence

				if(pars)
					b.append('(')

				argument[0].toAstString(b, bin.precedence)
				b.append(' ')
				b.append(operator)
				b.append(' ')
				// assume non-commutative operators are left-associative
				argument[1].toAstString(b, bin.precedence + if(bin.abelian) 0 else 1)

				if(pars)
					b.append(')')
			}
			1 if un !== null -> {
				operator!!
				b.append(operator)

				if(operator.last().isLetter())
					b.append(' ')

				argument[0].toAstString(b, Int.MAX_VALUE)
			}

			else -> super<InvocationExpressionImplementation>.toAstString(b, precedence)
		}

	}
}