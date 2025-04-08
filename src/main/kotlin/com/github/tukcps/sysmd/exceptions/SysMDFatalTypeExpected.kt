package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.model.kerml.Element


/**
 * In the model, a type is expected in a particular position.
 * There is an element, but it is not a type.
 */
class SysMDFatalTypeExpected (
    message: String,
    input: CharSequence? = null,
    token: Token? = null,
    element: Element? = null,
    cause: Throwable? = null
) : SysMDException(
    message = message,
    input = input,
    token = token,
    kind = Issue.Kind.FATAL,
    element = element,
    cause = cause,
)