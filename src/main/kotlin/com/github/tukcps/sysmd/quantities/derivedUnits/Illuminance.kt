package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.LuminousIntensity

private var siUnitSet = setOf(LuminousIntensity.Candela.copy(), Length.Meter.copy(-2))

open class Illuminance(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Illuminance", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Lux : Illuminance("lux", "lx", NoPrefix, 1.0)

    override fun copy(): Illuminance {
        return Illuminance(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}