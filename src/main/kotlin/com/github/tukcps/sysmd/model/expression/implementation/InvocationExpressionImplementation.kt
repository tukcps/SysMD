package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.InvocationExpression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.SimpleName

/** ref. 8.3.4.8.5 */
open class InvocationExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "InvocationExpression"
) : InvocationExpression, InstantiationExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    override fun toAstString(b : StringBuilder, precedence : Int)
    {
        b.append(functionName)
        b.append("(")
        for((i, p) in argument.withIndex())
        {
            if(i > 0)
                b.append(", ")

            p.toAstString(b, 0)
        }
        b.append(")")
    }
}