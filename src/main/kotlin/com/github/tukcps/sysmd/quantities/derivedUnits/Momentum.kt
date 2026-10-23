package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object Momentum : DerivedQuantityKind("Momentum", setOf(
    Mass.Kilogram, Length.Meter, Duration.Second.pow(-1)
))
{
    val KilogramMeterPerSecond = unit("kilogram meter per second", "kg m/s")
}
