package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Duration

private var siUnitSet = setOf(Duration.Second.copy(-1))

open class Frequency(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "Frequency", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Hertz : Frequency("hertz", "Hz", NoPrefix, 1.0)
    object FLOPS : Frequency("floating point operations per second", "FLOPS", NoPrefix, 1.0)

    override fun copy(): Frequency {
        return Frequency(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}