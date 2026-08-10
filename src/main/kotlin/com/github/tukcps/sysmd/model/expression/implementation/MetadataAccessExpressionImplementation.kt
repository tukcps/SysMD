package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.MetadataAccessExpression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.MetadataFeature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.MembershipImplementation
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class MetadataAccessExpressionImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	declaredName: SimpleName? = null,
	declaredShortName: SimpleName? = null,
	expression: String? = null,
) : MetadataAccessExpression, ExpressionImplementation(
	model,
	elementId = elementId,
	declaredName = declaredName,
	declaredShortName = declaredShortName,
	expression = expression
) {
	override val metaclassFeature : MetadataFeature
		get() = TODO("Not yet implemented")

	override val isModelLevelEvaluable : Boolean get() = true

	override var referencedElement : Element?
		get() = super.referencedElement
		set(value) {
			if(value !== null)
			{
				assert(referencedElement === null)
				model.addOwnedRelationship(MembershipImplementation(
					model,
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
		model,
		declaredName = declaredName,
		declaredShortName = declaredShortName,
		expression = expression
	).also {
		it.updateFrom(this)
	}
}