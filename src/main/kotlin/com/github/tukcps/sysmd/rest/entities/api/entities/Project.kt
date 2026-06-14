package com.github.tukcps.sysmd.rest.entities.api.entities

import com.github.tukcps.sysmd.rest.entities.interchange.ProjectBase
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid


/**
 *  Basic interface of a Project.
 *  The SysML v2 fields that are no basic types or where there are potential
 *  differences in request, response, and persistence models are modeled as
 *  functions to avoid problems with serialization.
 */
interface Project: Record, ProjectBase {
    override var id: Uuid
    override var name: String?
    var created: Instant
    fun defaultBranchId(): Uuid? = null
    fun branchesIdList(): List<Uuid> = emptyList()
    fun commitsIdList(): List<Uuid> = emptyList()
    fun headsIdList(): List<Uuid> = emptyList()
}