package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object MassFlow : DerivedQuantityKind("MassFlow", setOf(
    Mass.Kilogram, Duration.Second.pow(-1)
))
{
    val KilogramPerSecond = unit("kilogram per second", "kg/s")
}
