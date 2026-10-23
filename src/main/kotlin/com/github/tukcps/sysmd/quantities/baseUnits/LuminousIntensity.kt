package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseQuantityKind
import com.github.tukcps.sysmd.quantities.unit

data object LuminousIntensity : BaseQuantityKind("LuminousIntensity")
{
    val Candela = unit("candela", "cd")

    override val baseUnit = setOf(Candela)
}
