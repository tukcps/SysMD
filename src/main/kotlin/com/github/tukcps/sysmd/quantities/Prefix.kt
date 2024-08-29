package com.github.tukcps.sysmd.quantities

/**
 * Prefix of a Unit.
 */
open class Prefix : Cloneable {

    val name: String
    val symbol: String
    val factor: Double


    constructor(name: String, symbol: String, factor: Double) {
        this.name = name
        this.symbol = symbol
        this.factor = factor
    }

    constructor(original: Prefix) {
        name = original.name
        symbol = original.symbol
        factor = original.factor
    }

    public override fun clone(): Prefix {
        return Prefix(name, symbol, factor)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Prefix) {
            false
        } else if (other.name != this.name) {
            false
        } else if (other.symbol != this.symbol) {
            false
        } else other.factor == this.factor
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}