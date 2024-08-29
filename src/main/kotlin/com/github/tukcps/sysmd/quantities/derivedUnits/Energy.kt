package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(Length.Meter.copy(2), Mass.Kilogram.copy(), Time.Second.copy(-2))

open class Energy(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "energy", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Joule : Energy("joule", "J", NoPrefix, 1.0)
    object WattSecond : Energy("watt-second", "Ws", NoPrefix, 1.0)
    object WattHour : Energy("watt-hour", "Wh", NoPrefix, 3600.0)
    object Calorie : Energy("calorie", "cal", NoPrefix, 4.184)

    override fun copy(): Energy {
        return Energy(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}