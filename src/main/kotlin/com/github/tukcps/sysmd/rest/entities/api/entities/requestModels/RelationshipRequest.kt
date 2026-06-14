package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import kotlinx.serialization.SerialName
import kotlin.uuid.Uuid


/**
 * Entity via which a Relationship element is requested.
 */
class RelationshipRequest(
    val elementId: Uuid,
    val name: String?,
    val shortName: String?,

    @SerialName("@type")
    val type: String = "Relationship",

    val source: List<Uuid>,     // qualified names of source
    val target: List<Uuid>,     // qualified names of target
)
