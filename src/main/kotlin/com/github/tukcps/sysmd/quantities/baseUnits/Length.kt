package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

open class Length(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "Length", convFac, exponent) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Meter : Length("meter", "m", NoPrefix)
    object Mile : Length("mile", "mi", NoPrefix, 1609.344)
    object NauticalMile : Length("nautical mile", "nmi", NoPrefix, 1852.0)
    object Yard : Length("yard", "ya", NoPrefix, 0.9144)
    object Feet : Length("feet", "ft", NoPrefix, 0.3048)
    object Inch : Length("inch", "in", NoPrefix, 0.0254)

    override fun copy(exponentValue: Int): Length {
        return Length(name, symbol, prefix, convFac, exponentValue)
    }
}