@file:Suppress("unused")
package com.github.tukcps.sysmd.rest.entities.api.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid


/**
 * A commit
 */
interface Commit: Record {
    var owningProject: Uuid?
    var created: Instant
    var modified: Instant?

    fun payloadIdList(): List<Uuid>
    fun previousCommitIdList(): List<Uuid>
}

data class CommitImplementation(
    override var owningProject: Uuid? = null,
    override var created: Instant = Clock.System.now(),
    override var modified: Instant? = null,
    override var id: Uuid,
    override var alias: Collection<String> = emptyList(),
    override var name: String? = null,
    override var description: String = ""
): Commit {
    override fun payloadIdList(): List<Uuid> { TODO("Not yet implemented") }
    override fun previousCommitIdList(): List<Uuid> { TODO("Not yet implemented") }
}