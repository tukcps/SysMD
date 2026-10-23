package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.StorageCapacity

data object BitRate : DerivedQuantityKind("BitRate", setOf(
    StorageCapacity.Bit, Duration.Second.pow(-1)
))
{
    val BitsPerSecond = unit("bits per second", "bit/s")
    val BytesPerSecond = unit("Bytes per second", "B/s", 8)
}
