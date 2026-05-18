package com.github.tukcps.sysmd.rest.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import io.github.tukcps.sysmlv2.api.entities.Commit
import io.github.tukcps.sysmlv2.api.entities.Project
import io.github.tukcps.sysmlv2.api.entities.responseModels.ProjectResponse
import java.time.OffsetDateTime
import java.util.*

class ProjectImplementation(
    @setparam:JsonSerialize(using = ZonedDateTimeSerializer::class)
    override var created: OffsetDateTime = OffsetDateTime.now(),
    override var alias: Collection<String> = mutableListOf(),
    override var description: String = "",
    override var id: UUID = UUID.randomUUID(),
    override var name: String? = "",
    var defaultBranchId: UUID
) : Project {
    var branches = mutableListOf<BranchImplementation>()
    override fun branchesIdList(): Collection<UUID> {
        return branches.map { it.id }
    }

    var commits = mutableListOf<Commit>()
    override fun commitsIdList(): Collection<UUID> {
        return commits.map { it.id }
    }

    var defaultBranch: BranchImplementation? = null
    override fun defaultBranchId(): UUID = defaultBranchId

    override fun headsIdList(): List<UUID> {
        return branches.groupBy { b -> b.referencedCommitId() }.keys.filterNotNull()
    }

    constructor(project: ProjectResponse): this(
        id = project.id !!,
        name = project.name !!,
        description = project.description?:"",
        defaultBranchId = project.defaultBranch!!.id !!
    )
}