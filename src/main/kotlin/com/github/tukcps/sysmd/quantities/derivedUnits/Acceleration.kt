package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(Length.Meter.copy(), Duration.Second.copy(-2))

open class Acceleration(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Acceleration", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object MeterPerSecondSquared : Acceleration("Meter per second squared", "m/s^2", NoPrefix, convFac = 1.0)

    override fun copy(): Acceleration {
        return Acceleration(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}