package com.github.tukcps.sysmd.model.datamodel

import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


@Serializable
data class IdentifiedImplementation(
    override var id: Uuid? = null,
) : Identified {
    override fun clone() = copy()
}

/**
 * A function to clone a list
 */
fun <T: Identified> List<T>.clone(): MutableList<T> {
    val result = mutableListOf<T>()
    this.forEach { identity ->
        @Suppress("UNCHECKED_CAST")
        result.add(identity.clone() as T)
    }
    return result
}
