package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

private var siUnitSet = setOf(Mass.Kilogram.copy(), Length.Meter.copy(-3))

open class MassDensity(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "MassDensity", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object KilogramPerCubicMeter : MassDensity("kilogram per cubic meter", "kg/m^3", NoPrefix, 1.0)

    override fun copy(): MassDensity {
        return MassDensity(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}