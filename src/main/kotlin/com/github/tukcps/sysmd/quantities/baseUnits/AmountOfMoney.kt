package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.BaseQuantityKind
import com.github.tukcps.sysmd.quantities.unit

data object AmountOfMoney : BaseQuantityKind("AmountOfMoney")
{
    val Euro = unit("euro", "EUR")
    val Dollar = unit("USD", "USD")
    val Pound = unit("pound", "GBP")

    // this will ensure no two currencies are implicitly converted to another
    override val baseUnit = setOf(Euro, Dollar, Pound)
}