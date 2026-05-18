package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind
import com.github.tukcps.sysmd.model.expression.AstBinOp
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.services.session.Session

/**
 * A function call of a user-defined function.
 */
abstract class AstAggregationFunction(
    name: String,
    model: Session
) :
    AstFunction(name, model, 0, ArrayList()) {
    abstract fun getDependentPropertyStrings(): Set<String>
}

/**
 * Builds a balanced binary tree from a list of operands using divide-and-conquer.
 * This is the main function used during AST construction to build balanced trees directly.
 */
fun buildBalancedTree(operands: List<AstNode>, op: Kind): AstNode {
    if (operands.size == 1) return operands[0]
    if (operands.size == 2) return AstBinOp(operands[0], op, operands[1])
    
    // Split the list in half and build balanced subtrees
    val mid = operands.size / 2
    val leftSubtree = buildBalancedTree(operands.subList(0, mid), op)
    val rightSubtree = buildBalancedTree(operands.subList(mid, operands.size), op)
    
    return AstBinOp(leftSubtree, op, rightSubtree)
}
