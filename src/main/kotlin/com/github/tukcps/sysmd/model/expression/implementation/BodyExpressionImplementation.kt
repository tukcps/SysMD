package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.BodyExpression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName

class BodyExpressionImplementation(
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	typeConstraint: MutableList<String> = mutableListOf(),
	expression: String? = null,
	elementType: String = "BodyExpression",
) : BodyExpression, ExpressionImplementation(
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	typeConstraint = typeConstraint,
	expression = expression,
	elementType = elementType,
)
{
	override fun learnType() : List<Type> = returnExpression!!.type

	override fun initialize()
	{
		TODO("Not yet implemented")
	}

	override fun evalUp()
	{
		TODO("Not yet implemented")
	}

	override fun evalDown()
	{
		TODO("Not yet implemented")
	}

	override fun toAstString(b : StringBuilder, precedence : Int)
	{
		b.append('{')

		for(arg in arguments)
		{
			b.append(' ')
			b.append(arg.direction.name.lowercase())
			b.append(' ')
			b.append(arg.name)
			b.append(" : ")
			b.append(arg.elementType)
			b.append(";")
		}

		b.append(' ')
		returnExpression?.toAstString(b, 0) ?: b.append("(?)")
		b.append(" }")
	}

	override fun clone() = BodyExpressionImplementation(
		declaredName = declaredName,
		declaredShortName = declaredShortName,
		typeConstraint = typeConstraint,
		expression = expression,
		elementType = elementType,
	).also {
		it.updateFrom(this)
	}

	override fun updateFrom(template : Element)
	{
		super.updateFrom(template)

		if(template is BodyExpression)
		{
			for(arg in template.features())
				model!!.addOwnedMember(arg.clone(), this)
		}
	}
}