package com.github.tukcps.sysmd.rest.entities.requests

import kotlin.uuid.Uuid

data class IndexEntry(
    var filename: String,
    var content: String
)

/**
 * A selection of the data from .project, .meta that is used.
 */
data class ProjectMetaRequest (
    var id: Uuid,
    var name: String,
    var description: String? = null,
    var website: String? = null,
    var index: List<IndexEntry> = mutableListOf()
)
