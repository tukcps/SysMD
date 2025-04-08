package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.KerML


/**
 * This Exception is thrown for all errors that are caused in the parsing methods.
 * It is thrown from the parser from which textual representation and current token are retrieved.
 */
class SyntaxError(
    parser: KerML? = null,
    message: String,
) : SysMDError(
    message = message,
    kind = Issue.Kind.ERROR_SYNTACTICAL,
    input = parser?.input,
    token = parser?.token,
    element = parser?.semantics?.owners?.peek()?.ref,
)


fun KerML.throwSyntaxError(
    message: String,
) {
    throw SyntaxError(parser = this, message = message)
}