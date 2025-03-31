package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

open class InformationCapacity(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "InformationCapacity", convFac, exponent) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Bit : InformationCapacity("bit", "bit", NoPrefix, 1.0)
    object Byte : InformationCapacity("byte", "B", NoPrefix, 8.0)

    /**
     * Returns Units of current Type
     */
    override fun copy(exponentValue: Int): InformationCapacity {
        return InformationCapacity(name, symbol, prefix, convFac, exponentValue)
    }
}