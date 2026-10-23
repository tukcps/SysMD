package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.*

data object Length : BaseQuantityKind("Length")
{
    // per NIST SP 811 and ISO 80000-3
    val METERS_PER_YARD = exactDecimal("0.9144")
    val METERS_PER_FOOT = METERS_PER_YARD / 3
    val METERS_PER_INCH = METERS_PER_FOOT / 12
    val METERS_PER_MILE = METERS_PER_FOOT * 5280
    // per ISO 80000-3
    const val METERS_PER_NAUTICAL_MILE = 1852

    val Meter = unit("meter", "m")
    val NauticalMile = unit("nautical mile", "nmi", METERS_PER_NAUTICAL_MILE)
    val Mile = unit("mile", "mi", METERS_PER_MILE)
    val Yard = unit("yard", "yd", METERS_PER_YARD)
    val Feet = unit("feet", "ft", METERS_PER_FOOT)
    val Inch = unit("inch", "inch", METERS_PER_INCH)

    override val baseUnit = setOf(Meter)
}
