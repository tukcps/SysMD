package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.UnresolvedType
import com.github.tukcps.sysmd.model.util.SimpleName

class InvariantImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null,
    elementType: String = "Invariant",
): Invariant, ExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = mutableListOf("true"),
    expression = expression,
    elementType = elementType,
) {
    override var isNegated: Boolean = false

	override fun learnType() = listOf(model?.repo?.booleanType ?: UnresolvedType("ScalarValues::Boolean"))

	override fun initialize()
	{
		val result = this.result ?: return

		result.initialize()
		upQuantity = result.upQuantity
		downQuantity = result.downQuantity
	}

	override fun evalDown()
	{
		val result = this.result ?: return

		result.evalDown()
		downQuantity = result.downQuantity
	}

	override fun evalUp()
	{
		val result = this.result ?: return

		result.evalUp()
		upQuantity = result.upQuantity
	}

	override fun toAstString(b : StringBuilder, precedence : Int)
	{
		println("invariant ${!isNegated}")

		name?.let {
			b.append(' ')
			b.append(it)
		}

		this.result?.let {
			b.append("{ ")
			it.toAstString(b, 0)
			b.append(" }")
		}
	}


	override fun clone() = InvariantImplementation(
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		expression = expression,
		elementType = elementType,
	).also {
		it.updateFrom(this)
	}
}
