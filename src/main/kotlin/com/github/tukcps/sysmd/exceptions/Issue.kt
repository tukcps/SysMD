package com.github.tukcps.sysmd.exceptions

import java.util.Objects.hash
import kotlin.uuid.Uuid


/**
 * A report of an issues in a session or pars run.
 * @param message string that describes the issue
 * @param input input of the parser if known
 * @param indices indices that mark a specific line or region in the input
 * @param element elementId of element related to the issue
 * @param cause exception with stack trace if known
 */
data class Issue(
    val kind: Kind,
    val message: String,
    val input: CharSequence? = null,
    val indices: IntRange? = null,
    val element: Uuid? = null,
    val cause: Throwable? = null,
) {
    enum class Kind {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        WARN_ITERATIONS_EXCEEDED,
        WARN_UNRESOLVED_OWNER,
        WARN_UNRESOLVED_TYPE,
        WARN_INCONSISTENCY,
        ERROR,
        ERROR_UNRESOLVED_NAME,
        ERROR_SEMANTIC,
        ERROR_TYPE_WRONG,
        ERROR_CYCLIC_DEPENDENCY,
        ERROR_SYNTACTICAL,
        ERROR_LEXICAL,
        FATAL;
    }

    fun line(): Int? =
        if (input !== null && indices !== null) {
            return input.subSequence(0, indices.first).count { it == '\n' } + 1
        }  else null

    override fun equals(other: Any?): Boolean
        = other is Issue && kind == other.kind && message == other.message && input == other.input &&
            indices == other.indices && element == other.element

    override fun hashCode(): Int = hash(kind, message, input, indices, element)

    override fun toString() = message
}