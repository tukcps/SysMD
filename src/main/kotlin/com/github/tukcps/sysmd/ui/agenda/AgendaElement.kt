package com.github.tukcps.sysmd.ui.agenda

import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.exceptions.SysMDException

/**
 * This class stores additional information to the qualified names of the
 * elements on which an error occurred
 */
class AgendaElement(
    //TODO do that below
    //TODO Consider using the exception directly instead of passing on its values
    /**
     * There shall be also a reference to the notebook cell.
     * This shall allow linking the Agenda element with, e.g., highlighting of
     * a notebook cell.
     */

    /**
     * Qualified name of the element
     */
    val qualifiedName: String,

    /** Thrown exception */
    val exceptionClass: SysMDException,

    /**
     * Comprehensive error message
     */
    val errorMessage: String,

    val textualRepresentation: TextualRepresentation? = null,
    /**
     * Line of an element in code
     */
    line: Int = -1,

    /**
     * Link to wiki
     */
    val link: String = ""
) {
    /**
     * The line number in the code where the error occurred.
     * Default value is -1 as sign for unknown line number
     */
    internal var line = line
        set(value) {
            if (value < -1) {
                throw IllegalArgumentException(
                    "The line number must be greater or equal to zero for a valid line number." +
                            "Set to -1 if no line number shall be displayed or if it's unknown."
                )
            } else {
                field = value
            }
        }

    init {
        if (line < -1) throw IllegalArgumentException("The line number must be greater or equal to zero")
    }

    override fun toString(): String {
        return "AgendaElement: (QualifiedName: $qualifiedName, ErrorMessage: $errorMessage, Line: $line)"
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            !is AgendaElement -> false
            else -> (qualifiedName == other.qualifiedName && errorMessage == other.errorMessage && line == other.line)
        }
    }

    override fun hashCode(): Int {
        var result = qualifiedName.hashCode()
        result = 31 * result + errorMessage.hashCode()
        result = 31 * result + exceptionClass.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + line
        return result
    }
}