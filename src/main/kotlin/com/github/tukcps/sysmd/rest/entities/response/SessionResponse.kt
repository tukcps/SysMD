package com.github.tukcps.sysmd.rest.entities.response

import java.util.*

/**
 * More comprehensive information than just the project endpoint provides
 */
data class SessionResponse (
    var id: UUID? = null,
    var projectName: String? = null,
    var projectDescription: String? = null,
    var projectIndex: Collection<String> = mutableListOf(),
    var projectMaintainer: Collection<String> = mutableListOf(),
)