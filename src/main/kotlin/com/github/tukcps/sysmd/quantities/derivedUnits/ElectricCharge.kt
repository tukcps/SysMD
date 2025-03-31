package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(Time.Second.copy(), ElectricCurrent.Ampere.copy())

open class ElectricCharge(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "ElectricCharge", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Coulomb : ElectricCharge("coulomb", "C", NoPrefix, 1.0)
    object AmpereSecond : ElectricCharge("ampere second", "As", NoPrefix, 1.0)
    object AmpereHours : ElectricCharge("ampere hour", "Ah", NoPrefix, 3600.0)

    override fun copy(): ElectricCharge {
        return ElectricCharge(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}