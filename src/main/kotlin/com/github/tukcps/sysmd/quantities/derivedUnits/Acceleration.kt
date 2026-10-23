package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.exactDecimal
import java.math.BigDecimal

data object Acceleration : DerivedQuantityKind("Acceleration", setOf(
    Length.Meter, Duration.Second.pow(-2)
)) {
    /** Standard freefall acceleration, per ISO-80000, in m/s² */
    val STANDARD_GRAVITY = exactDecimal("9.80665") // should we also add a G unit?

    val MeterPerSecondSquared = unit("Meter per second squared", "m/s^2", 1)
}