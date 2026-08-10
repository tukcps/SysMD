package com.github.tukcps.sysmd.rest


import com.github.tukcps.sysmd.rest.entities.api.entities.Commit
import com.github.tukcps.sysmd.rest.entities.api.entities.CommitDataObject
import com.github.tukcps.sysmd.rest.entities.api.entities.responseModels.CommitResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

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
    override var id: Uuid = Uuid.random()
    var type:String="Commit"
    override var owningProject: Uuid? = null
    override fun payloadIdList(): List<Uuid> {
        TODO("Not yet implemented")
    }

    private var previousIdList: List<Uuid> = mutableListOf()
    override fun previousCommitIdList(): List<Uuid> {
        TODO("Not yet implemented")
    }

    override var description: String = ""

    var payload: List<CommitDataObject> = arrayListOf()

    override var alias: Collection<String>
        get() = TODO("Not yet implemented")
        set(value) {}

    override var created: Instant = Clock.System.now()
    override var modified: Instant? = null
    override var name: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    constructor(response: CommitResponse): this() {
        id = response.id
        owningProject = response.owningProject?.id
        description = response.description?:""
        previousIdList = response.previousCommit?.map { it.id!! }!!
    }
    override fun toString() =
        "CommitImplementation { project = $owningProject, description = $description, #elements = ${payload.size} }f"
}