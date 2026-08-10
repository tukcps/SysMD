package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.BodyExpression
import com.github.tukcps.sysmd.model.generated.elementType
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class BodyExpressionImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	expression: String? = null,
) : BodyExpression, ExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression,
)
{
	override fun learnType() : List<Type> = resultExpression!!.type

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

		for(arg in inputParameters)
		{
			b.append(' ')
			b.append(arg.direction?.name?.lowercase() ?: "inout") // shouldn't be null
			b.append(' ')
			b.append(arg.name)
			b.append(" : ")
			b.append(arg.elementType().name)
			// fixme: bindings?
			b.append(";")
		}

		returnParameter?.let {
			b.append(" return ")
			if(it.name !== null)
				b.append(it.name)
			b.append(" : ")
			b.append(it.elementType().name)
			// fixme: bindings
			b.append("; ")
		}

		b.append(' ')
		resultExpression?.toAstString(b, 0) ?: b.append("(?)")
		b.append(" }")
	}

	override fun clone() = BodyExpressionImplementation(
		model,
		declaredName = declaredName,
		declaredShortName = declaredShortName,
		expression = expression,
	).also {
		it.updateFrom(this)
	}
}