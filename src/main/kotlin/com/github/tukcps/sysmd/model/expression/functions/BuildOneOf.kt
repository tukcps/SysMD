package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.session.Session

fun buildOneOfAst(
    model: Session,
    expression: Feature,
    args: ArrayList<AstNode>,
    semantics: SemanticActions
): AstNode  {
    var s1 = args.first()
    var decVarCounter = 1
    args.forEach { s2 ->
        val cond = AstLeaf(model, Quantity(model.builder.variable("${expression.elementId}::EnumDecision-$decVarCounter", expression.elementId.toString(), true)))
        decVarCounter++
        s1 = semantics.conditionalExpressionActions(cond, s1, s2)!!.run()
    }
    return s1
}