package com.github.tukcps.sysmd.quantities

/**
 * Describes a DerivedUnit, which is a composition of different BaseUnits with possibly different exponent values.
 * This class contains all important properties about these Units.
 * @param name Name of the DerivedUnit
 * @param symbol Short symbol of the DerivedUnit
 * @param prefix Prefix of the DerivedUnit (NoPrefix if there is none)
 * @param dimension Dimension-string of the DerivedUnit
 * @param baseUnitSet Set of the Base Units of the current DerivedUnit
 * @param convFac Conversion to BaseUnit (Base Value = confFac * Value of current DerivedUnit)
 * @param exponent Exponent of the DerivedUnit
 * @param isLogarithmic This value is true, if the value of the current DerivedUnit uses a logarithmic representation (like Decibel)
 * @param isDifference This value is true, if the current DerivedUnit represents a difference between two Units of the same type
 */
open class DerivedUnit(
    name: String,
    symbol: String,
    prefix: Prefix,
    dimension: String,
    var baseUnitSet: Set<BaseUnit>,
    convFac: Double,
    exponent: Int = 1,
    isLogarithmic: Boolean = false,
    isDifference: Boolean = false
) : UnitOfMeasurement(name, symbol, prefix, dimension, convFac, exponent, isLogarithmic, isDifference), Cloneable {
    open fun copy(): DerivedUnit {
        val unitSet = mutableSetOf<BaseUnit>()
        baseUnitSet.forEach {
            unitSet.add(it.copy())
        }
        return DerivedUnit(name, symbol, prefix, dimension, unitSet, convFac, exponent, isLogarithmic, isDifference)
    }

    override fun clone(): UnitOfMeasurement {
        return copy()
    }
}
