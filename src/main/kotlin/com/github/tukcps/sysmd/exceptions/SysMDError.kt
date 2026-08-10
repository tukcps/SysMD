package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Scanner
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.model.kerml.Element
import java.util.*
import kotlin.uuid.Uuid


/**
 * General error class that is also used for persisting the error list.
 * Each entry has:
 * @param message Mandatory textual description
 * @param token the token where the error has occurred
 * @param element the element in which the error has occurred
 * @param
 */
open class SysMDError(
    message: String,
    input: CharSequence? = null,
    token: Token? = null,
    kind: Issue.Kind = Issue.Kind.ERROR,
    element: Element? = null,
    elementId: Uuid? = null,
    path: String? = null,
    cause: Throwable? = null,
) : SysMDException(message, input, token, kind, element, elementId, path, cause)


/**
 * This Exception is thrown for all errors that are caused in the parsing methods.
 * It just writes an error message. Line and column are optional.
 */
class LexicalError(
    scanner: Scanner,
    message: String,
) : SysMDError(message = message, input = scanner.input, token = scanner.token) {
    init {
        if (scanner is KerML) { token = scanner.token }
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
){
    init {
        if (cause is SysMDException && cause.element != null) {
            this.element = cause.element
            this.input = cause.input
        }
    }
    companion object {
        val explanation = """
            This error has been caused during parsing the semantic analysis of the model. 
        """.trimIndent()
    }
}

/**
 * This Exception is thrown for all errors during initialized and propagate phases.
 * It just creates an error message.
 */
open class SolverError(
    message: String,
    path: String? = null,
    elementId: Uuid?=null,
    cause: Throwable? = null
) : SysMDError(
        message,
        cause = cause,
        path = path,
        element = null,
        elementId = elementId,
    ){
    init {
        if (cause is SysMDException && cause.element != null) {
            this.element = cause.element
            this.input = cause.input
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
class InternalError(
    message: String,
    element: Element? = null,
    cause: Throwable? = null
) : SysMDError(message, element = element, cause = cause)


/**
 * Exception for static errors.
 *
 * Use more precise exception class derived from this class if possible
 */
open class StaticException(message: String, cause: Throwable? = null) :
    SysMDError(
        message = message,
        cause = cause
    )


/**
 * Exception for initialization problems.
 *
 * For import errors use [ImportException]
 */
class InitialisationException(
    message: String,
    cause: Throwable? = null
) : StaticException(message, cause)

/**
 * Exception for import errors.
 */
class ImportException(
    message: String,
    cause: Throwable?
) :
    StaticException(message, cause)


/**
 * Exception for problems occurring during constraint evaluation, propagation
 *
 * This exception is used highly generically.
 * Consider creating a request for more specific error class.
 */
open class SolverException(
    message: String,
    element: Element?,
    cause: Throwable?,
) : SysMDError(message = message, element = element, cause = cause)


/**
 * Issues like ranges or missing specifications of elements are caught with this exception.
 *
 * Beware that this class and its subclasses are **not** to be used for actual errors!
 */
open class ExportIssue(
    message: String,
    cause: Throwable?,
    element: Element?
) : SysMDException(
    message = message, cause = cause, element = element
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
    message: String,
    cause: Throwable?,
    element: Element?
) : ExportIssue(
    message = message,
    cause = cause,
    element = element
)

/**
 * Exception for elements parameterized with a range of values which should have
 * a single value for export
 */
class ParametrisationIssue(
    message: String,
    cause: Throwable?,
    element: Element?
) : ExportIssue(
    message = message,
    cause = cause,
    element = element
)

//TODO specify reason for this exception
class SpecificationIssue(
    message: String,
    cause: Throwable?,
    element: Element?
) : ExportIssue(
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
    element: Element? = null,
    cause: Throwable? = null
) : SolverException(
    message = message,
    element = element,
    cause = cause
)