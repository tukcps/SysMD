package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.exceptions.*
import com.github.tukcps.sysmd.model.kerml.Element


/**
 * Method to add a SysMD Exception to the list of a model's issues.
 * @param cause a SysMD Exception that shall include an error message, related textual representation, and element.
 */
@Deprecated("Replace with status.error")
fun Session.report(cause: SysMDException) {
    status.error(message = cause.message, cause = cause)
}


/**
 * Method to add an issue to the list of a model's issues.
 * @param element the element with the issue
 * @param message a textual message explaining the issue
 */
@Deprecated("Replace with status.inconsistency", ReplaceWith("status.inconsistency"))
fun Session.reportInconsistency(element: Element, message: String) {
    status.inconsistency(
        element = element,
        message = "$message in element ${element.declaredName}"
    )
}
