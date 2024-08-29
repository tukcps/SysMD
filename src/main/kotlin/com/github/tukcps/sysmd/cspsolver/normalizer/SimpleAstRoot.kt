package com.github.tukcps.sysmd.cspsolver.normalizer

import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.AstRoot

/**
 * Simpler version of [AstRoot].
 */
data class SimpleAstRoot (
    val originalAst : AstNode,
) {

    var dependency: AstNode = originalAst

    fun toExpressionString(): String {
        return dependency.toExpressionString()
    }

}