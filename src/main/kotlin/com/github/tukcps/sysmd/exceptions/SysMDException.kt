package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.model.kerml.Element


/**
 * General exception class that holds information for debugging SysMD and as well to generate error messages.
 * Each entry has:
 * @param message Mandatory textual description; for error message
 * @param input The input sequence that caused the issue, if known.
 * @param token The input token where the error has occurred, if known.
 * @param element path of the element in which the error has occurred; for debugging
 * @param cause the initial exception that caused the SysMD exception; for debugging
 */
open class SysMDException(
    override var message: String,
    var input: CharSequence? = null,
    var token: Token? = null,
    var kind: Issue.Kind = Issue.Kind.ERROR,
    var element: Element? = null,
    var path: String? = null,
    override val cause: Throwable? = null,
): Exception(message, cause) {

    /**
     * A method for comparison that is straightforward to prevent duplicates.
     * Does not consider cause and token that can be different from different runs for the same error.
     */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            !is SysMDException -> false
            else -> message == other.message && input == other.input && token?.lineNo == other.token?.lineNo
        }
    }

    /**
     * The hash code as needed for equals and the *set* of errors in the status.
     */
    override fun hashCode(): Int {
        var result = message.hashCode()
        // result = 31 * result + (textualRepresentation?.hashCode() ?: 0)
        result = 31 * result + (token?.lineNo?.hashCode() ?: 0)
        result = 31 * result + (input?.hashCode() ?: 0)
        return result
    }

    /**
     * To string
     */
    override fun toString(): String {
        var string = ""
        if (token != null) {
            string += "Line ${token!!.lineNo}, near '${token!!.string}' "
        }
        if (element != null) {
            string += "in '${element}': "
        }
        string += message
        return string
    }

    open val explanation get() = EXPLANATION

    companion object {
        const val EXPLANATION = "An exception inside SysMD that is not specifically classified."
    }
}