package com.github.tukcps.sysmd.rest.entities.api.entities

data class DataDifference(
    var baseData: DataVersion? = null,
    var compareData : DataVersion? = null,
)
