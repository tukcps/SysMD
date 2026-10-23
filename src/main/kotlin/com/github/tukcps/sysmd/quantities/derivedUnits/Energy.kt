package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object Energy : DerivedQuantityKind("Energy", setOf(
    Length.Meter.pow(2), Mass.Kilogram, Duration.Second.pow(-2)
))
{
    val Joule = unit("joule", "J")
    val WattSecond = unit("watt-second", "Ws")
    val WattHour = unit("watt-hour", "Wh", 3600)
    // thermochemical calorie per NIST SP 811 and ISO 80000-5
    val Calorie = unit("calorie", "cal", "4.184")
}
