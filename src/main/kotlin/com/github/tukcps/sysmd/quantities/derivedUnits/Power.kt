package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object Power : DerivedQuantityKind("Power", setOf(
    Length.Meter.pow(2), Mass.Kilogram, Duration.Second.pow(-3)
))
{
    /** Defined as 550 ft lbf */
    val HORSE_POWER_IN_WATT = (Length.METERS_PER_FOOT * Force.NEWTONS_PER_POUND) * 550
    /** Defined as 75 m kgf */
    val PS_IN_WATT = Acceleration.STANDARD_GRAVITY * 75

    val Watt = unit("watt", "W")
    val Horsepower = unit("horsepower", "HP", HORSE_POWER_IN_WATT)
    val PferdeStaerke = unit("metric horsepower", "PS", PS_IN_WATT)
}
