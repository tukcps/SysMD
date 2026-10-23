package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object MomentOfInertia : DerivedQuantityKind("MomentOfInertia", setOf(
    Length.Meter.pow(2), Mass.Kilogram
))
{
    val KilogramSquareMeter = unit("kilogram square meter", "kg m^2")
}
