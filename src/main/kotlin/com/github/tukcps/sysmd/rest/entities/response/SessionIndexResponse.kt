package com.github.tukcps.sysmd.rest.entities.response

data class IndexEntry(
    var filename: String,
    var content: String
)

data class SessionIndexResponse (
    var files: Collection<IndexEntry> = mutableListOf()
)
