package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

data object Frequency : DerivedQuantityKind("Frequency", setOf(
    Duration.Second.pow(-1)
))
{
    val Hertz = unit("hertz", "Hz")
    val FLOPS = unit("floating point operations per second", "FLOPS")
}
