package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object Permittivity : DerivedQuantityKind("Permittivity", setOf(
    Duration.Second.pow(4), ElectricCurrent.Ampere.pow(2),
    Length.Meter.pow(-3), Mass.Kilogram.pow(-1)
))
{
    val FaradPerMeter = unit("farad per meter", "F/m")
}
