package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseQuantityKind
import com.github.tukcps.sysmd.quantities.unit

data object Duration : BaseQuantityKind("Duration")
{
    const val SECONDS_PER_MINUTE = 60
    const val SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE
    const val SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR
    const val SECONDS_PER_YEAR = 365 * SECONDS_PER_DAY // or 365.2425?

    /**
     * generate UnitObjects and add them to the UnitList
     */
    val Second = unit("second", "s", 1)
    val Minute = unit("minute", "min", SECONDS_PER_MINUTE)
    val Hour = unit("hour", "h", SECONDS_PER_HOUR)
    val Day = unit("day", "d", SECONDS_PER_DAY)
    val Year = unit("year", "a", SECONDS_PER_YEAR)

    override val baseUnit = setOf(Second)
}