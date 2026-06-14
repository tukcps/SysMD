package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class DataIdentityRequest(
    @SerialName("@id")
    override var id: Uuid? = null,
    @SerialName("@type")
    val type: String = "DataIdentity"
): Identified {
    override fun clone() = copy()
}