package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import kotlinx.serialization.SerialName

/**
 * Request model for commits:
 * - name
 * - description
 * - id of the previous commit or null, if no previous commit.
 */
data class CommitRequest(
    @SerialName("@type")
    val type: String = "Commit",
    var description: String? = null,
    var change: List<DataVersionRequest> = ArrayList()
)