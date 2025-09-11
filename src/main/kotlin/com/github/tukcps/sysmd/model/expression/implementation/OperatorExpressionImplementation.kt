package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.OperatorExpression
import com.github.tukcps.sysmd.model.expression.functions.AstFunction
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.resolve.resolve

class OperatorExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    textualRepresentation:  MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "OperatorExpression"
) : OperatorExpression, InvocationExpressionImplementation(
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
    override var operator: String? = null

    //InstantiatedType = Resolution of its operator
    @Deprecated("BaseFunction, DataFunctions, ControlFunctions not yet implemented")
    override fun instantiatedType() : Type? {
        operator?.let {
            return model!!.global.resolve<Function>(operator!!) //returns null if not initialized/not resolved
        }
        return null
    }

    //Non-standard
    override var operatorPrecedence: Array<String>? = null

    override var operatorAst: AstFunction? = null

}