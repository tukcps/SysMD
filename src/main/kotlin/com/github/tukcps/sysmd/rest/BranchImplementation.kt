package com.github.tukcps.sysmd.rest

import com.github.tukcps.sysmd.rest.entities.api.entities.Branch
import com.github.tukcps.sysmd.rest.entities.api.entities.Commit
import com.github.tukcps.sysmd.rest.entities.api.entities.Project
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.BranchResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

class BranchImplementation(
    override var created: Instant? = Clock.System.now(),
    override var modified: Instant? = null,
    override var alias: Collection<String> = arrayListOf(),
    override var description: String = "",
    override var id: Uuid = Uuid.random(),
    override var name: String? = ""
) : Branch {

    var owningProject: Project? = null
    var owningProjectId: Uuid? = null
    override fun owningProjectId(): Uuid? = owningProject?.id

    var referencedCommitId: Uuid? = null
    var referencedCommit: Commit? = null
    override fun referencedCommitId(): Uuid? = referencedCommitId
    constructor(branch: BranchResponse): this(
        created = branch.created,
        id = branch.id,
        name = branch.name!!
    ) {
        referencedCommitId = branch.referencedCommit?.id
        owningProjectId = branch.owningProject?.id
    }
}