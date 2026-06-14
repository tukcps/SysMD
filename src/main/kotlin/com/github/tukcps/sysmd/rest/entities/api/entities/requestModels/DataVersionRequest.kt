package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.commitData.CommitData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class DataVersionRequest(
    override var id: Uuid? = null,
    @SerialName("@type")
    var type: String = "DataVersion",
    var identity: DataIdentityRequest? = null,
    val payload: CommitData? = null,
): Identified {
    override fun clone() = copy()
}