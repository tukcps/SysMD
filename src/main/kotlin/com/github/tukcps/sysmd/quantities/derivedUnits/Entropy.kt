package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedQuantityKind
import com.github.tukcps.sysmd.quantities.unit
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.Length
import com.github.tukcps.sysmd.quantities.baseUnits.Mass
import com.github.tukcps.sysmd.quantities.baseUnits.ThermodynamicTemperature

data object Entropy : DerivedQuantityKind("Entropy", setOf(
    Length.Meter.pow(2), Mass.Kilogram,
    Duration.Second.pow(-2), ThermodynamicTemperature.Kelvin.pow(-1)
))
{
    val JoulePerKelvin = unit("joule per kelvin", "J/K")
}
