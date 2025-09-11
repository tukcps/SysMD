package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.InstantiationExpression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.ParameterMembership
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.util.SimpleName

abstract class InstantiationExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    textualRepresentation:  MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "InstantiationExpression"
) : InstantiationExpression, ExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    textualRepresentation = textualRepresentation,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
)
{
    override var argument: List<Expression>
	    get() = ownedRelationship.filterIsInstance<ParameterMembership>()
		    .filter { it.parameterDirection == Feature.FeatureDirectionKind.IN } // ?
			.map { it.ownedMemberParameter }
		    // TODO: What about return parameters & result expressions?
			// TODO: Standard says we need to take FeatureValue
			.filterIsInstance<Expression>()
	    set(value) {
		    TODO()
		}

    var instantiatedType: Type = this //FIXME: Hack

    open fun instantiatedType(): Type? = this //FIXME: Should be self?

    //FIXME: Necessary to also implement TypeImplementation via composition or is this covered by the fact that expression already implements type?
    var internalType: TypeImplementation = this as TypeImplementation

}