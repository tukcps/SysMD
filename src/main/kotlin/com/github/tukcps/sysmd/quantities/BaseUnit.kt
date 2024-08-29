package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.quantities.baseUnits.*

/**
 * Describes a BaseUnit, which is a collection of the 7 SI Units and the InformationCapacity.
 * This class contains all important properties about these Units.
 * @param name Name of the BaseUnit
 * @param symbol Short symbol of the BaseUnit
 * @param prefix Prefix of the BaseUnit (NoPrefix if there is none)
 * @param dimension Dimension-string of the BaseUnit
 * @param convFac Conversion to BaseUnit (Base Value = confFac * Value of current BaseUnit)
 * @param exponent Exponent of the BaseUnit
 * @param isDifference This value is true, if the current BaseUnit represents a difference between two Units of the same type
 */
open class BaseUnit(
    name: String,
    symbol: String,
    prefix: Prefix,
    dimension: String,
    convFac: Double = 1.0,
    exponent: Int = 1,
    isDifference: Boolean = false
) :
    UnitOfMeasurement(name, symbol, prefix, dimension, convFac, exponent, isDifference = isDifference), Cloneable {

    override fun clone(): BaseUnit = copy()

    open fun copy(exponentValue: Int = exponent): BaseUnit {
        return BaseUnit(name, symbol, prefix, dimension, convFac, exponentValue, isDifference = isDifference)
    }

    /**
     * Get Base Unit Set of the current Unit
     * @return Base Unit Set
     */
    override fun getBaseUnits(): Set<BaseUnit> {
        return when (this) {
            is AmountOfSubstance -> setOf(AmountOfSubstance.Mole.copy())
            is ElectricCurrent -> setOf(ElectricCurrent.Ampere.copy())
            is InformationCapacity -> setOf(InformationCapacity.Bit.copy())
            is Length -> setOf(Length.Meter.copy())
            is LuminousIntensity -> setOf(LuminousIntensity.Candela.copy())
            is Mass -> setOf(Mass.Kilogram.copy())
            is Temperature -> setOf(Temperature.Kelvin.copy())
            is Time -> setOf(Time.Second.copy())
            is EmptyUnit -> setOf(EmptyUnit.Empty.copy())
            is Currency -> setOf(Currency.Euro.copy())
            else -> throw UnknownUnitError("$this should be a base unit, but it is not defined")
        }
    }
}


