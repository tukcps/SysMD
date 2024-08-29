package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix


open class AmountOfSubstance(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "amount of substance", convFac, exponent) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Mole : AmountOfSubstance("mole", "mol", NoPrefix)

    override fun copy(exponentValue: Int): AmountOfSubstance {
        return AmountOfSubstance(name, symbol, prefix, convFac, exponentValue)
    }
}