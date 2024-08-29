package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length

private var siUnitSet = setOf(Length.Meter.copy(2))

open class Area(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "area", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object SquareMeters : Area("square meters", "m^2", NoPrefix, 1.0)
    object Hectare : Area("hectare", "ha", NoPrefix, 10000.0)
    object Acre : Area("acre", "ac", NoPrefix, 4046.86)

    override fun copy(): Area {
        return Area(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}