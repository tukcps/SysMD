package com.github.tukcps.sysmd.quantities

import io.github.tukcps.aadd.util.toDoubleExact
import io.github.tukcps.aadd.values.real.ia.RealRange
import java.math.BigDecimal

/** The kind of Quantity (e.g. mass) */
sealed class Kind(val domain : String)

abstract class BaseQuantityKind(domain : String) : Kind(domain)
{
    abstract val baseUnit : Set<BaseUnit>
}

abstract class DerivedQuantityKind(
    domain : String, val siUnitSet : Set<BaseUnit>
) : Kind(domain)
{
    /** Convenience constructor  */
    constructor(domain : String, unit : Unit) : this(domain, unit.siUnitSet())
}
