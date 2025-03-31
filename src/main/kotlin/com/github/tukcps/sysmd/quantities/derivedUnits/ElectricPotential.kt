package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(
    Length.Meter.copy(2), Mass.Kilogram.copy(),
    Time.Second.copy(-3), ElectricCurrent.Ampere.copy(-1)
)

open class ElectricPotential(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isDifference: Boolean) :
    DerivedUnit(name, symbol, prefix, "ElectricPotential", siUnitSet, convFac, exponent, isDifference = isDifference) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Volt : ElectricPotential("volt", "φ", NoPrefix, 1.0, isDifference = false)

    override fun copy(): ElectricPotential {
        return ElectricPotential(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}