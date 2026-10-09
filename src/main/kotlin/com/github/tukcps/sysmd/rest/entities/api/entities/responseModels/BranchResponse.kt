package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

import com.github.tukcps.sysmd.model.datamodel.ElementReference
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
    var referencedCommit: ElementReference? = null,
    var owningProject: ElementReference? = null,
    var head: ElementReference? = null,
    var name: String? = null,
): Identity