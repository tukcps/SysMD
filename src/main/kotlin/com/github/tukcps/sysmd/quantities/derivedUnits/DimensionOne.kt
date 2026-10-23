package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.UnitOfKind
import com.github.tukcps.sysmd.quantities.unit
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.div

data object DimensionOne : DerivedQuantityKind("DimensionOne", emptySet())
{
    val RADIANS_PER_DEGREE = RealRange.PI / 180.0

    val One = unit("one", "1")
    val Pi = unit("pi", "π", RealRange.PI)
    val Degree = unit("degree", "°", RADIANS_PER_DEGREE)
    val Percent = unit("percent", "%", "0.01")
    val Decibel = UnitOfKind(this, "decibel", "dB", isLogarithmic = true)
    val FLOPs = unit("floating point operations", "FLOPs")
}
