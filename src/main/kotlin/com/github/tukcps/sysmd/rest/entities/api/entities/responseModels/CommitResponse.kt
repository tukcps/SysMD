package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.rest.entities.api.entities.Identity
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlin.uuid.Uuid

/**
 * Used only for the REST response of a Commit class.
 * Change since 2.8:
 * - Payload removed; get the payload via the Get Elements request.
 */
class CommitResponse(
    @SerialName("@id")
    override var id: Uuid = Uuid.random(),                 // id, always there.
    @SerialName("@type")
    var type: String = "Commit",
    var description: String? = null,    // description text of the commit, optional
    var previousCommit: List<Identified>? = ArrayList(), // Previous commit, optional
    var owningProject: Identified? = null,
    var created: Instant? = null
): Identity