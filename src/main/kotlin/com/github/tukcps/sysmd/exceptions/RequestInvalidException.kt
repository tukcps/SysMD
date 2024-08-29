package com.github.tukcps.sysmd.exceptions

class RequestInvalidException(message: String):
    RuntimeException(message) {
    var errorMessage: String? = message

    override fun toString(): String {
        return "AGILA: $message"
    }
}
