package com.github.tukcps.sysmd.rest.entities.requests


// class Code (var body: String = "") // Needed for valid JSON
// Easier to pass everything in a request body -> not sure if it fits under requests
data class CodeRequest(
    val language: String = "SysML",
    val level: Int = 1,
    val code: String = ""
)