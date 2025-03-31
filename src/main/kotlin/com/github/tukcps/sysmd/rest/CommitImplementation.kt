package com.github.tukcps.sysmd.rest

import io.github.tukcps.sysmlv2.api.entities.Commit
import io.github.tukcps.sysmlv2.api.entities.CommitDataObject
import io.github.tukcps.sysmlv2.api.entities.responseModels.CommitResponse
import java.time.OffsetDateTime
import java.util.*

/**
 * A 'Commit' is a class that holds
 * - a set of elements,
 * - properties, and
 * - relationships.
 * To retrieve and search the elements properly, the global node is given.
 * The 'Any' node is there as well, but shared with all projects.
 * Project is a subclass with specific extensions.
 */
@Suppress("UNUSED_PARAMETER")
class CommitImplementation(): Commit {
    override var id: UUID = UUID.randomUUID()
    var type:String="Commit"
    override var owningProject: UUID? = null
    override fun payloadIdList(): List<UUID> {
        TODO("Not yet implemented")
    }

    private var previousIdList: List<UUID> = mutableListOf()
    override fun previousCommitIdList(): List<UUID> {
        TODO("Not yet implemented")
    }

    override var description: String = ""

    var payload: List<CommitDataObject> = arrayListOf()

    override var alias: Collection<String>
        get() = TODO("Not yet implemented")
        set(value) {}

    override var created: OffsetDateTime = OffsetDateTime.now()
    override var modified: OffsetDateTime? = null
    override var name: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    constructor(response: CommitResponse): this() {
        id = response.id!!
        owningProject = response.owningProject?.id
        description = response.description?:""
        previousIdList = response.previousCommit?.map { it.id!! }!!
    }
    override fun toString() =
        "CommitImplementation { project = $owningProject, description = $description, #elements = ${payload.size} }f"
}