package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(Length.Meter.copy(2), Time.Second.copy(-2))

open class DoseEquivalent(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "dose equivalent", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Sievert : DoseEquivalent("sievert", "Sv", NoPrefix, 1.0)

    override fun copy(): DoseEquivalent {
        return DoseEquivalent(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}