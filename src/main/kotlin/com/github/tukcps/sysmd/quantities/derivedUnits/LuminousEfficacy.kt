package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.LuminousIntensity
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object LuminousEfficacy : DerivedQuantityKind("LuminousEfficacy", setOf(
    Duration.Second.pow(3), LuminousIntensity.Candela,
    Length.Meter.pow(-2), Mass.Kilogram.pow(-1)
))
{
    val LumenPerWatt = unit("lumen per watt", "lm/W")
}
