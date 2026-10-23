package com.github.tukcps.sysmd.quantities

import io.github.tukcps.aadd.util.toDoubleExact
import io.github.tukcps.aadd.values.real.ia.RealRange
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private val infinitePrecision = MathContext(0, RoundingMode.UNNECESSARY)

/** Convenience constructor for use in kind objects */
internal fun<K : Kind> K.unit(name : String, symbol : String, convFac : RealRange) : UnitOfKind<K> = UnitOfKind(this,
    name = name, symbol = symbol, convFac = convFac
)

internal fun<K : Kind> K.unit(name : String, symbol : String, convFac : BigDecimal) : UnitOfKind<K> =
    unit(name, symbol, convFac.toDoubleExact())

internal fun<K : Kind> K.unit(name : String, symbol : String, convFac : Int = 1) : UnitOfKind<K> =
    unit(name, symbol, RealRange(convFac.toDouble()))

internal fun<K : Kind> K.unit(name : String, symbol : String, convFac : String) : UnitOfKind<K> =
    unit(name, symbol, convFac.toDoubleExact())

fun Kind.exactDecimal(str : String) = BigDecimal(str, infinitePrecision)

operator fun BigDecimal.times(n : Int) : BigDecimal = multiply(BigDecimal(n), infinitePrecision)
operator fun BigDecimal.div(n : Int) : BigDecimal = divide(BigDecimal(n), infinitePrecision)
