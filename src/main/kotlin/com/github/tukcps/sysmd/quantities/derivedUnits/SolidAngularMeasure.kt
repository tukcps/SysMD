package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

private var siUnitSet = emptySet<BaseUnit>()

open class SolidAngularMeasure(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "SolidAngularMeasure", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Steradian : SolidAngularMeasure("steradian", "sr", NoPrefix, 1.0)

    override fun copy(): SolidAngularMeasure {
        return SolidAngularMeasure(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}