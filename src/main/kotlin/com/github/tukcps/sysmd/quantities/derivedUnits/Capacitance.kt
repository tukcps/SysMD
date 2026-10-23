package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object Capacitance : DerivedQuantityKind("Capacitance", setOf(
    Duration.Second.pow(4), ElectricCurrent.Ampere.pow(2),
    Length.Meter.pow(-2), Mass.Kilogram.pow(-1)
))
{
    val Farad = unit("farad", "F")
}
