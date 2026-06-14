package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlinx.serialization.SerialName

data class BranchRequest(
    @SerialName("@type")
    val type: String = "Branch",
    @SerialName("@id")
    var head: Identified? = null,
    var name: String? = null
)