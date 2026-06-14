package com.github.tukcps.sysmd.rest.entities.api.entities

import io.ktor.http.*
import kotlin.uuid.Uuid


/**
 * PIM Baseclass for all records;
 * Ch. 7.1.1 of SysML v2 API std.
 */
interface Record {
    var id: Uuid
    fun resourceIdentifier(): Url? = null
    var alias: Collection<String>   // includes name at least
    var name: String?               // Unique, --> humanIdentifier
    var description: String
}