package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.NullExpression
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName

class NullExpressionImplementation(
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	typeConstraint: MutableList<String> = mutableListOf(),
	expression: String? = null,
	elementType: String = "NullExpression"
) : NullExpression, ExpressionImplementation(
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	typeConstraint = typeConstraint,
	expression = expression,
	elementType = elementType)
{
	/* TODO: A NullExpression must directly or indirectly specialize the base NullExpression Performances::nullEvaluations from the Kernel Semantic Library */

	override fun learnType() : List<Type> = emptyList() // TODO: return `Base::Anything[0]`

	override fun initialize()
	{
	}

	override fun evalUp()
	{
	}

	override fun evalDown()
	{
	}

	override fun toAstString(b : StringBuilder, precedence : Int)
	{
		b.append("null")
	}

	override fun clone() = NullExpressionImplementation(
		declaredName = declaredName,
		declaredShortName = declaredShortName,
		typeConstraint = typeConstraint,
		expression = expression
	).also {
		it.updateFrom(this)
	}
}