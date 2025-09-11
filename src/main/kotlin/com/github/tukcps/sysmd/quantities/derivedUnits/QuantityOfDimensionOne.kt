package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

private var siUnitSet = emptySet<BaseUnit>()

open class QuantityOfDomainOne(name: String, symbol: String, prefix: Prefix, convFac: Double, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "QuantityOfDomainOne", siUnitSet, convFac, isLogarithmic = isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object One : QuantityOfDomainOne("one", "1", NoPrefix, 1.0)
    object Radiant : QuantityOfDomainOne("radiant","π",NoPrefix,kotlin.math.PI)
    object Degree : QuantityOfDomainOne("degree","°",NoPrefix,2*kotlin.math.PI/360)
    object Percent : QuantityOfDomainOne("percent", "%", NoPrefix, 0.01)
    object Decibel : QuantityOfDomainOne("decibel", "dB", NoPrefix, 1.0, isLogarithmic = true)

    override fun copy(): QuantityOfDomainOne {
        return QuantityOfDomainOne(name, symbol, prefix, convFac, isLogarithmic)
    }
}