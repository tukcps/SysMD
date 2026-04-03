package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.MetadataAccessExpression
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.MembershipImplementation
import com.github.tukcps.sysmd.model.util.*

class MetadataAccessExpressionImplementation(
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	typeConstraint: MutableList<String> = mutableListOf(),
	expression: String? = null,
	elementType: String = "MetadataAccessExpression"
) : MetadataAccessExpression, ExpressionImplementation(
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	typeConstraint = typeConstraint,
	expression = expression,
	elementType = elementType)
{
	override val metaclassFeature : MetadataFeature
		get() = TODO("Not yet implemented")

	override val isModelLevelEvaluable : Boolean get() = true

	override var referencedElement : Element?
		get() = super.referencedElement
		set(value) {
			if(value !== null)
			{
				assert(referencedElement === null)
				value.model = this.model
				model!!.addOwnedRelationship(MembershipImplementation(
					memberElement = value,
					membershipOwningNamespace = this
				))
			}
		}

	override fun learnType() : List<Type> = emptyList() // TODO

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
		b.append(localIdentifier(referencedElement))
		b.append(".metadata")
	}

	override fun clone() = MetadataAccessExpressionImplementation(
		declaredName = declaredName,
		declaredShortName = declaredShortName,
		typeConstraint = typeConstraint,
		expression = expression
	).also {
		it.updateFrom(this)
	}

	override fun updateFrom(template : Element)
	{
		super.updateFrom(template)

		if(template is MetadataAccessExpression && referencedElement === null)
			this.referencedElement = template.referencedElement
	}
}