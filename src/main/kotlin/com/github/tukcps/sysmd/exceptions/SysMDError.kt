package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Scanner
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation


/**
 * General error class that is also used for persisting the error list.
 * Each entry has:
 * @param message Mandatory textual description
 * @param textualRepresentation the textual representation in which the error has occurred
 * @param token the token where the error has occurred
 * @param element the element in which the error has occurred
 * @param
 */
open class SysMDError(
    message: String,
    textualRepresentation: TextualRepresentation? = null,
    token: Token? = null,
    element: Element? = null,
    cause: Throwable? = null,
    priority: Int = 2
) : SysMDException(message, textualRepresentation, token, element, cause, priority = priority)


/**
 * This Exception is thrown for all errors that are caused in the parsing methods.
 * It just writes an error message. Line and column are optional.
 */
class LexicalError(
    scanner: Scanner,
    message: String,
) : SysMDError(message = message, priority = 3) {
    init {
        if (scanner is KerML) {
            textualRepresentation = scanner.semantics.textualRepresentation
            token = scanner.token
        }
    }
    companion object {
        val explanation = """
            This error has been caused during scanning the single tokens of the textual representation. 
            Check the tokens around the current token given above. 
            If there is an error before this error, first fix the error before this one.
        """.trimIndent()
    }
}

/**
 * This Exception is thrown for all errors during initialized and propagate phases.
 * It just creates an error message.
 */
open class SemanticError(message: String, element: Element? = null, cause: Throwable? = null) :
    SysMDError(
        message,
        cause = cause,
        element = element,
        priority = 2
){
    init {
        if (textualRepresentation != null)
            this.textualRepresentation = element?.textualRepresentation?.firstOrNull()
        if (cause is SysMDException && cause.element != null) {
            this.element = cause.element
            this.textualRepresentation = cause.textualRepresentation
        }
    }
    companion object {
        val explanation = """
            This error has been caused during parsing the semantic analysis of the model. 
        """.trimIndent()
    }
}


/**
 * Error in the setup of an expression.
 */
class ExpressionError internal constructor(msg: String, element: Element? = null) :
    SysMDError("Error in expression: $msg", element = element)

/**
 * Internal problem caused exception; e.g. due to inconsistent internal data structures.
 */
class InternalError(message: String, cause: Throwable? = null) : SysMDError(message, cause = cause) {
    init {
        priority = 2
    }
}


/**
 * Exception for static errors.
 *
 * Use more precise exception class derived from this class if possible
 */
open class StaticException(textualRepresentation: TextualRepresentation?, message: String, cause: Throwable? = null) :
    SysMDError(
        message = message,
        textualRepresentation = textualRepresentation,
        cause = cause
    ) {
    init {
        priority = 2
    }
}

/**
 * Exception for initialization problems.
 *
 * For import errors use [ImportException]
 */
class InitialisationException(
    textualRepresentation: TextualRepresentation?,
    message: String,
    cause: Throwable? = null
) :
    StaticException(textualRepresentation, message, cause) {
    init {
        priority = 3
    }
}

/**
 * Exception for import errors.
 */
class ImportException(
    textualRepresentation: TextualRepresentation?,
    message: String,
    cause: Throwable?
) :
    StaticException(textualRepresentation, message, cause) {
    init {
        priority = 3
    }
}

/**
 * Exception for problems occurring during constraint evaluation, propagation
 *
 * This exception is used highly generically.
 * Consider creating a request for more specific error class.
 */
open class SolverException(
    textualRepresentation: TextualRepresentation?,
    message: String,
    element: Element?,
    cause: Throwable?,
) : SysMDError(textualRepresentation = textualRepresentation, message = message, element = element, cause = cause, priority = 2)

/**
 * Issues like ranges or missing specifications of elements are caught with this exception.
 *
 * Beware that this class and its subclasses are **not** to be used for actual errors!
 */
open class ExportIssue(
    textualRepresentation: TextualRepresentation?,
    message: String,
    cause: Throwable?,
    element: Element?
) : SysMDException(
    message = message, textualRepresentation = textualRepresentation, cause = cause, element = element, priority = 2
) {
    companion object {
        val explanation = """
            This error has been caused during parsing the textual representation. 
            Check the syntax around the current token given above. 
            If there is an error before this error, first fix the error before this one.
        """.trimIndent()
    }
}

/**
 * Exception for elements which are yet undefined but not affecting the model
 * and should be defined when exporting
 */
class ClassificationIssue(
    textualRepresentation: TextualRepresentation?,
    message: String,
    cause: Throwable?,
    element: Element?
) : ExportIssue(
    textualRepresentation = textualRepresentation,
    message = message,
    cause = cause,
    element = element
)

/**
 * Exception for elements parameterized with a range of values which should have
 * a single value for export
 */
class ParametrisationIssue(
    textualRepresentation: TextualRepresentation?,
    message: String,
    cause: Throwable?,
    element: Element?
) : ExportIssue(
    textualRepresentation = textualRepresentation,
    message = message,
    cause = cause,
    element = element
)

//TODO specify reason for this exception
class SpecificationIssue(
    textualRepresentation: TextualRepresentation?,
    message: String,
    cause: Throwable?,
    element: Element?
) : ExportIssue(
    textualRepresentation = textualRepresentation,
    message = message,
    cause = cause,
    element = element
)

/**
 * Exception for inheritance errors
 * Examples:
 * - Cyclic dependencies
 */
open class InheritanceException(
    message: String,
    textualRepresentation: TextualRepresentation? = null,
    element: Element? = null,
    cause: Throwable? = null
) : SolverException(
    message = message,
    textualRepresentation = textualRepresentation,
    element = element,
    cause = cause
)