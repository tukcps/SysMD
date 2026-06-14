package com.github.tukcps.sysmd.rest.entities.api.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


interface Identified: Cloneable {
    @SerialName("@id")
    var id: Uuid?
    public override fun clone(): Identified
}

@Serializable
data class IdentifiedImpl(
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
