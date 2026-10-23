package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass

data object Force : DerivedQuantityKind("Force", setOf(
    Length.Meter, Mass.Kilogram, Duration.Second.pow(-2)
))
{
    /** The weight force exerted by a pound of mass under standard gravity */
    val NEWTONS_PER_POUND = Mass.KG_PER_POUND * Acceleration.STANDARD_GRAVITY

    val Newton = unit("newton", "N")
}
