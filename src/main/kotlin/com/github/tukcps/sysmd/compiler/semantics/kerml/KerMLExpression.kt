package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
//import com.github.tukcps.sysmd.compiler.semantics.ActionsContextImplementation
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.ConstructorExpression
import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.InstantiationExpression
import com.github.tukcps.sysmd.model.expression.InvocationExpression
import com.github.tukcps.sysmd.model.expression.OperatorExpression
import com.github.tukcps.sysmd.model.expression.functions.AstIte
import com.github.tukcps.sysmd.model.util.SimpleName

//import com.github.tukcps.sysmd.compiler.semantics.ActionsContext

/**
 * Generates an ITE-Statement from an if-else expression.
 */
class ConditionalExpressionActions(
    var context: ActionsContext,
    var condExpr: AstNode? = null,
    var thenExpr: AstNode? = null,
    var elseExpr: AstNode? = null,
) {
    internal fun run(): AstIte {
        return AstIte(context.model, arrayListOf(condExpr!!, thenExpr!!, elseExpr!!))
    }
}

open class ExpressionActions<T: Expression>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: String = "Base::Anything",
    valuePart: AstRoot? = null
    //production: FeatureActions<T>.() -> Unit,
    ): FeatureActions<T>(context, creator, defaultType, valuePart
    ) {
        //TODO!
    }

//Base semantic functions for Constructor- and Operator(Invocation)Expressions
abstract class InstantiationExpressionActions<T: InstantiationExpression>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: String = "Base::Anything",
    valuePart: AstRoot? = null
    //production: FeatureActions<T>.() -> Unit,
): FeatureActions<T>(context, creator, defaultType, valuePart
) {
    //TODO!
}

open class ConstructorExpressionActions<T: ConstructorExpression>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: String = "Base::Anything",
    valuePart: AstRoot? = null
    //production: FeatureActions<T>.() -> Unit,
): FeatureActions<T>(context, creator, defaultType, valuePart
) {
    //TODO!
}

open class InvocationExpressionActions<T: InvocationExpression>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: String = "Base::Anything",
    valuePart: AstRoot? = null
    //production: FeatureActions<T>.() -> Unit,
): FeatureActions<T>(context, creator, defaultType, valuePart
) {
    //TODO!
}

open class OperatorExpressionActions<T: OperatorExpression>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: String = "Base::Anything",
    valuePart: AstRoot? = null
    //production: FeatureActions<T>.() -> Unit,
): FeatureActions<T>(context, creator, defaultType, valuePart
) {
    //TODO!
}
