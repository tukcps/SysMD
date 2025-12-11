package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.SimpleName

class InvariantImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    expression: String? = null,
    elementType: String = "Invariant",
): Invariant, ExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    typeConstraint = mutableListOf("true"),
    expression = expression,
    elementType = elementType,
) {
    override var isNegated: Boolean = false

    override val type get() = listOfNotNull(model?.repo?.booleanType)


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
}
