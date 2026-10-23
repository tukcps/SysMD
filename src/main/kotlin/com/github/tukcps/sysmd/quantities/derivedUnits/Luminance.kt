package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.LuminousIntensity

data object Luminance : DerivedQuantityKind("Luminance", setOf(
    LuminousIntensity.Candela, Length.Meter.pow(-2)
))
{
    val Stilb = unit("stilb", "sb", 10000)
}
