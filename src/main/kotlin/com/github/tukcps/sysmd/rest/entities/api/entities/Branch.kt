@file:Suppress("unused")

package com.github.tukcps.sysmd.rest.entities.api.entities

import kotlinx.datetime.Instant
import kotlin.uuid.Uuid

interface Branch: CommitReference {
    var created: Instant?
    var modified: Instant?
    fun owningProjectId(): Uuid?
}