package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix
import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.StorageCapacity

private var siUnitSet = setOf(StorageCapacity.Bit.copy(), Duration.Second.copy(-1))


open class BitRate(name: String, symbol: String, prefix: Prefix, convFac: Double, exponent: Int = 1, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "BitRate", siUnitSet, convFac, exponent, isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object BitsPerSecond : BitRate("bits per second", "bit/s", NoPrefix, 1.0)
    object BytesPerSecond : BitRate("Bytes per second", "B/s", NoPrefix, 8.0)

    override fun copy(): BitRate {
        return BitRate(name, symbol, prefix, convFac, exponent, isLogarithmic)
    }
}