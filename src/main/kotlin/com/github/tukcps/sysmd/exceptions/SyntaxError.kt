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
    priority = 3,
    textualRepresentation = parser?.semantics?.textualRepresentation,
    token = parser?.token,
    element = parser?.semantics?.owners?.peek()?.ref
) {
    override val explanation
        get() = SyntaxError.explanation

    companion object {
        val explanation = """
            This error has been caused during parsing the textual representation. 
            Check the syntax around the current token given above. 
            If there is an error before this error, first fix the error before this one.
        """.trimIndent()
    }
}


fun KerML.throwSyntaxError(
    message: String,
) {
    throw SyntaxError(parser = this, message = message)
}