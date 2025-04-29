package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.KerML


/**
 * This Exception is thrown for all errors that are caused in the parsing methods.
 * It is thrown from the parser from which textual representation and current token are retrieved.
 */
class SyntaxError(
    parser: KerML,
    message: String,
    kind: Issue.Kind = Issue.Kind.ERROR_SYNTACTICAL,
) : SysMDError(
    message = message,
    kind = kind,
    input = parser.input,
    token = parser.token,
    element = parser.semantics.owners.peek()?.ref,
)


fun KerML.throwSyntaxError(
    message: String,
    kind: Issue.Kind = Issue.Kind.ERROR_SYNTACTICAL,
) {
    throw SyntaxError(parser = this, message = message, kind)
}