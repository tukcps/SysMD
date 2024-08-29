package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.compiler.scanner.Token

/**
 * An internal error that is usually a bug.
 * Can occur e.g. when the internal model representation is checked, and an
 * inconsistency is detected.
 */
class SysMDInternalError  (
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
    priority = 100,
)