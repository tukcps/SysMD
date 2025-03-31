package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(Time.Second.copy(1))

open class Timestamp(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Timestamp", siUnitSet, convFac, exponent, isLogarithmic, isDifference = false) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object UnixTimeStamp : Timestamp("unixtimestamp", "uts", NoPrefix, 1.0)

    override fun copy(): Timestamp {
        return Timestamp(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }

}