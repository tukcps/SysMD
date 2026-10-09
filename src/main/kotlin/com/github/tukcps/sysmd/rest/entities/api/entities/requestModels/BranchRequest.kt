package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import com.github.tukcps.sysmd.model.datamodel.ElementReference
import kotlinx.serialization.SerialName

data class BranchRequest(
    @SerialName("@type")
    val type: String = "Branch",
    @SerialName("@id")
    var head: ElementReference? = null,
    var name: String? = null
)