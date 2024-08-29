package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

open class Currency(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "currency", convFac, exponent) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Euro : Currency("euro", "€", NoPrefix)

    override fun copy(exponentValue: Int): Currency {
        return Currency(name, symbol, prefix, convFac, exponentValue)
    }
}