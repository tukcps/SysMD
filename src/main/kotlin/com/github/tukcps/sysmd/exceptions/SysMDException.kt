package com.github.tukcps.sysmd.exceptions

import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation


/**
 * General exception class that is also used for persisting in the list of errors/issues.
 * Each entry has:
 * @param message Mandatory textual description
 * @param textualRepresentation the textual representation in which the error has occurred
 * @param token the token where the error has occurred
 * @param element the element in which the error has occurred
 * @param cause the initial exception that caused the SysMD exception
 */
open class SysMDException(
    override var message: String,
    var textualRepresentation: TextualRepresentation? = null,
    var token: Token? = null,
    var element: Element? = null,
    override val cause: Throwable? = null,
    var priority: Int,
): Exception(message, cause) {

    /**
     * A method for comparison that is straightforward to prevent duplicates.
     * Does not consider cause and token that can be different from different runs for the same error.
     */
    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            !is SysMDException -> false
            else -> message == other.message && element?.qualifiedName == other.element?.qualifiedName && token?.lineNo == other.token?.lineNo
        }
    }

    /**
     * The hash code as needed for equals and the *set* of errors in the status.
     */
    override fun hashCode(): Int {
        var result = message.hashCode()
        // result = 31 * result + (textualRepresentation?.hashCode() ?: 0)
        result = 31 * result + (token?.lineNo?.hashCode() ?: 0)
        result = 31 * result + (element?.qualifiedName?.hashCode() ?: 0)
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
            string += "in '${element!!.escapedName()?:element?.elementType}': "
        }
        string += message
        return string
    }

    open val explanation get() = EXPLANATION

    companion object {
        const val EXPLANATION = "An exception inside SysMD that is not specifically classified."
    }

}