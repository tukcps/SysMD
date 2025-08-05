package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.functions.AstIte
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext

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
