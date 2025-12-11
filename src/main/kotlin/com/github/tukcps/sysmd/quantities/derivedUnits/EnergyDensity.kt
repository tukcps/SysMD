package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(Mass.Kilogram.copy(), Duration.Second.copy(-2), Length.Meter.copy(-1))

open class EnergyDensity(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "EnergyDensity", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object JoulePerCubicMeter : EnergyDensity("Joule per cubic meter", "J/m^3", NoPrefix, 1.0)

    override fun copy(): EnergyDensity {
        return EnergyDensity(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}