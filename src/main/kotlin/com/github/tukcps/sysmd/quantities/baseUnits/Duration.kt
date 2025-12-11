package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

open class Duration(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "Duration", convFac, exponent, isDifference = false, alternativeDomain = "Time") {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Second : Duration("second", "s", NoPrefix, 1.0)
    object Minute : Duration("minute", "min", NoPrefix, 60.0)
    object Hour : Duration("hour", "h", NoPrefix, 3600.0)
    object Day : Duration("day", "d", NoPrefix, 86400.0)
    object Year : Duration("year", "a", NoPrefix, 31536000.0)


    override fun copy(exponentValue: Int): Duration {
        return Duration(name, symbol, prefix, convFac, exponentValue)
    }
}