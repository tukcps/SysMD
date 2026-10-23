package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object Pressure : DerivedQuantityKind("Pressure", setOf(
    Mass.Kilogram, Length.Meter.pow(-1), Duration.Second.pow(-2)
))
{
    val PASCAL_PER_PSI = Force.NEWTONS_PER_POUND / Area.SQ_METERS_PER_SQ_INCH

    val Pascal = unit("pascal", "Pa")
    val Bar = unit("bar", "bar", 100000)
    val PoundPerSquareInch = unit("psi", "psi", PASCAL_PER_PSI)
}
