package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Length

private var siUnitSet = setOf(Length.Meter.copy(3))

open class Volume(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "volume", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object CubicMeter : Volume("cubic meter", "m^3", NoPrefix, 1.0)
    object Liter : Volume("liter", "l", NoPrefix, 0.001)
    object Pint : Volume("pint", "pt", NoPrefix, 0.000473176473)
    object Quart : Volume("quart", "qt", NoPrefix, 0.000946352946)
    object Gallon : Volume("gallon", "gal", NoPrefix, 0.003785411784)
    object Barrel : Volume("barrel", "bbl", NoPrefix, 0.119240471196)

    override fun copy(): Volume {
        return Volume(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}