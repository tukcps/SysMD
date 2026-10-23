package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.quantities.baseUnits.Duration
import com.github.tukcps.sysmd.quantities.baseUnits.ThermodynamicTemperature
import io.github.tukcps.aadd.util.toDoubleExact
import io.github.tukcps.aadd.values.real.ia.RealRange

/**
 * Describes a UnitOfMeasurement, which is a DerivedUnit or a BaseUnit. This class contains all important properties about these Units.
 * @param kind Corresponding quantity kind. Also fixes domain.
 * @param name Name of the UnitOfMeasurement
 * @param symbol Short symbol of the UnitOfMeasurement
 * @param prefix Prefix of the UnitOfMeasurement (NoPrefix if there is none)
 * @param convFac Conversion to BaseUnit (Base Value = confFac * Value of current Unit)
 * @param exponent Exponent of the UnitOfMeasurement
 * @param isLogarithmic This value is true, if the value of the current UnitOfMeasurement uses a logarithmic representation (like Decibel)
 * @param isDifference This value is true, if the current UnitOfMeasurement represents a difference between two Units of the same type
 */
data class UnitOfKind<out K : Kind>(
    val kind : K,
    val name: String,
    val symbol: String,
    val prefix: Prefix = NoPrefix,
    val convFac: RealRange = RealRange(1.0),
    val exponent: Int = 1,
    val isLogarithmic: Boolean = false,
    val isDifference: Boolean = false,
    val alternativeDomain: String = "",
) : Cloneable {

    val domain get() = kind.domain

    override fun clone() : UnitOfKind<K> = copy()

    /**
     * Negates all exponents in the SI Set if the Unit is detected in the Denominator
     */
    fun negateExponent(): UnitOfKind<K> = copy(exponent = -exponent)

    val baseUnits : Set<UnitOfKind<BaseQuantityKind>> get() = when(kind) {
        is BaseQuantityKind -> {
            val b = kind.baseUnit

            // some base quantities have multiple base units; we need to pick the one that matches the current unit
            when {
                b.size > 1 -> setOf(b.single { it.name == this.name })
                else -> b
            }
        }
        is DerivedQuantityKind -> kind.siUnitSet
    }

    fun pow(n : Int) = copy(exponent = exponent * n)

    @Suppress("UNCHECKED_CAST")
    inline fun<reified T : Kind> tryCast() : UnitOfKind<T>? = when {
        kind is T -> (this as UnitOfKind<T>)
        else -> null
    }

    val hasOffset get() = kind is ThermodynamicTemperature && name in listOf(kind.Fahrenheit.name, kind.Celsius.name)
}

typealias UnitOfMeasurement = UnitOfKind<*>
typealias BaseUnit = UnitOfKind<BaseQuantityKind>
typealias DerivedUnit = UnitOfKind<DerivedQuantityKind>
typealias UnitOfTemperature = UnitOfKind<ThermodynamicTemperature>
