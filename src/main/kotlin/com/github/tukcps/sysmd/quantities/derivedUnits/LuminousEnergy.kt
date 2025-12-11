package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.LuminousIntensity
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(LuminousIntensity.Candela.copy(), Duration.Second.copy())

open class LuminousEnergy(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "LuminousEnergy", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object LumenSecond : LuminousEnergy("luminous energy", "lms", NoPrefix, 1.0)

    override fun copy(): LuminousEnergy {
        return LuminousEnergy(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}