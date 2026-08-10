@file:Suppress("unused")
package com.github.tukcps.sysmd.rest.entities.api.entities

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