package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object Conductance : DerivedQuantityKind("Conductance", setOf(
    Duration.Second.pow(3), ElectricCurrent.Ampere.pow(2),
    Length.Meter.pow(-2), Mass.Kilogram.pow(-1)
))
{
    val Siemens = unit("siemens", "S")
}
