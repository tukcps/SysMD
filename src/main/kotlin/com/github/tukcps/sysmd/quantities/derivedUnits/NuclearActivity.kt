package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

data object NuclearActivity : DerivedQuantityKind("NuclearActivity", setOf(
    Duration.Second.pow(-1)
))
{
    val Becquerel = unit("becquerel", "Bq")
}
