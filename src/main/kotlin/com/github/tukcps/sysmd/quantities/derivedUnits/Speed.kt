package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(Length.Meter.copy(), Duration.Second.copy(-1))

open class Speed(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Speed", siUnitSet, convFac, exponent, isLogarithmic, alternativeDomain = "Velocity") {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object MeterPerSeconds : Speed("meter per second", "m/s", NoPrefix, 1.0)
    object MilesPerHour : Speed("miles per hour", "mph", NoPrefix, 0.44704)
    object Knot : Speed("knot", "kt", NoPrefix, 0.514444444444444)

    override fun copy(): Speed {
        return Speed(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}