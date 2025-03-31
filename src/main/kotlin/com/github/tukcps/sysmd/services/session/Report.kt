package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.exceptions.*
import com.github.tukcps.sysmd.model.kerml.Element


/**
 * The error reporting function adds error messages to the status.
 * If the flag catchExceptions is set to false, an exception is thrown.
 * The function uses also the value of lineNo to add a line number to the error message, if present.
 * Avoid use of this function; better directly pass the exception instead.
 * @param element The element in which the error occurred.
 * @param message A descriptive error message.
 */
fun Session.reportInfo(element: Element, message: String) {
    val textualRepresentation = element.textualRepresentation
    report(SysMDInfo(
        element=element,
        textualRepresentation = textualRepresentation.firstOrNull(),
        message = message))
}


/**
 * The error reporting function adds error messages to the status.
 * If the flag catchExceptions is set to false, an exception is thrown.
 * The function uses also the value of lineNo to add a line number to the error message, if present.
 * Avoid use of this function; better directly pass the exception instead.
 * @param exception the exception that caused the error
 */
fun Session.report(exception: Throwable) {
    if (exception is SysMDException) {
        status.exceptions.add(exception)
    } else
        status.exceptions.add(InternalError("Internal error: ${exception.message}", exception))
}


//Use only if not related to a concrete element, as no details about the element are passed
fun Session.report(message: String) {
    status.exceptions.add(SysMDError(message = message))
}

/**
 * Method to add an issue to the list of a model's issues.
 * @param element the element with the issue
 * @param message a textual message explaining the issue
 * @param cause a Throwable that holds among others the stack trace
 */
fun Session.report(element: Element?, message: String, cause: Throwable? = null) {
    val textualRepresentation = element?.textualRepresentation?.firstOrNull()
    status.exceptions.add(
        if (cause is SysMDException) {
            cause.element = element
            cause
        } else SysMDError(
            textualRepresentation = textualRepresentation,
            message = message,
            element = element,
            cause = cause
        )
    )
}


/**
 * Method to add a SysMD Exception to the list of a model's issues.
 * @param cause a SysMD Exception that shall include an error message, related textual representation, and element.
 */
fun Session.report(cause: SysMDException) {
    status.exceptions.add(cause)
}


/**
 * Method to add an issue to the list of a model's issues.
 * @param element the element with the issue
 * @param message a textual message explaining the issue
 */
fun Session.reportInconsistency(element: Element, message: String) {
    val textualRepresentation = element.textualRepresentation
    status.exceptions.add(SysMDInconsistency(
        element = element,
        message = "$message in element ${element.declaredName}",
        textualRepresentation = textualRepresentation.firstOrNull())
    )
}

