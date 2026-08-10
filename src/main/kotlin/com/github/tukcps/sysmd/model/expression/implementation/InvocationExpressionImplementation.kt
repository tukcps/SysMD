package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.InvocationExpression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.Unresolved
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/** ref. 8.3.4.8.5 */
open class InvocationExpressionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null,
) : InvocationExpression, InstantiationExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression,
) {
    override fun toAstString(b : StringBuilder, precedence : Int)
    {
        b.append(functionName) // generating a relative name could be nicer
        b.append("(")
        var head = true

        for(arg in positionalArguments)
        {
            if(! head)
                b.append(", ")

            head = false

            if(arg is Expression)
            {
                arg.toAstString(b, 0)
                continue
            }

            arg.featureValue?.let {
                it.toAstString(b, 0)
                continue
            }

            b.append(arg.qualifiedName ?: "???") // fallback, this happens e.g. for type references
        }

        if(head) for((feat,arg) in namedArguments)
        {
            if(! head)
                b.append(", ")

            head = false
            b.append((feat as? Unresolved)?.relativeName ?: feat.name ?: feat.shortName)
            b.append(" = ")
            arg.toAstString(b, 0)
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
        model,
        declaredName= declaredName,
        declaredShortName = declaredShortName,
        expression = expression,
    ).also { it.updateFrom(this) }
}