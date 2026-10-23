package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.div
import java.math.BigDecimal
import java.math.MathContext

data object Speed : DerivedQuantityKind("Speed", setOf(
    Length.Meter, Duration.Second.pow(-1)
))
{
    /** 1 knot = 1 nautical mile / 1 hour. Has no exact (arbitrary-precision) decimal representation. */
    val MS_PER_KNOT = RealRange(Length.METERS_PER_NAUTICAL_MILE.toDouble()) / 3600.0
    val MS_PER_MPH = Length.METERS_PER_MILE / 3600

    val MeterPerSeconds = unit("meter per second", "m/s")
    val MilesPerHour = unit("miles per hour", "mph", MS_PER_MPH)
    val Knot = unit("knot", "kt", MS_PER_KNOT)
}
