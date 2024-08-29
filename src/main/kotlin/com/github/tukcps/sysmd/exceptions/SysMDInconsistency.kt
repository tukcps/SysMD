package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.compiler.scanner.Token


/**
 * A contradiction in the model, deteced usually by constraint propagation.
 */
class SysMDInconsistency (
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
    priority = 1,
)