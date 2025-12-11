package com.github.tukcps.sysmd.quantities.derivedUnits

import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.DerivedUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix

private var siUnitSet = emptySet<BaseUnit>()

open class DimensionOne(name: String, symbol: String, prefix: Prefix, convFac: Double, isLogarithmic: Boolean = false) :
    DerivedUnit(name, symbol, prefix, "DimensionOne", siUnitSet, convFac, isLogarithmic = isLogarithmic) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object One : DimensionOne("one", "1", NoPrefix, 1.0)
    object Radiant : DimensionOne("radiant","π",NoPrefix,kotlin.math.PI)
    object Degree : DimensionOne("degree","°",NoPrefix,2*kotlin.math.PI/360)
    object Percent : DimensionOne("percent", "%", NoPrefix, 0.01)
    object Decibel : DimensionOne("decibel", "dB", NoPrefix, 1.0, isLogarithmic = true)

    override fun copy(): DimensionOne {
        return DimensionOne(name, symbol, prefix, convFac, isLogarithmic)
    }
}