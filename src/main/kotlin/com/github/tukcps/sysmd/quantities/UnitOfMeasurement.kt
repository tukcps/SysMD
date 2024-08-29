package com.github.tukcps.sysmd.quantities


/**
 * Describes a UnitOfMeasurement, which is a DerivedUnit or a BaseUnit. This class contains all important properties about these Units.
 * @param name Name of the UnitOfMeasurement
 * @param symbol Short symbol of the UnitOfMeasurement
 * @param prefix Prefix of the UnitOfMeasurement (NoPrefix if there is none)
 * @param dimension Dimension string of the UnitOfMeasurement
 * @param convFac Conversion to BaseUnit (Base Value = confFac * Value of current Unit)
 * @param exponent Exponent of the UnitOfMeasurement
 * @param isLogarithmic This value is true, if the value of the current UnitOfMeasurement uses a logarithmic representation (like Decibel)
 * @param isDifference This value is true, if the current UnitOfMeasurement represents a difference between two Units of the same type
 */
open class UnitOfMeasurement(
    val name: String,
    val symbol: String,
    var prefix: Prefix,
    var dimension: String,
    var convFac: Double,
    var exponent: Int,
    var isLogarithmic: Boolean = false,
    var isDifference: Boolean = false,
) : Cloneable {

    public override fun clone(): UnitOfMeasurement {
        return UnitOfMeasurement(
            name,
            symbol,
            prefix.clone(),
            dimension,
            convFac,
            exponent,
            isLogarithmic,
            isDifference
        )
    }

    /**
     * Negates all exponents in the SI Set if the Unit is detected in the Denominator
     */
    fun negateExponent(): UnitOfMeasurement {
        exponent *= -1
        return this
    }

    /**
     * Get Base Unit Set of the current Unit
     * @return Base Unit Set
     */
    open fun getBaseUnits(): Set<BaseUnit> {
        return when (this) {
            is DerivedUnit -> baseUnitSet
            is BaseUnit -> getBaseUnits() //calls function in BaseUnit Class
            else -> throw UnknownUnitError("$this is an invalid unit type")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnitOfMeasurement) return false
        if (name != other.name) return false
        if (symbol != other.symbol) return false
        if (prefix != other.prefix) return false
        if (dimension != other.dimension) return false
        if (convFac != other.convFac) return false
        if (exponent != other.exponent) return false
        return isLogarithmic == other.isLogarithmic
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + symbol.hashCode()
        result = 31 * result + prefix.hashCode()
        result = 31 * result + dimension.hashCode()
        result = 31 * result + convFac.hashCode()
        result = 31 * result + exponent
        result = 31 * result + isLogarithmic.hashCode()
        return result
    }
}