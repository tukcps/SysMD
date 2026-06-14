package com.github.tukcps.sysmd.rest.entities.response

import java.util.*

data class IndexEntry(
    var filename: String,
    var content: String
)

data class ProjectMetaResponse (
    var id: UUID,
    var name: String,
    var description: String? = null,
    var website: String? = null,
    var index: List<IndexEntry> = mutableListOf()
)
