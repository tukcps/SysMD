package com.github.tukcps.sysmd.rest.entities

import io.github.tukcps.sysmlv2.api.entities.Branch
import io.github.tukcps.sysmlv2.api.entities.Commit
import io.github.tukcps.sysmlv2.api.entities.Project
import io.github.tukcps.sysmlv2.api.entities.responseModels.BranchResponse
import java.time.OffsetDateTime
import java.util.*

class BranchImplementation(
    override var created: OffsetDateTime? = OffsetDateTime.now(),
    override var modified: OffsetDateTime? = null,
    override var alias: Collection<String> = arrayListOf(),
    override var description: String = "",
    override var id: UUID = UUID.randomUUID(),
    override var name: String? = ""
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