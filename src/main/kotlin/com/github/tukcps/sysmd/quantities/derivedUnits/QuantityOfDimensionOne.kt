package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

private var siUnitSet = emptySet<BaseUnit>()

open class QuantityOfDimensionOne(name: String, symbol: String, prefix: Prefix, convFac: Double, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "quantity of dimension one", siUnitSet, convFac, isLogarithmic = isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object One : QuantityOfDimensionOne("one", "1", NoPrefix, 1.0)
    object Radiant : QuantityOfDimensionOne("radiant","π",NoPrefix,kotlin.math.PI)
    object Degree : QuantityOfDimensionOne("degree","°",NoPrefix,2*kotlin.math.PI/360)
    object Percent : QuantityOfDimensionOne("percent", "%", NoPrefix, 0.01)
    object Decibel : QuantityOfDimensionOne("decibel", "dB", NoPrefix, 1.0, isLogarithmic = true)

    override fun copy(): QuantityOfDimensionOne {
        return QuantityOfDimensionOne(name, symbol, prefix, convFac, isLogarithmic)
    }
}