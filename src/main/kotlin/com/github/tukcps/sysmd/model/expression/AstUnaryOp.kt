package com.github.tukcps.sysmd.model.expression

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.model.expression.functions.AstNot
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.MINUS
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.PLUS
import com.github.tukcps.sysmd.quantities.VectorQuantity

internal class AstUnaryOp(
    val op: Token.Kind,
    val operand: AstNode
): AstNode(operand.model) {

    override var root: AstNode? = null
        set(value) {
            field = value
            operand.root = value
        }

    override fun initialize() {
        upQuantity = when (operand.upQuantity.values[0]) {
            is AADD -> VectorQuantity(mutableListOf(model.builder.Reals), "")
            is IDD -> VectorQuantity(mutableListOf(model.builder.Integers))
            else -> throw SemanticError("unary minus argument must be of type Integer or Real")
        }
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUpRec() {
        operand.evalUpRec()
        evalUp()
    }

    override fun evalUp() {
        upQuantity = when(op) {
            MINUS -> operand.upQuantity.negate()
            PLUS -> operand.upQuantity.clone()
            else -> { throw SemanticError("Problem with unary operation")
            }
        }
    }

    override fun evalDownRec() {
        evalDown()
        operand.evalDownRec()
    }

    /** No propagation; discrete solver handles Boolean functions */
    override fun evalDown() {
        when(op) {
            MINUS -> operand.downQuantity = downQuantity.negate()
            PLUS -> operand.downQuantity = downQuantity.clone()
            else -> throw SemanticError("unary minus only supported for Integer or Real")
        }
    }

    override fun <R> runDepthFirst(block: AstNode.() -> R): R {
        operand.runDepthFirst(block)
        return this.run(block)
    }

    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R =
        with(receiver) {
            withDepthFirst(operand, block)
            return block()
        }

    override fun toExpressionString(): String {
        return "-${operand.toExpressionString()}"
    }

    override fun clone(): AstNot {
        val parClone = arrayListOf(operand.clone())
        return AstNot(model, parClone)
    }
}