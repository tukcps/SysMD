package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.compiler.scanner.Token


/**
 * In the model, a type is expected in a particular position.
 * There is an element, but it is not a type.
 */
class FeatureExpected (
    message: String,
    textualRepresentation: TextualRepresentation? = null,
    token: Token? = null,
    element: Element? = null,
    cause: Throwable? = null
) : SemanticError(
    message = message,
    element = element,
    cause = cause,
) {
    companion object {
        val explanation = """
            In the model, a feature kind of element is expected in a particular position. 
            There is an element, but it is not a feature kind of element. 
        """.trimIndent()
    }
}