package com.github.tukcps.sysmd.rest.entities.api.entities


data class DataVersion(
    var baseData : DataVersion? = null,
    var compareData : DataVersion? = null,
)
