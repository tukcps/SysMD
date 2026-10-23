package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseQuantityKind
import com.github.tukcps.sysmd.quantities.unit

data object AmountOfSubstance : BaseQuantityKind("AmountOfSubstance")
{
    /**
     * generate UnitObjects and add them to the UnitList
     */
    val Mole = unit("mole", "mol")

    override val baseUnit = setOf(Mole)
}