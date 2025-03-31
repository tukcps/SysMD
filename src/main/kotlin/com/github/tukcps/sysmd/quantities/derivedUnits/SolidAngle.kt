package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

private var siUnitSet = emptySet<BaseUnit>()

open class SolidAngle(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "SolidAngle", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Steradian : Pressure("steradian", "sr", NoPrefix, 1.0)

    override fun copy(): SolidAngle {
        return SolidAngle(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}