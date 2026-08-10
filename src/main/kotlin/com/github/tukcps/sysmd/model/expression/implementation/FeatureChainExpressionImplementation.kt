package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.FeatureChainExpression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** '.' has infinitely higher precedence than proper operators, but e.g. `(a.b.c).d.e` still forces parens  */
private const val dotPrecedence = 1000_000

class FeatureChainExpressionImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
) : FeatureChainExpression, OperatorExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
) {
	init {
		operator = "."
	}

	/** Non-Standard. We don't implement FeatureChain as own element type. */
	override var targetFeature : String? = null

	override fun clone() = FeatureChainExpressionImplementation(model).also {
		it.updateFrom(this)
	}

	override fun updateFrom(template : Element)
	{
		super.updateFrom(template)

		if(template is FeatureChainExpressionImplementation)
			this.targetFeature = template.targetFeature
	}

	override fun toAstString(b : StringBuilder, precedence : Int)
	{
		val source = source
		val targetFeature = targetFeature

		if(source === null || targetFeature === null)
			return super<OperatorExpressionImplementation>.toAstString(b, precedence)

		val par = dotPrecedence < precedence // i.e. parent is also FeatureChainExpression

		if(par)
			b.append('(')

		source.toAstString(b, dotPrecedence + 1)

		b.append('.')
		b.append(targetFeature)

		if(par)
			b.append(')')
	}
}