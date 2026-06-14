package com.github.tukcps.sysmd.rest.entities.interchange

import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    /**
     * The index of the project's global scope.
     * Key is the name, value the path to the model interchange file.
     * File path is relative to the root of the project interchange file archive.
     */
    var index: LinkedHashMap<String, String> = linkedMapOf(),
    var created: Instant = Clock.System.now(),
    var metamodel: Url? = null,
    var includesDerived: Boolean? = null,
    var includesImplied: Boolean? = null,
    var checkSum: Map<String, String>? = null   // not implemented
)