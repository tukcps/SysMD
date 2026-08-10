package com.github.tukcps.sysmd.rest

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import com.github.tukcps.sysmd.rest.entities.api.entities.Commit
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.ProjectResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

class ProjectImplementation(
    @setparam:JsonSerialize(using = ZonedDateTimeSerializer::class)
    override var created: Instant = Clock.System.now(),
    override var alias: Collection<String> = mutableListOf(),
    override var description: String = "",
    override var id: Uuid = Uuid.random(),
    override var name: String? = "",
    var defaultBranchId: Uuid
) : Project {
    var branches = mutableListOf<BranchImplementation>()
    override fun branchesIdList(): List<Uuid> {
        return branches.map { it.id }
    }

    var commits = mutableListOf<Commit>()
    override fun commitsIdList(): List<Uuid> {
        return commits.map { it.id }
    }

    var defaultBranch: BranchImplementation? = null
    override fun defaultBranchId(): Uuid = defaultBranchId

    override fun headsIdList(): List<Uuid> {
        return branches.groupBy { b -> b.referencedCommitId() }.keys.filterNotNull()
    }

    constructor(project: ProjectResponse): this(
        id = project.id,
        name = project.name!!,
        description = project.description?:"",
        defaultBranchId = project.defaultBranch!!.id !!
    )
}