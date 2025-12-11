package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(Duration.Second.copy(-1))

open class NuclearActivity(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "NuclearActivity", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Becquerel : NuclearActivity("becquerel", "Bq", NoPrefix, 1.0)

    override fun copy(): NuclearActivity {
        return NuclearActivity(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}