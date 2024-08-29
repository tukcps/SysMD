package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

open class Mass(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "mass", convFac, exponent) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Kilogram : Mass("kilogram", "kg", NoPrefix, 1.0)
    object Gram : Mass("gram", "g", NoPrefix, 0.001)
    object Tonne : Mass("tonne", "t", NoPrefix, 1000.0)
    object ShortTon : Mass("short ton", "tn", NoPrefix, 907.18474)
    object Pound : Mass("pound", "lb", NoPrefix, 0.45359237)
    object Ounce : Mass("ounce", "oz", NoPrefix, 0.028349523125)
    object Carat : Mass("carat", "ct", NoPrefix, 0.0002)
    object Grain : Mass("grain", "gr", NoPrefix, 0.00006479891)


    override fun copy(exponentValue: Int): Mass {
        return Mass(name, symbol, prefix, convFac, exponentValue)
    }
}