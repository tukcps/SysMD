package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.model.kerml.Element


/**
 * General error class that is also used for persisting the error list.
 * Each entry has:
 * @param message Mandatory textual description
 * @param textualRepresentation the textual representation in which the error has occurred
 * @param token the token where the error has occurred
 * @param element the element in which the error has occurred
 * @param
 */
open class SysMDInfo(
    message: String,
    textualRepresentation: String? = null,
    token: Token? = null,
    element: Element? = null,
    cause: Throwable? = null
): SysMDException(message, textualRepresentation, token, kind = Issue.Kind.INFO, element = element, cause = cause)
