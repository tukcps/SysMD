package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.*
import io.github.tukcps.aadd.values.real.ia.RealRange
import java.math.BigDecimal

data object Mass : BaseQuantityKind("Mass")
{
    val Kilogram = unit("kilogram", "kg")
    val Gram = unit("gram", "g", RealRange.pow10(-3))
    val Tonne = unit("tonne", "t", 1000)

    val Carat = unit("carat", "ct", "0.0002")

    // per NIST SP 811 and ISO 80000-3
    val KG_PER_POUND = BigDecimal("0.45359237")
    val KG_PER_SHORT_TON = KG_PER_POUND * 2000
    val KG_PER_OUNCE = KG_PER_POUND / 16
    val KG_PER_GRAIN = KG_PER_POUND / 7000

    val ShortTon = unit("short ton", "tn", KG_PER_SHORT_TON)
    val Pound = unit("pound", "lb", KG_PER_POUND)
    val Ounce = unit("ounce", "oz", KG_PER_OUNCE)
    val Grain = unit("grain", "gr", KG_PER_GRAIN)

    override val baseUnit = setOf(Kilogram)
}
