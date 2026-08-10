package com.github.tukcps.sysmd.quantities

import io.github.tukcps.aadd.*
import io.github.tukcps.aadd.values.Range

operator fun DD<*>.contains(x : Long) = when(this) {
    is AADD -> min <= x && max >= x
    is IDD -> x in min..max
    else -> false
}

val epsilon = 1e-9

val DD<*>.isZero get() = when(this) {
    is AADD -> getRange() in Range(-epsilon, epsilon)
    is IDD -> min == 0L && max == 0L
    else -> false
}

val VectorQuantity.isZero get() = values.all { it.isZero }
