package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(
    Length.Meter.copy(2), Mass.Kilogram.copy(),
    Duration.Second.copy(-3), ElectricCurrent.Ampere.copy(-1)
)
open class ElectricPotentialDifferenceValue(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isDifference: Boolean) :
    DerivedUnit(name, symbol, prefix, "ElectricPotentialDifference", siUnitSet, convFac, exponent, isDifference = isDifference) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Volt : ElectricPotentialDifferenceValue("volt", "V", NoPrefix, 1.0, isDifference = false)

    override fun copy(): ElectricPotentialDifferenceValue {
        return ElectricPotentialDifferenceValue(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}