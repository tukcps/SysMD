package com.github.tukcps.sysmd.rest.entities.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date

/**
 * The class creates a consistent/standard API exception handling structure across the entire project.
 *
 * @author Khushnood Adil Rafique
 */
class ExceptionResponse (
    /** exception response comes in this specific format:  */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    var timeStamp: Date? = Date(),
    var message: String? = null,
    var details: String? = null
)