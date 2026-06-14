package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels

/**
 * Not in standard.
 */
class FileResponse(
    var name: String,
    var url: String,
    var type: String,
    var size: Long,
    var id: String){
    // Just for Jackson deserialization ... constructor without parameters.
    private constructor(): this(name="", url="", type="",0, id="")
}