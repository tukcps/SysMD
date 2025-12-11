package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(
    Length.Meter.copy(), Mass.Kilogram.copy(),
    Duration.Second.copy(-3), ElectricCurrent.Ampere.copy(-1)
)

open class ElectricFieldStrength(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "ElectricFieldStrength", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object NewtonPerCoulomb : ElectricFieldStrength("newton per coulomb", "N/C", NoPrefix, 1.0)

    override fun copy(): ElectricFieldStrength {
        return ElectricFieldStrength(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}