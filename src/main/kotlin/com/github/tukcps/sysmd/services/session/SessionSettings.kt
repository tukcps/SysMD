package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.services.Runlevel

/**
 * @param runlevel The runlevel to which the session will be started after loading the libraries.
 */
class SessionSettings(
    // If true, the parser will not catch exceptions. For debugging.
    var catchExceptions: Boolean = true,

    /**
     * The runlevel to which the session will be started after building it.
     */
    var runlevel: Runlevel = Runlevel.NAMES_RESOLVED,

    /**
     * The maximum integer representation; values above will be considered as "no upper/lower bound" in specifications,
     * but not as really infinity.
     */
    var maxInt: Long = Int.MAX_VALUE.toLong(),
    var minInt: Long = - Int.MAX_VALUE.toLong(),
    var minReal: Double = - Float.MAX_VALUE.toDouble(),
    var maxReal: Double = Float.MAX_VALUE.toDouble(),
)