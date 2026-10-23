package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseQuantityKind
import com.github.tukcps.sysmd.quantities.unit

data object StorageCapacity : BaseQuantityKind("StorageCapacity")
{
    val Bit = unit("bit", "bit")
    val Byte = unit("byte", "B", 8)

    override val baseUnit = setOf(Bit)
}
