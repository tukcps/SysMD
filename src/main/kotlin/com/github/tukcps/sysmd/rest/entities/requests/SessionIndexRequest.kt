package com.github.tukcps.sysmd.rest.entities.requests

data class IndexEntry(
    var filename: String,
    var content: String
)

data class SessionIndexRequest (
    var files: Collection<IndexEntry> = mutableListOf()
)
