package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(Length.Meter.copy(2), Mass.Kilogram.copy(), Duration.Second.copy(-3))

open class Power(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Power", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Watt : Power("watt", "W", NoPrefix, 1.0)
    object Horsepower : Power("horsepower", "HP", NoPrefix, 745.69987158227022)
    object PferdeStaerke : Power("metric horsepower", "PS", NoPrefix, 735.49875)

    override fun copy(): Power {
        return Power(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}