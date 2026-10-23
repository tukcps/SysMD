package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.*
import com.github.tukcps.sysmd.quantities.baseUnits.Length

data object Volume : DerivedQuantityKind("Volume", setOf(
    Length.Meter.pow(3)
))
{
    val CubicMeter = unit("cubic meter", "m^3")
    val Liter = unit("liter", "l", "0.001")

    val CUBIC_METERS_PER_CUBIC_INCH = Length.METERS_PER_INCH.pow(3)!!
    /** 1 gallon := 231 in³ */
    val CUBIC_METERS_PER_GALLON = CUBIC_METERS_PER_CUBIC_INCH * 231
    val CUBIC_METERS_PER_QUART = CUBIC_METERS_PER_GALLON / 4
    val CUBIC_METERS_PER_PINT = CUBIC_METERS_PER_GALLON / 8
    val CUBIC_METERS_PER_BARREL = CUBIC_METERS_PER_GALLON * 42
    // no fluid ounce

    val Pint = unit("pint", "pt", CUBIC_METERS_PER_PINT)
    val Quart = unit("quart", "qt", CUBIC_METERS_PER_QUART)
    val Gallon = unit("gallon", "gal", CUBIC_METERS_PER_GALLON)
    val Barrel = unit("barrel", "bbl", CUBIC_METERS_PER_BARREL)
}
