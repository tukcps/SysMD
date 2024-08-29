package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Time

private var siUnitSet = setOf(Time.Second.copy(-1))

open class Activity(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "activity", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Becquerel : Activity("becquerel", "Bq", NoPrefix, 1.0)

    override fun copy(): Activity {
        return Activity(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}