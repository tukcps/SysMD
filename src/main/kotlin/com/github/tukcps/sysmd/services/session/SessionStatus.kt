package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.datamodel.ElementData
import java.util.*
import kotlin.uuid.Uuid

/**
 * In this class, we save the status of the current analysis.
 *
 * Classification is:
 * - TRACE (e.g., created or updated elements)
 * - DEBUG
 * - INFO (e.g., deprecated constructs to be replaced)
 * - WARN (e.g., constraints that cannot be satisfied)
 * - ERROR (e.g., syntax errors where parts could not be compiled; semantic errors that prevent further analysis)
 * - FATAL (internal events that require a reset)
 */
class SessionStatus {
    /**
     * The elements built in the last compile run.
     */
    val elementsBuilt = mutableListOf<ElementData>()

    /**
     * Hashmap of error messages, property id is key, string (error message).
     */
    val issues = linkedSetOf<Issue>()

    /** The number of iterations used in the constraint propagation */
    var numberOfPropagateIterations: Int = 0



    /**
     * Map of updated values; the element id is the key, and string (updated result).
     */
    val updatedValues: HashMap<String, String> = hashMapOf()


    /**
     * Resets all internal values: source, lineNo, columnNo, and the maps errors, errorsByLine,
     * and updates.
     */
    fun reset() {
        issues.clear()
        elementsBuilt.clear()
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param message A descriptive information message.
     * @param element The session in which complementary context information is searched.
     */
    fun info(message: String, element: ElementData? = null, cause: Throwable? = null) {
        issues.add(
            Issue(
                kind = Issue.Kind.INFO,
                message = message,
                input = element?.input,
                indices = element?.indices,
                element = element?.elementId,
                cause = cause
            )
        )
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param message A descriptive information message.
     * @param element The session in which complementary context information is searched.
     */
    fun inconsistency(message: String, element: ElementData?=null, kind: Issue.Kind = Issue.Kind.WARN_INCONSISTENCY) {
        issues.add(
            Issue(
                kind = kind,
                message = message,
                input = element?.input,
                indices = element?.indices,
                element = element?.elementId
            )
        )
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param kind A classification of the warning
     * @param message A descriptive information message.
     * @param element The session in which complementary context information is searched.
     */
    fun warn(
        kind: Issue.Kind = Issue.Kind.WARN,
        message: String,
        element: ElementData? = null,
        cause: Throwable? = null) {
        issues.add(
            Issue(
                kind = kind,
                message = message,
                input = element?.input,
                indices = element?.indices,
                element =element?.elementId,
                cause = cause
            )
        )
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param kind A classification of the warning
     * @param message A descriptive information message.
     * @param elementId The element id of the related element.
     */
    fun warn(
        kind: Issue.Kind = Issue.Kind.WARN,
        message: String,
        elementId: Uuid?,
        cause: Throwable? = null) {
        issues.add(
            Issue(
                kind = kind,
                message = message,
                element = elementId,
                cause = cause
            )
        )
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param message A descriptive information message.
     * @param element The session in which complementary context information is searched.
     */
    fun error(
        message: String,
        element: ElementData? = null,
        kind: Issue.Kind=Issue.Kind.ERROR,
        cause: Throwable? = null
    ) {
        val issue = Issue(
            kind = kind,
            message = message,
            input = element?.input,
            indices = element?.indices,
            cause = cause,
        )
        issues.add(issue)
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param message A descriptive information message.
     * @param compiler The compiler in which complementary context information is searched.
     * @param element The element in which complementary context information is searched.
     * @param cause A stacktrace in cause of an exception, for debugging.
     */
    fun fatal(message: String, compiler: KerML? = null, element: Element? = null, cause: Throwable? = null) {
        issues.add(
            Issue(
                kind = Issue.Kind.FATAL,
                message = message+if (cause?.message != null) " - ${cause.message}" else "",
                input = compiler?.input?:element?.input?:(cause as? SysMDException)?.input?:(cause as? SysMDException)?.element?.input,
                indices = element?.indices,
                element = element?.elementId,
                cause = cause?:SysMDException(message)
            )
        )
    }

    override fun toString(): String = "Status: ${issues.size} issues"

}