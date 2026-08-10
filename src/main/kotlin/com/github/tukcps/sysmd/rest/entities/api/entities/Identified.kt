package com.github.tukcps.sysmd.rest.entities.api.entities

import kotlinx.serialization.SerialName
import kotlin.uuid.Uuid

/**
 * Base class for all entities that are identified by an id.
 */
interface Identified: Cloneable {
    @SerialName("@id")
    var id: Uuid?
    public override fun clone(): Identified
}
