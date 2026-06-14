package com.github.tukcps.sysmd.rest.entities.api.entities

import java.util.*

/**
 * A project usage refers to another project used within a project.
 * It is part of commit data.
 */
interface ProjectUsage {
    var usedCommit: UUID
    var owningProject: UUID
}