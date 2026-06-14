package com.github.tukcps.sysmd.rest.entities.api.entities.requestModels

import kotlin.uuid.Uuid

class ImportRequest {
    val projectId: Uuid? = null
    val commitId: Uuid? = null
}