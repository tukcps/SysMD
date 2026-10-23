package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.unit

object AbsorbedDose : DerivedQuantityKind("AbsorbedDose", setOf(
    Length.Meter.pow(2), Duration.Second.pow(-2)
))
{
    val Gray = unit("gray", "Gy")
}