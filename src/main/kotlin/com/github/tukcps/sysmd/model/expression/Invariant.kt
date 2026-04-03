package com.github.tukcps.sysmd.model.expression


/**
 * An invariant is an expression of type Boolean that must always evaluate to "True";
 * or to "False" if isNegates is true
 */
interface Invariant: BooleanExpression {
    var isNegated: Boolean

    override fun clone(): Invariant
}