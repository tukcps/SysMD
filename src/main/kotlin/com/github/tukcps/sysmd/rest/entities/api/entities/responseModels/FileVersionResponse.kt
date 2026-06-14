package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant


/**
 * Not in standard.
 *
 */
open class FileVersionResponse(
    var name: String? = null,
    var url: String? = null,
    var type: String? = null,
    var size: Long = 0L,
    var id: String? = null,
    var version: Long = 0L,
    var created: Instant = Clock.System.now(),
    var modified: Instant? = null
)