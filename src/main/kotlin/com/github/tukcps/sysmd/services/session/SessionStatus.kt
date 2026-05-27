package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.model.kerml.Element

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

    /** The number of iterations used in the constraint propagation */
    var numberOfPropagateIterations: Int = 0

    /**
     * Hashmap of error messages, property id is key, string (error message).
     */
    val issues = linkedSetOf<Issue>()

    /**
     * Map of updated values; the element id is the key, and string (updated result).
     */
    val updatedValues: HashMap<String, String> = hashMapOf()

    /**
     * List of all created element's path
     */
    val createdElements = mutableSetOf<String>()

    /**
     * Resets all internal values: source, lineNo, columnNo, and the maps errors, errorsByLine,
     * and updates.
     */
    fun reset() {
        issues.clear()
        updatedValues.clear()
        createdElements.clear()
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param message A descriptive information message.
     * @param compiler The compiler in which complementary context information is searched.
     * @param element The session in which complementary context information is searched.
     */
    fun info(message: String, compiler: KerML? = null, element: Element? = null, cause: Throwable? = null) {
        issues.add(
            Issue(
                kind = Issue.Kind.INFO,
                message = message,
                input = compiler?.input?:element?.input,
                token = compiler?.token,
                path = compiler?.semantics?.ownerName()?:element?.path(),
                cause = cause
            )
        )
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param message A descriptive information message.
     * @param compiler The compiler in which complementary context information is searched.
     * @param element The session in which complementary context information is searched.
     */
    fun inconsistency(message: String, compiler: KerML? = null, element: Element? = null, path: String?=null, kind: Issue.Kind = Issue.Kind.WARN_INCONSISTENCY) {
        issues.add(
            Issue(
                kind = kind,
                message = message,
                input = compiler?.input?:element?.input,
                token = compiler?.token,
                path = path?:compiler?.semantics?.ownerName()?:element?.path(),
            )
        )
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param kind A classification of the warning
     * @param message A descriptive information message.
     * @param compiler The compiler in which complementary context information is searched.
     * @param element The session in which complementary context information is searched.
     */
    fun warn(
        kind: Issue.Kind = Issue.Kind.WARN,
        message: String, compiler: KerML? = null,
        path: String? = null,
        element: Element? = null,
        cause: Throwable? = null) {
        issues.add(
            Issue(
                kind = kind,
                message = message,
                input = compiler?.input?:element?.input?:element?.owner?.input,
                indices = element?.indices?:element?.owner?.indices,
                token = compiler?.token,
                path = path?:element?.path(),
                cause = cause
            )
        )
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param message A descriptive information message.
     * @param compiler The compiler in which complementary context information is searched.
     * @param element The session in which complementary context information is searched.
     */
    fun error(message: String, compiler: KerML? = null, element: Element? = null, path: String? = null, kind: Issue.Kind=Issue.Kind.ERROR, cause: Throwable? = null) {

        val issue = Issue(
            kind = kind,
            message = message,
            input = compiler?.input?:element?.input?:(cause as? SysMDException)?.input?:(cause as? SysMDException)?.element?.input,
            indices = compiler?.token?.indices?:element?.indices,
            token = compiler?.token,
            path = path?:compiler?.semantics?.ownerName()?:element?.path(),
            cause = cause
        )
        issues.add(issue)
    }

    /**
     * The information reporting function adds message reports to the status.
     * @param message A descriptive information message.
     * @param compiler The compiler in which complementary context information is searched.
     * @param element The session in which complementary context information is searched.
     */
    fun fatal(message: String, compiler: KerML? = null, element: Element? = null, cause: Throwable? = null) {
        issues.add(
            Issue(
                kind = Issue.Kind.FATAL,
                message = message+if (cause?.message != null) " - ${cause.message}" else "",
                input = compiler?.input?:element?.input?:(cause as? SysMDException)?.input?:(cause as? SysMDException)?.element?.input,
                indices = element?.indices,
                token = compiler?.token,
                path = compiler?.semantics?.ownerName()?:element?.path(),
                cause = cause?:SysMDException(message)
            )
        )
    }

    override fun toString(): String = "Status: ${issues.size} issues reported, ${createdElements.size} elements created, ${updatedValues.size} values updated."

}