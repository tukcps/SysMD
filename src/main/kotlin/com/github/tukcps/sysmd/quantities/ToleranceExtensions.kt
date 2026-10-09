package com.github.tukcps.sysmd.quantities

import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.util.Tolerance

fun Tolerance.areEqual(quantity : VectorQuantity, other : VectorQuantity) : Boolean
{
    if (quantity.values.size != other.values.size)
        return false

    val isReal = quantity.values.isNotEmpty() && quantity.values[0] is AADD
    if (isReal && !quantity.unit.hasSameDimension(other.unit))
        return false

    for (i in quantity.values.indices) {
        val l = quantity.values[i]
        val r = other.values[i]
        val same = when (l) {
            is AADD if r is AADD -> areEqual(l.getRange(), r.getRange())
            is BDD if r is BDD -> l.value == r.value
            is IDD if r is IDD -> areEqual(l.getRange(), r.getRange())
            is StrDD if r is StrDD -> l.toString() == r.toString()
            else -> false
        }

        if (!same)
            return false
    }

    return true
}
