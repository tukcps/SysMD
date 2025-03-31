package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

open class Time(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "Time", convFac, exponent, isDifference = false) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Second : Time("second", "s", NoPrefix, 1.0)
    object Minute : Time("minute", "min", NoPrefix, 60.0)
    object Hour : Time("hour", "h", NoPrefix, 3600.0)
    object Day : Time("day", "d", NoPrefix, 86400.0)
    object Year : Time("year", "a", NoPrefix, 31536000.0)


    override fun copy(exponentValue: Int): Time {
        return Time(name, symbol, prefix, convFac, exponentValue)
    }
}