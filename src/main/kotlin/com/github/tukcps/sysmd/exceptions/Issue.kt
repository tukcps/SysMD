package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.scanner.Token


/**
 * A report of an issues in a session or pars run.
 * @param message string that describes the issue
 * @param input input of the parser if known
 * @param indices indices that mark a specific line or region in the input
 * @param token input-token of the parser, if known, includes line a column in the input
 * @param elementPath path to the element that is affected by the issue
 * @param cause exception with stack trace if known
 */
class Issue(
    var kind: Kind,
    var message: String,
    var input: CharSequence? = null,
    var indices: IntRange? = null,
    var token: Token? = null,
    var elementPath: String? = null,
    var cause: Throwable? = null,
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

    /**
     * Equals and hashcode are needed to ensure that an existing error is not added twice;
     * hence, they are saved in a HashSet that requires equals and hashCode.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Issue) return false
        if (other.message != message) return false
        if (other.kind != kind) return false
        if (other.input != input) return false
        if (other.token?.kind != token?.kind) return false
        if (other.elementPath != elementPath) return false
        return true
    }

    override fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + (input?.hashCode() ?: 0)
        result = 31 * result + (token?.kind?.hashCode() ?: 0)
        result = 31 * result + (elementPath?.hashCode() ?: 0)
        return result
    }

    fun line(): Int? {
        if (input != null&&indices!=null) {
            require(indices!!.first in input!!.indices && indices!!.last in input!!.indices) {
                "Indices must be within input range"
            }
            return input!!.subSequence(0, indices!!.first).count { it == '\n' } + 1
        } else
            return token?.lineNo
    }

    override fun toString() = message
}