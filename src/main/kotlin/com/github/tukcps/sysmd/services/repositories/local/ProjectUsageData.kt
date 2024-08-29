package com.github.tukcps.sysmd.services.repositories.local


import java.io.File
import java.net.URI
import java.util.*

class ProjectUsageData(
    var resource: URI,
    var versionConstraint: String = "0",
    override var owningProject: UUID = UUID.randomUUID(),
    override var usedCommit: UUID = UUID.randomUUID()
) : com.github.tukcps.sysmlv2.entities.ProjectUsage {
    /** Checks if we have cached a local file for it. */
    fun toLocalFile(): File? {
        TODO()
    }

    override fun toString(): String =
        resource.toString() + "::$versionConstraint"

}