package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object MagneticFlux : DerivedQuantityKind("MagneticFlux", setOf(
    Length.Meter.pow(2), Mass.Kilogram,
    Duration.Second.pow(-2), ElectricCurrent.Ampere.pow(-1)
))
{
    val Weber = unit("weber", "Wb")
}
