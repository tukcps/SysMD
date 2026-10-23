package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length

data object DoseEquivalent : DerivedQuantityKind("DoseEquivalent", setOf(
    Length.Meter.pow(2), Duration.Second.pow(-2)
))
{
    val Sievert = unit("sievert", "Sv")
}
