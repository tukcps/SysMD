package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

import com.github.tukcps.sysmd.rest.entities.api.entities.requestModels.DataVersionRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class DataDifferenceResponse(
    val baseData: DataVersionRequest?,
    val compareData: DataVersionRequest?
) {
    @SerialName("@type")
    val type: String = "DataDifference"
}