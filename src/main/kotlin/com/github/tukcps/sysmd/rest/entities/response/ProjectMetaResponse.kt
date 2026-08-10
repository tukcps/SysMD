package com.github.tukcps.sysmd.rest.entities.response

import kotlin.uuid.Uuid

data class IndexEntry(
    var filename: String,
    var content: String
)

data class ProjectMetaResponse (
    var id: Uuid,
    var name: String,
    var description: String? = null,
    var website: String? = null,
    var index: List<IndexEntry> = mutableListOf()
)
