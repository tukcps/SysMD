package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

open class ElectricCurrent(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "ElectricCurrent", convFac, exponent) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Ampere : ElectricCurrent("ampere", "A", NoPrefix)

    override fun copy(exponentValue: Int): ElectricCurrent {
        return ElectricCurrent(name, symbol, prefix, convFac, exponentValue)
    }
}