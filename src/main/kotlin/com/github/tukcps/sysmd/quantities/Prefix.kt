package com.github.tukcps.sysmd.quantities

import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.pow

/**
 * Prefix of a Unit.
 */
data class Prefix(
    val name: String,
    val symbol: String,
    val factorBase: Int,
    val factorExponent : Int
) : Cloneable {
    public override fun clone() = copy()

    // integer to double loses no bits
    val factor = pow(RealRange(factorBase.toDouble()), factorExponent.toDouble())
}