package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.scanner.Token


/**
 * A report of an issues in a session or pars run.
 * @param message string that describes the issue
 * @param input input of the parser if known
 * @param token input-token of the parser, if known, includes line a column in the input
 * @param elementPath path to the element that is affected by the issue
 * @param cause exception with stack trace if known
 */
class Issue(
    var kind: Kind,
    var message: String,
    var input: CharSequence? = null,
    var token: Token? = null,
    var elementPath: String? = null,
    var cause: Throwable? = null,
) {
    enum class Kind {
        TRACE { override fun toString(): String = "Trace" },
        DEBUG { override fun toString(): String = "Debug information" },
        INFO  { override fun toString(): String = "Information" },
        WARN  { override fun toString(): String = "Warning" },
        WARN_ITERATIONS_EXCEEDED { override fun toString(): String = "Number of iterations exceeded upper bound" },
        WARN_UNRESOLVED_OWNER { override fun toString(): String = "Element for which the owner could not be resolved" },
        WARN_UNRESOLVED_TYPE { override fun toString(): String = "The type of an element could not be resolved" },
        WARN_INCONSISTENCY { override fun toString(): String = "Inconsistency" },
        ERROR { override fun toString(): String = "Error" },
        ERROR_UNRESOLVED_NAME { override fun toString(): String = "Unresolved name" },
        ERROR_SEMANTIC { override fun toString(): String = "Semantic error" },
        ERROR_TYPE_WRONG { override fun toString(): String = "Element with different type expected" },
        ERROR_CYCLIC_DEPENDENCY { override fun toString(): String = "Cyclic dependency" },
        ERROR_SYNTACTICAL { override fun toString(): String = "Syntax error" },
        ERROR_LEXICAL { override fun toString(): String = "Lexical error" },
        FATAL { override fun toString(): String = "Fatal error" };
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
}