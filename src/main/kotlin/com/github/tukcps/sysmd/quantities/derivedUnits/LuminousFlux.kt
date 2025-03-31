package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.LuminousIntensity

private var siUnitSet = setOf(LuminousIntensity.Candela.copy())

open class LuminousFlux(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "LuminousFlux", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Lumen : LuminousFlux("lumen", "lm", NoPrefix, 1.0)

    override fun copy(): LuminousFlux {
        return LuminousFlux(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}