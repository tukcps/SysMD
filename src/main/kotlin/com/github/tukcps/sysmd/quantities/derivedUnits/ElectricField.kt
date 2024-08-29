package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(
    Length.Meter.copy(), Mass.Kilogram.copy(),
    Time.Second.copy(-3), ElectricCurrent.Ampere.copy(-1)
)

open class ElectricField(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "electric field", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object NewtonPerCoulomb : ElectricField("newton per coulomb", "N/C", NoPrefix, 1.0)

    override fun copy(): ElectricField {
        return ElectricField(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}