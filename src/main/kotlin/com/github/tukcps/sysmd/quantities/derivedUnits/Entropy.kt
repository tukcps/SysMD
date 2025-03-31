package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Temperature
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(
    Length.Meter.copy(2), Mass.Kilogram.copy(),
    Time.Second.copy(-2), Temperature.Kelvin.copy(-1)
)

open class Entropy(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Entropy", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object JoulePerKelvin : Entropy("joule per kelvin", "J/K", NoPrefix, 1.0)

    override fun copy(): Entropy {
        return Entropy(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}