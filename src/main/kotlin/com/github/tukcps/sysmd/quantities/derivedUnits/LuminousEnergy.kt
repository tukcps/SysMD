package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.LuminousIntensity

data object LuminousEnergy : DerivedQuantityKind("LuminousEnergy", setOf(
    LuminousIntensity.Candela, Duration.Second
))
{
    val LumenSecond = unit("luminous energy", "lms")
}
