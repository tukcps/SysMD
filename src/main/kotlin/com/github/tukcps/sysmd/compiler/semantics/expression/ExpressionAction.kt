package com.github.tukcps.sysmd.compiler.semantics.expression

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.expression.implementation.OperatorInformation
import com.github.tukcps.sysmd.model.generated.ElementType

/** Semantic actions for expressions.
 * (!) All OwningMemberships have to be created manually
 */
open class ExpressionAction(
    context: ActionsContext, type : ElementType, // isImplicit : String = "Performances::Evaluation" // TODO
) : FeatureAction(context, type = type, owningMembershipType = null)

open class InvocationExpressionAction(
    context: ActionsContext, type : ElementType = ElementType.InvocationExpression
) : ExpressionAction(context, type = type)
{
    /** The function to call */
    var function
        get() = element.functionName
        set(x) {
            element.functionName = x
        }

    override fun afterProduction() {
        // fixme: there are cases where an invocation is confused for a feature reference...
        //if(element.functionName === null)
        //    throw SyntaxError(context.compiler, "Internal error: InvocationExpression's function wasn't initialized during parse", Issue.Kind.FATAL)

        super.afterProduction()
    }

    fun addArgument(expr : ElementData) = context.addOwnedElement(expr, ElementType.ParameterMembership)

}

open class OperatorExpressionAction(
    context: ActionsContext,
    val operator : String,
    val opInfo : OperatorInformation = OperatorInformation[operator] ?: throw IllegalArgumentException("Unknown operator $operator"),
    type : ElementType = ElementType.OperatorExpression
) : InvocationExpressionAction(context, type = type)
{
    override fun afterProduction() {
        this.function = "${opInfo.namespace}::$operator"
        element.operator = operator

        super.afterProduction()
    }
}