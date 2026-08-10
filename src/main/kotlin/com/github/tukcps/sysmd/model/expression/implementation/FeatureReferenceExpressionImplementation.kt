package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.expression.FeatureReferenceExpression
import com.github.tukcps.sysmd.model.expression.checkEvent
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.MembershipImplementation
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.StrDD
import kotlin.uuid.Uuid

class FeatureReferenceExpressionImplementation(
	model : Session,
	elementId : Uuid = Uuid.random(),
	declaredName : SimpleName? = null,
	declaredShortName : SimpleName? = null,
	expression : String? = null,
) : FeatureReferenceExpression, ExpressionImplementation(
	model,
	elementId = elementId,
	declaredName,
	declaredShortName,
	expression
) {
	/** Setter as shorthand for initialization, won't work right if referent already set */
	override var referent: Feature?
		get() = super.referent
		set(value) {
			if(value !== null)
			{
				assert(referent === null)
				model.addOwnedRelationship(MembershipImplementation(
					model,
					memberElement = value,
					membershipOwningNamespace = this
				))
			}
		}

	override fun visibleMemberships(
		excluded: Set<Namespace>,
		isRecursive: Boolean,
		includeAll: Boolean,
		filter: Membership.() -> Boolean
	): List<Membership> {
		// the membership containing referent. Prevents an Unresolved referent from being resolved if included.
		val exclude = ownedMembership.firstOrNull { it !is ParameterMembership }
		return super.visibleMemberships(excluded, isRecursive, includeAll) { this !== exclude && filter() }
	}

	override fun learnType() : List<Type> = referent?.type ?: emptyList() // FIXME: Should report error here?

	override fun initialize()
	{
		// FIXME: fallback only in place for testing; throw IllegalStateException instead
		upQuantity = referent?.variable?.vectorQuantity ?: VectorQuantity(model.builder.Integers)

		downQuantity = upQuantity
	}

	override fun evalUp()
	{
		// FIXME: fallback only in place for testing
		upQuantity = referent?.variable?.vectorQuantity ?: downQuantity
	}

	override fun evalDown()
	{
		val v = referent?.variable ?: return

		when(downQuantity.value)
		{
			is AADD -> {
				// TODO: check is only hot fix ... (?)
				if (! (downQuantity.value.asAadd().maxIsInf && downQuantity.value.asAadd().minIsInf) ) {
					v.vectorQuantity = downQuantity.constrain(
						v.vectorQuantity,
						v.rangeSpecs,
						v.unitSpec
					)
				}

				if (v.satisfyAll) {
					if(v.rangeSpecs.size != downQuantity.values.size && v.rangeSpecs.size != 1)
					{
						throw VectorDimensionError("Vector size of ${downQuantity.values.size} does not match " +
								"Constraint size of ${v.rangeSpecs.size}")
					}
					if(v.rangeSpecs.size == downQuantity.values.size &&
						v.rangeSpecs.zip(downQuantity.values).any { (s,q) ->
							s !in (q as AADD).getRange()
						})
					{
						model.status.warn(
							Issue.Kind.WARN_INCONSISTENCY, "Cannot be satisfied for all values.",
							elementId = variable?.relatedElement
						)
					}
				}

				v.checkEvent()
			}
			is IDD -> {
				v.vectorQuantity = downQuantity.constrain(v.vectorQuantity)//.clone()

				if(v.satisfyAll) {
					if(v.intSpecs.size != downQuantity.values.size && v.rangeSpecs.size != 1)
					{
						throw VectorDimensionError("Vector size of ${downQuantity.values.size} does not match " +
								"Constraint size of ${v.rangeSpecs.size}")
					}
					if(v.intSpecs.size == downQuantity.values.size &&
						v.intSpecs.zip(downQuantity.values).any { (s,q) ->
							s !in (q as IDD).getRange()
						})
					{
						model.status.warn(Issue.Kind.WARN_INCONSISTENCY,
							"Cannot be satisfied for all values.", elementId =  variable?.relatedElement)
					}
				}

				v.checkEvent()
			}
			is BDD -> {}
			is StrDD -> {
				v.vectorQuantity = downQuantity.constrainString(v.vectorQuantity)
				v.checkEvent()
			}
			else -> throw IllegalStateException("Unexpected DD type ${downQuantity.value.javaClass}")
		}
	}

	override fun toAstString(b : StringBuilder, precedence : Int)
	{
		b.append(localIdentifier(referent))
	}

	override fun clone() = FeatureReferenceExpressionImplementation(
		model,
		declaredName= declaredName,
		declaredShortName = declaredShortName,
		expression = expression,
	).also {
		it.updateFrom(this)
	}
	
	// TODO
}