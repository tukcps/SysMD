package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.InvocationExpression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.SimpleName

/** ref. 8.3.4.8.5 */
open class InvocationExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "InvocationExpression"
) : InvocationExpression, InstantiationExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
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

    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean {
        if(this in visited)
            return false

        val ext = visited + this

        return argument.all { it.modelLevelEvaluable(ext) } && (function?.isModelLevelEvaluable ?: false)
    }

    override fun clone() = InvocationExpressionImplementation(
        declaredName= declaredName,
        declaredShortName = declaredShortName,
        typeConstraint = typeConstraint,
        expression = expression,
        elementType = elementType,
    ).also {
        it.updateFrom(this)
    }
}