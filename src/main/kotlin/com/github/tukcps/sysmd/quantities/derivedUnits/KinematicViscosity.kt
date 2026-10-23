package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length

data object KinematicViscosity : DerivedQuantityKind("KinematicViscosity", setOf(
    Length.Meter.pow(2), Duration.Second.pow(-1)
))
{
    val Stokes = unit("stokes", "St", "0.0001")
}
