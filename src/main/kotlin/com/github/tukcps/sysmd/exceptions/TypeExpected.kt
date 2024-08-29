package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.compiler.scanner.Token


/**
 * In the model, a type is expected in a particular position.
 * There is an element, but it is not a type.
 */
class TypeExpected (
    message: String,
    textualRepresentation: TextualRepresentation? = null,
    token: Token? = null,
    element: Element? = null,
    cause: Throwable? = null
) : SysMDException(
    message = message,
    textualRepresentation = textualRepresentation,
    token = token,
    element = element,
    cause = cause,
    priority = 3,
){
    companion object {
        val explanation = """
            In the model, a type is expected in a particular position. 
            There is an element, but it is not a type. 
        """.trimIndent()
    }
}