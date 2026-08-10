package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.services.Runlevel

/**
 * @param runlevel The runlevel to which the session will be started after loading the libraries.
 */
class SessionSettings(

    /** if true, the compiler adds implicit specializations */
    var addImplied: Boolean = true,

    /** if true, the compiler adds default multiplicities (which is not needed by standard) */
    var addDefaultMultiplicity: Boolean = false,

    /** if true, the compiler adds constraints (range, type) to generated types */
    var addConstraints: Boolean = true,

    /** The runlevel to which the session will be started after building it. */
    var runlevel: Runlevel = Runlevel.NAMES_RESOLVED,

    /** Whether to report namespaces with two similar names or not. */
    var reportDoubleNames: Boolean = true,

    /** Whether to include owning relationships in the root namespace in the output of the compiler. */
    var includeOwningRelationshipsToRoot: Boolean = true,

    /**
     * The maximum integer representation; values above will be considered as "no upper/lower bound" in specifications,
     * but not as really infinity.
     */
    var maxInt: Long = Int.MAX_VALUE.toLong(),
    var minInt: Long = - Int.MAX_VALUE.toLong(),
    var minReal: Double = - Float.MAX_VALUE.toDouble(),
    var maxReal: Double = Float.MAX_VALUE.toDouble(),
)