package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.quantities.baseUnits.*

/**
 * Describes a BaseUnit, which is a collection of the 7 SI Units and the InformationCapacity.
 * This class contains all important properties about these Units.
 * @param name Name of the BaseUnit
 * @param symbol Short symbol of the BaseUnit
 * @param prefix Prefix of the BaseUnit (NoPrefix if there is none)
 * @param domain Domain-string of the BaseUnit
 * @param convFac Conversion to BaseUnit (Base Value = confFac * Value of current BaseUnit)
 * @param exponent Exponent of the BaseUnit
 * @param isDifference This value is true, if the current BaseUnit represents a difference between two Units of the same type
 * @param alternativeDomain This value is used for alternative domains
 */
open class BaseUnit(
    name: String,
    symbol: String,
    prefix: Prefix,
    domain: String,
    convFac: Double = 1.0,
    exponent: Int = 1,
    isDifference: Boolean = false,
    alternativeDomain: String = ""
) :
    UnitOfMeasurement(name, symbol, prefix, domain, convFac, exponent, isDifference = isDifference, alternativeDomain = alternativeDomain), Cloneable {

    override fun clone(): BaseUnit = this

    open fun copy(exponentValue: Int = exponent): BaseUnit = copyWith(exponent = exponentValue)

    override fun copyWith(prefix: Prefix, exponent: Int): BaseUnit {
        return when (this) {
            is AmountOfMoney -> AmountOfMoney(name, symbol, prefix, convFac, exponent)
            is AmountOfSubstance -> AmountOfSubstance(name, symbol, prefix, convFac, exponent)
            is Duration -> Duration(name, symbol, prefix, convFac, exponent)
            is ElectricCurrent -> ElectricCurrent(name, symbol, prefix, convFac, exponent)
            is EmptyUnit -> EmptyUnit(name, symbol, prefix, convFac, exponent)
            is Length -> Length(name, symbol, prefix, convFac, exponent)
            is LuminousIntensity -> LuminousIntensity(name, symbol, prefix, convFac, exponent)
            is Mass -> Mass(name, symbol, prefix, convFac, exponent)
            is StorageCapacity -> StorageCapacity(name, symbol, prefix, convFac, exponent)
            is ThermodynamicTemperature -> ThermodynamicTemperature(name, symbol, prefix, convFac, exponent)
            else -> BaseUnit(name, symbol, prefix, domain, convFac, exponent, isDifference, alternativeDomain)
        }
    }

    /**
     * Get Base Unit Set of the current Unit
     * @return Base Unit Set
     */
    override fun getBaseUnits(): Set<BaseUnit> {
        return when (this) {
            is AmountOfSubstance -> setOf(AmountOfSubstance.Mole.copy())
            is ElectricCurrent -> setOf(ElectricCurrent.Ampere.copy())
            is StorageCapacity -> setOf(StorageCapacity.Bit.copy())
            is Length -> setOf(Length.Meter.copy())
            is LuminousIntensity -> setOf(LuminousIntensity.Candela.copy())
            is Mass -> setOf(Mass.Kilogram.copy())
            is ThermodynamicTemperature -> setOf(ThermodynamicTemperature.Kelvin.copy())
            is Duration -> setOf(Duration.Second.copy())
            is EmptyUnit -> setOf(EmptyUnit.Empty.copy())
            // Each currency is its own base unit: there is no fixed exchange rate, so USD must not
            // canonicalize to EUR (which silently made 1 USD == 1 EUR == 1 GBP and allowed
            // 100 USD + 100 EUR = 200 EUR). Mixing currencies now fails like any other unit mismatch.
            is AmountOfMoney -> setOf(AmountOfMoney(name, symbol, NoPrefix))
            else -> throw UnknownUnitError("$this should be a base unit, but it is not defined")
        }
    }
}


