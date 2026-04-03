package com.github.tukcps.sysmd.model.expression

/** SysMD extension.
 * A name that is resolved dynamically during evaluation, instead of statically during initialization.
 * Occurs only in some legacy function invocations (e.g. sumOverParts, sumOverSubclasses, etc.)
 *
 * May be replaced by syntactic rewriting in future
 */
interface RawNameExpression : Expression {
    /** The unresolved identifier given in the input */
    val rawName : String?

    override fun clone(): RawNameExpression
}