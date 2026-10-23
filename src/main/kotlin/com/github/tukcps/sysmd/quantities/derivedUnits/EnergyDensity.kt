package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object EnergyDensity : DerivedQuantityKind("EnergyDensity", setOf(
    Mass.Kilogram, Duration.Second.pow(-2), Length.Meter.pow(-1)
))
{
    val JoulePerCubicMeter = unit("Joule per cubic meter", "J/m^3")
}
