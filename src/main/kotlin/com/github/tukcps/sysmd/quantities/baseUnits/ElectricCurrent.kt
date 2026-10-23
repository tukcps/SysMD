package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseQuantityKind
import com.github.tukcps.sysmd.quantities.unit

data object ElectricCurrent : BaseQuantityKind("ElectricCurrent")
{
    val Ampere = unit("ampere", "A")

    override val baseUnit = setOf(Ampere)
}
