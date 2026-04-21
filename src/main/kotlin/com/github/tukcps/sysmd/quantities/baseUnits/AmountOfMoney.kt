package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

open class AmountOfMoney(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "AmountOfMoney", convFac, exponent) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Euro : AmountOfMoney("euro", "EUR", NoPrefix)
    object Dollar : AmountOfMoney("USD", "USD", NoPrefix)
    object Pound : AmountOfMoney("pound", "GBP", NoPrefix)

    override fun copy(exponentValue: Int): AmountOfMoney {
        return AmountOfMoney(name, symbol, prefix, convFac, exponentValue)
    }
}