package com.github.tukcps.sysmd.services.repositories.local


import com.github.tukcps.sysmd.rest.entities.api.entities.ProjectUsage
import io.ktor.http.*
import java.io.File
import kotlin.uuid.Uuid

class ProjectUsageData(
    var resource: Url,
    var versionConstraint: String = "0",
    override var owningProject: Uuid = Uuid.random(),
    override var usedCommit: Uuid = Uuid.random()
) : ProjectUsage {

    /** Checks if we have cached a local file for it. */
    fun toLocalFile(): File? {
        TODO()
    }

    override fun toString(): String =
        resource.toString() + "::$versionConstraint"

}