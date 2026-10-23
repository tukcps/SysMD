package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.ElectricCurrent

data object ElectricCharge : DerivedQuantityKind("ElectricCharge", setOf(
    Duration.Second, ElectricCurrent.Ampere
))
{
    val Coulomb = unit("coulomb", "C")
    val AmpereSecond = unit("ampere second", "As")
    val AmpereHours = unit("ampere hour", "Ah", 3600)
}
