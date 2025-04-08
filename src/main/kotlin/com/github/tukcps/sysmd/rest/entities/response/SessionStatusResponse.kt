package com.github.tukcps.sysmd.rest.entities.response

import com.github.tukcps.sysmd.services.session.SessionStatus
import java.util.*

data class SysMDReportModel(
    val message: String? = null,
    val line: Int?=null,
    val token: String?=null,
    val elementId: UUID?=null,
)

data class SessionStatusResponse (

    /** The number of iterations used in the constraint propagation */
    var numberOfPropagateIterations: Int = 0,

    /** Hashmap of error messages, property id is key, string (error message). */
    val reports: MutableCollection<SysMDReportModel> = mutableListOf(),

    /** Map of updated properties, property id is key, and string (updated result). */
    val updates: MutableMap<UUID, String> = hashMapOf()
) {
    constructor(sessionStatus: SessionStatus): this(
        sessionStatus.numberOfPropagateIterations,
    ) {
        sessionStatus.issues.forEach {
            // reports.add(SysMDReportModel(message = it.message, line = it.token?.lineNo, token = it.token?.string, elementId = it.elementPath?.elementId))
        }
        sessionStatus.updatedValues.forEach {
            updates[it.key] = it.value
        }
    }
}