package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.expression.FeatureReferenceExpression
import com.github.tukcps.sysmd.model.expression.checkEvent
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.StrDD
import kotlin.properties.Delegates.observable

class FeatureReferenceExpressionImplementation(
	declaredName : SimpleName? = null,
	declaredShortName : SimpleName? = null,
	direction : Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
	isEnd : Boolean = false,
	typeConstraint : MutableList<String> = mutableListOf(),
	expression : String? = null,
	elementType : String = "Expression"
) : FeatureReferenceExpression, ExpressionImplementation(
	declaredName,
	declaredShortName,
	direction,
	isEnd,
	typeConstraint,
	expression,
	elementType
)
{
	/** Identifier of the referenced feature */
	override var identifier : QualifiedName? by observable(null) { _, _, _ ->
		_referent = null
	}

	/** Backing field for `referent` (implicit backing field cannot be mutable) */
	private var _referent : Feature? = null

	// FIXME: Is variable more fitting for our implementation?
	/** The feature being referenced. `null` if unset or not ready to resolve.  */
	override val referent : Feature? get() {
		_referent?.let { return it }
		val identifier = this.identifier ?:
				return null

		// TODO: AstLeaf has a reference to a namespace
		val feature =  (owningNamespace ?: model?.global)?.resolve(identifier)?.member<Feature>()
				?: throw IllegalStateException("Name '$identifier' doesn't reference any feature")

		_referent = feature

		return feature
	}

	override val type get() =
		referent?.type ?: emptyList()

	override fun initialize()
	{
		// FIXME: fallback only in place for testing; throw IllegalStateException instead
		upQuantity = referent?.variable?.vectorQuantity ?: VectorQuantity(model!!.builder.Integers)

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

				if (v.feature.isSufficient)
				{
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
						model!!.status.warn(Issue.Kind.WARN_INCONSISTENCY,
							"Cannot be satisfied for all values.", element =  variable?.feature)
					}
				}

				v.checkEvent()
			}
			is IDD -> {
				v.vectorQuantity = downQuantity.constrain(v.vectorQuantity)//.clone()

				if(v.feature.isSufficient)
				{
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
						model!!.status.warn(Issue.Kind.WARN_INCONSISTENCY,
							"Cannot be satisfied for all values.", element =  variable?.feature)
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
		b.append(identifier)
	}

	// TODO
}