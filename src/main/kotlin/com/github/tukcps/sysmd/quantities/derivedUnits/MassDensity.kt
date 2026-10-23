package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object MassDensity : DerivedQuantityKind("MassDensity", setOf(
    Mass.Kilogram, Length.Meter.pow(-3)
))
{
    val KilogramPerCubicMeter = unit("kilogram per cubic meter", "kg/m^3")
}
