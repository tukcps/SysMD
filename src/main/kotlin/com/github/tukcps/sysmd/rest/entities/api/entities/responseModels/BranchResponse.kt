package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.rest.entities.api.entities.Identity
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * @author: Amartya Parijat
 * Response Model for Branches
 */
@Serializable
data class BranchResponse(
    @SerialName("@id")
    override var id: Uuid = Uuid.random(),
    @SerialName("@type")
    val type: String = "Branch",
    var created: Instant? = null,
    var referencedCommit: Identified? = null,
    var owningProject: Identified? = null,
    var head: Identified? = null,
    var name: String? = null,
): Identity