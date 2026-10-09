package com.github.tukcps.sysmd.quantities

import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.bounds.DoubleBound
import io.github.tukcps.aadd.values.bounds.LongBound
import io.github.tukcps.aadd.values.real.ia.RealRange

@Deprecated("Use AADD 0.9.6+")
operator fun DD<*>.contains(x : Long) = when(this) {
    is AADD -> min <= DoubleBound.Finite(x.toDouble()) && max >= DoubleBound.Finite(x.toDouble())
    is IDD ->  min<= x && max >= x
    else -> false
}

val epsilon = 1e-9

val DD<*>.isZero get() = when(this) {
    is AADD -> getRange() in RealRange(-epsilon, epsilon)
    is IDD -> min == LongBound.Finite(0L) && max == LongBound.Finite(0L)
    else -> false
}

val VectorQuantity.isZero get() = values.all { it.isZero }
