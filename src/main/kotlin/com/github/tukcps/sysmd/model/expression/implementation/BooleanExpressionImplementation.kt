package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.BooleanExpression
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class BooleanExpressionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
): BooleanExpression, ExpressionImplementation(model,elementId = elementId) {
    override fun clone(): BooleanExpressionImplementation {
        TODO("Not yet implemented")
    }

    /** Initializes this expression subtree recursively.
     * Assigns default domains to `upQuantity` and `downQuantity` of the correct types for this AST.
     * Does not search for properties etc. in the symbol table as these might not be declared.
     */
    override fun initialize() {
        TODO("Not yet implemented")
    }

    override fun evalUp() {
        TODO("Not yet implemented")
    }

    override fun evalDown() {
        TODO("Not yet implemented")
    }

    override fun toAstString(b: StringBuilder, precedence: Int) {
        TODO("Not yet implemented")
    }

    override fun learnType(): List<com.github.tukcps.sysmd.model.kerml.Type> {
        TODO("Not yet implemented")
    }
}