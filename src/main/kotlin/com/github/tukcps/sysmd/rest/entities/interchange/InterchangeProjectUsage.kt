package io.github.tukcps.sysmlv2.interchange

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
open class InterchangeProjectUsage(
    open var resource: Url,
    open var versionConstraint: String? = null
)