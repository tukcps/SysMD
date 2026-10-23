package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseQuantityKind
import com.github.tukcps.sysmd.quantities.unit

data object EmptyUnit : BaseQuantityKind("Emptiness")
{
    val Empty = unit("empty", "?")

    override val baseUnit = setOf(Empty)
}
