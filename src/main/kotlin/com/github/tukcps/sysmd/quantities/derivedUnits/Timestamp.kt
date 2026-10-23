package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

data object Timestamp : DerivedQuantityKind("Timestamp", setOf(
    Duration.Second
))
{
    val UnixTimeStamp = unit("unixtimestamp", "uts")
}
