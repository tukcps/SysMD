package com.github.tukcps.sysmd.rest.entities.response

import com.github.tukcps.sysmd.services.session.SessionStatus
import java.security.MessageDigest
import java.util.*

/**
 * Response model for the status of a session.
 * Contains a list of issues, updates, and other information.
 */
data class SessionStatusResponse (

    /** The number of iterations used in the constraint propagation */
    var numberOfPropagateIterations: Int = 0,

    /** Hashmap of error messages, property id is key, string (error message). */
    val issues: MutableCollection<IssueResponse> = mutableListOf(),

    /** Map of updated properties, property id is key, and string (updated result). */
    val updates: MutableMap<String, String> = hashMapOf()
) {
    constructor(sessionStatus: SessionStatus): this(
        sessionStatus.numberOfPropagateIterations,
    ) {
        sessionStatus.issues.forEach { issue ->
            issues.add(IssueResponse(issue))
        }
        sessionStatus.updatedValues.forEach {
            updates[it.key] = it.value
        }
    }
}

fun hashBase64UrlSafe(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
}