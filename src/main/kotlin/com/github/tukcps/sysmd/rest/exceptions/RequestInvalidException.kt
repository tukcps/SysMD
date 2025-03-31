package com.github.tukcps.sysmd.rest.exceptions

class RequestInvalidException(message: String):
    RuntimeException(message) {
    override fun toString(): String {
        return "REST API: $message"
    }
}