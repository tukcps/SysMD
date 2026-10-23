package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object ElectricFieldStrength : DerivedQuantityKind("ElectricFieldStrength", setOf(
    Length.Meter, Mass.Kilogram,
    Duration.Second.pow(-3), ElectricCurrent.Ampere.pow(-1)
))
{
    val NewtonPerCoulomb = unit("newton per coulomb", "N/C")
}
