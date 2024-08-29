package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.compiler.scanner.Token


/**
 * Exception for an Element not found, where an element can be an arbitrary entity.
 * @param name id or name used as string for reporting the error
 */
class ElementNotFoundException private constructor(
    textualRepresentation: TextualRepresentation?,
    element: Element? = null,
    name: String? = null,
    message: String? = null,
    token: Token? = null
) : SysMDError(
    message = message?: "Element '$name' not found",
    textualRepresentation = textualRepresentation?:element?.textualRepresentation?.firstOrNull(),
    element = element,
    token = token
) {
    constructor(element: Element?, name: String)
            : this(null, element, name, null)

    init {
        priority = 2
    }
}
