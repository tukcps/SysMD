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
    Length.Meter.copy(-2), Mass.Kilogram.copy(-1)
)

open class Capacitance(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Capacitance", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Farad : Capacitance("farad", "F", NoPrefix, 1.0)

    override fun copy(): Capacitance {
        return Capacitance(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}