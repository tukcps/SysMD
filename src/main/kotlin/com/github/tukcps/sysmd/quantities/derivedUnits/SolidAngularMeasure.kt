package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit

data object SolidAngularMeasure : DerivedQuantityKind("SolidAngularMeasure", emptySet())
{
    val Steradian = unit("steradian", "sr")
}
