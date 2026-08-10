package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.OperatorExpression
import com.github.tukcps.sysmd.model.expression.functions.AstFunction
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class OperatorExpressionImplementation(
	model: Session,
	elementId : Uuid = Uuid.random(),
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	expression: String? = null,
) : OperatorExpression, InvocationExpressionImplementation(
	model,
	elementId = elementId,
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	expression = expression,
)
{
	override var operatorAst: AstFunction? = null

	override fun clone() = OperatorExpressionImplementation(
		model,
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		expression = expression,
	).also {
		it.updateFrom(this)
	}

	override fun updateFrom(template : Element)
	{
		super.updateFrom(template)

		if(template is OperatorExpressionImplementation)
		{
			operator = template.operator
			operatorAst = template.operatorAst
		}
	}

	//InstantiatedType = Resolution of its operator
	@Deprecated("BaseFunction, DataFunctions, ControlFunctions not yet implemented")
	override fun instantiatedType() : Type? {
		operator?.let {
			return model.global.resolve(operator!!)?.member<Function>() //returns null if not initialized/not resolved
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
		val cl = CallLikeOperatorInformation[operator]

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

				val (la,ra) = when {
					bin.abelian -> Pair(0,0)
					bin.rightAssociative -> Pair(1,0)
					else -> Pair(0,1)
				}

				argument[0].toAstString(b, bin.precedence + la)
				if(bin.leftSpace)
					b.append(' ')
				b.append(operator)
				if(bin.rightSpace)
					b.append(' ')
				argument[1].toAstString(b, bin.precedence + ra)

				if(pars)
					b.append(')')
			}
			2 if cl !== null -> {
				operator!!
				argument[0].toAstString(b, 0)
				b.append(cl.left)
				argument[1].toAstString(b, -10) // unpack sequences
				b.append(cl.right)
			}
			1 if un !== null -> {
				operator!!
				b.append(operator)

				if(operator.last().isLetter())
					b.append(' ')

				argument[0].toAstString(b, Int.MAX_VALUE)
			}

			else -> super.toAstString(b, precedence)
		}

	}
}