package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(
    Time.Second.copy(4), ElectricCurrent.Ampere.copy(2),
    Length.Meter.copy(-3), Mass.Kilogram.copy(-1)
)

open class Permittivity(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Permittivity", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object FaradPerSecond : Permittivity("farad per meter", "F/m", NoPrefix, 1.0)

    override fun copy(): Permittivity {
        return Permittivity(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}