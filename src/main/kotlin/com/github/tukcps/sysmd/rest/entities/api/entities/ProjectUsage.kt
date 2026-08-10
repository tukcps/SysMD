package com.github.tukcps.sysmd.rest.entities.api.entities

import kotlin.uuid.Uuid

/**
 * A project usage refers to another project used within a project.
 * It is part of commit data.
 */
interface ProjectUsage {
    var usedCommit: Uuid
    var owningProject: Uuid
}