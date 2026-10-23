package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.baseUnits.Length

data object Area : DerivedQuantityKind("Area", setOf(
    Length.Meter.pow(2)
))
{
    val SQ_METERS_PER_SQ_FOOT = Length.METERS_PER_FOOT.pow(2)!!
    // 1 Acre = 43560 ft²
    val SQ_METERS_PER_ACRE = SQ_METERS_PER_SQ_FOOT * 43_560
    val SQ_METERS_PER_SQ_INCH = Length.METERS_PER_INCH.pow(2)!!

    val SquareMeters = unit("square meters", "m^2")
    val Hectare = unit("hectare", "ha", 10000)

    val Acre = unit("acre", "ac", SQ_METERS_PER_ACRE)
}
