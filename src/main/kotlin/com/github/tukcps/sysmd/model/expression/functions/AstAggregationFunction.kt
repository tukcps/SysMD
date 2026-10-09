package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind
import com.github.tukcps.sysmd.model.expression.AstBinOp
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.quantities.VectorQuantity
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

    private var cachedGenerated: AstNode? = null
    private var cachedLeafQuantities: List<VectorQuantity>? = null
    private var cachedUpQuantity: VectorQuantity? = null

    /**
     * Evaluates the generated AST upwards and takes over its result.
     * The evaluation is skipped if the generated AST is the same one as before and no leaf has a new quantity since
     * the last call, because quantities are immutable. A rebuilt AST is always evaluated.
     */
    protected fun evalUpGenerated(generated: AstNode) {
        val leafQuantities = generated.getLeaves().map { it.variable?.vectorQuantity ?: it.upQuantity }
        val previous = cachedLeafQuantities
        if (previous != null && generated === cachedGenerated && upQuantity === cachedUpQuantity && previous.size == leafQuantities.size &&
            previous.indices.all { previous[it] === leafQuantities[it] }
        ) return
        generated.evalUpRec()
        upQuantity = generated.upQuantity
        cachedGenerated = generated
        cachedLeafQuantities = leafQuantities
        cachedUpQuantity = upQuantity
    }
}

/**
 * Builds a balanced binary tree from a list of operands using divide-and-conquer.
 * This is the main function used during AST construction to build balanced trees directly.
 */
fun buildBalancedTree(operands: List<AstNode>, op: Kind): AstNode {
    require(operands.isNotEmpty()) { "Cannot build a tree without operands" }
    if (operands.size == 1) return operands[0]
    if (operands.size == 2) return AstBinOp(operands[0], op, operands[1])
    
    // Split the list in half and build balanced subtrees
    val mid = operands.size / 2
    val leftSubtree = buildBalancedTree(operands.subList(0, mid), op)
    val rightSubtree = buildBalancedTree(operands.subList(mid, operands.size), op)
    
    return AstBinOp(leftSubtree, op, rightSubtree)
}
