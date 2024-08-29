package com.github.tukcps.sysmd.services.session

class SessionSettings {
    // If true, the parser will not catch exceptions. For debugging.
    var catchExceptions: Boolean = true

    // if true, the parser writes scanned text to stdio. For debugging.
    var log: Boolean = false

    var initialize = true

    /**
     * The maximum integer representation; values above will be considered as "no upper/lower bound" in specifications,
     * but not as really infinity.
     */
    var maxInt: Long = Int.MAX_VALUE.toLong()
    var minInt: Long = - Int.MAX_VALUE.toLong()
    var minReal: Double = - Float.MAX_VALUE.toDouble()
    var maxReal: Double = Float.MAX_VALUE.toDouble()
}