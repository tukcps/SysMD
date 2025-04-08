package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.compiler.scanner.Token

/**
 * An internal error that is usually a bug.
 * Can occur e.g. when the internal model representation is checked, and an
 * inconsistency is detected.
 */
class SysMDFatalInternalError  (
    message: String,
    token: Token? = null,
    element: Element? = null,
    cause: Throwable? = null
) : SysMDException(
    message = message,
    token = token,
    element = element,
    cause = cause,
)