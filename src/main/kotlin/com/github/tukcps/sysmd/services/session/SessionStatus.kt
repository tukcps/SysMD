package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.exceptions.SysMDInfo
import java.util.*

/**
 * In this class we save the status of the current analysis
 */
class SessionStatus {

    /** The number of iterations used in the constraint propagation */
    var numberOfPropagateIterations: Int = 0

    // Hashmap of error messages, property id is key, string (error message).
    val exceptions = mutableSetOf<SysMDException>()

    @Deprecated("Replace with exceptions.filterIsInstance<SysMDError")
    val errors: List<SysMDError>
        get() = exceptions.filterIsInstance<SysMDError>()

    // Hashmap of updated properties, property id is key, and string (updated result).
    val updates: HashMap<UUID, String> = hashMapOf()

    /**
     * Resets all internal values: source, lineNo, columnNo, and the mapx errors, errorsByLine,
     * and updates.
     */
    fun reset() {
        exceptions.clear()
        updates.clear()
    }

    override fun toString(): String =
        "Status: ${exceptions.filterIsInstance<SysMDError>().size} errors, ${exceptions.filterIsInstance<SysMDInfo>().size} issues, ${updates.size} values updated."

}