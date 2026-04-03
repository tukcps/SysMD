package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConditionalExpressionActions
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.session.Session
import java.util.*
import java.util.UUID.randomUUID

/**
 * Builds an Expression tree that selects one of the arguments.
 */
fun buildOneOfAst(
    model: Session,
    args: ArrayList<AstNode>,
    semantics: ActionsContext
): AstNode  {
    val id: UUID = randomUUID()
    var s1 = args.first()
    var decVarCounter = 1
    args.forEach { s2 ->
        if (s2 != s1 ) {
            val cond = AstLeaf(
                model,
                Quantity(
                    model.builder.variable(
                        "${id}::EnumDecision-$decVarCounter",
                        id.toString(),
                        true
                    )
                )
            )
            decVarCounter++
            s1 = ConditionalExpressionActions(semantics, cond, s1, s2).run()
        }
    }
    return s1
}