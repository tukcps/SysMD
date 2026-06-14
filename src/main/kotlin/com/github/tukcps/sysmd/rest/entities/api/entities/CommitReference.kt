@file:Suppress("unused")
package com.github.tukcps.sysmd.rest.entities.api.entities

import kotlin.uuid.Uuid

interface CommitReference : Record {
    fun referencedCommitId(): Uuid?
}