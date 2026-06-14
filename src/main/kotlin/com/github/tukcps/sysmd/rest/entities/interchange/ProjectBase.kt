package com.github.tukcps.sysmd.rest.entities.interchange

import kotlin.uuid.Uuid

/**
 * A common base class for the InterchangeProject (KerML ch. 8) and Project (API).
 */
interface ProjectBase {
    var id: Uuid
    val name: String?
    val description: String?
}