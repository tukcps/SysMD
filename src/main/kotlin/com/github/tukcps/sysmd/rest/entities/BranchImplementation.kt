package com.github.tukcps.sysmd.rest.entities

import com.github.tukcps.sysmlv2.entities.Branch
import com.github.tukcps.sysmlv2.entities.Commit
import com.github.tukcps.sysmlv2.entities.Project
import com.github.tukcps.sysmlv2.entities.responseModels.BranchResponse
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

class BranchImplementation(
    override var created: ZonedDateTime? = null,
    override var modified: LocalDateTime? = null,
    override var alias: List<String> = arrayListOf(),
    override var description: String = "",
    override var id: UUID = UUID.randomUUID(),
    override var name: String = ""
) : Branch {

    var owningProject: Project? = null
    var owningProjectId: UUID? = null
    override fun owningProjectId(): UUID? = owningProject?.id

    var referencedCommitId: UUID? = null
    var referencedCommit: Commit? = null
    override fun referencedCommitId(): UUID? = referencedCommitId
    constructor(branch: BranchResponse): this(
        created = branch.created,
        id = branch.id!!,
        name = branch.name!!
    ) {
        referencedCommitId = branch.referencedCommit?.id
        owningProjectId = branch.owningProject?.id
    }
}