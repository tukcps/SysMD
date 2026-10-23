package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.LuminousIntensity

data object LuminousFlux : DerivedQuantityKind("LuminousFlux", setOf(
    LuminousIntensity.Candela
))
{
    val Lumen = unit("lumen", "lm")
}
