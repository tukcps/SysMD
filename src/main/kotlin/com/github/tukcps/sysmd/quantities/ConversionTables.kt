package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.quantities.baseUnits.*
import com.github.tukcps.sysmd.quantities.derivedUnits.*

object ConversionTables {

    val prefixes = hashMapOf(
        "Y" to Yotta,
        "Yi" to Yobi,
        "Z" to Zetta,
        "Zi" to Zebi,
        "E" to Exa,
        "Ei" to Exbi,
        "P" to Peta,
        "Pi" to Pebi,
        "T" to Tera,
        "Ti" to Tebi,
        "G" to Giga,
        "Gi" to Gibi,
        "M" to Mega,
        "Mi" to Mebi,
        "k" to Kilo,
        "K" to Kilo,
        "ki" to Kibi,
        "Ki" to Kibi,
        "h" to Hecto,
        "da" to Deka,
        "d" to Deci,
        "c" to Centi,
        "m" to Milli,
        "μ" to Micro,
        "n" to Nano,
        "p" to Pico,
        "f" to Femto,
        "a" to Atto,
        "z" to Zepto,
        "y" to Yocto,
        "" to NoPrefix
    )

    val unitsMap = mutableMapOf(
        // Base Units
        "m" to Length.Meter,
        "kg" to Mass.Kilogram,
        "s" to Time.Second,
        "A" to ElectricCurrent.Ampere,
        //"K"     to Kelvin,
        "mol" to AmountOfSubstance.Mole,
        "cd" to LuminousIntensity.Candela,
        "bit" to InformationCapacity.Bit,
        "Bit" to InformationCapacity.Bit,
        "K" to Temperature.Kelvin,
        "?" to EmptyUnit.Empty,
        "€" to Currency.Euro,

        // Units in use with SI System
        "l" to Volume.Liter,
        "pt" to Volume.Pint,
        "qt" to Volume.Quart,
        "gal" to Volume.Gallon,
        "bbl" to Volume.Barrel,
        "min" to Time.Minute,
        "h" to Time.Hour,
        "d" to Time.Day,
        "a" to Time.Year,
        "y" to Time.Year,
        "yr" to Time.Year,
        "inch" to Length.Inch,
        "ft" to Length.Feet,
        "yd" to Length.Yard,
        "mi" to Length.Mile,
        "nmi" to Length.NauticalMile,
        "lb" to Mass.Pound,
        "oz" to Mass.Ounce,
        "ct" to Mass.Carat,
        "gr" to Mass.Grain,
        "t" to Mass.Tonne,
        "tn" to Mass.ShortTon,
        "HP" to Power.Horsepower,
        "B" to InformationCapacity.Byte,
        "Byte" to InformationCapacity.Byte,
        "°C" to Temperature.Celsius,
        "°F" to Temperature.Fahrenheit,
        "ac" to Area.Acre,
        "ha" to Area.Hectare,
        "mph" to Speed.MilesPerHour,
        "kt" to Speed.Knot,

        //Derived units of SI System
        "sr" to SolidAngle.Steradian,
        "Hz" to Frequency.Hertz,
        "N" to Force.Newton,
        "Pa" to Pressure.Pascal,
        "bar" to Pressure.Bar,
        "psi" to Pressure.PoundPerSquareInch,
        "J" to Energy.Joule,
        "cal" to Energy.Calorie,
        "W" to Power.Watt,
        "C" to ElectricCharge.Coulomb,
        "Ah" to ElectricCharge.AmpereHours,
        "As" to ElectricCharge.AmpereSecond,
        "V" to ElectricPotential.Volt,
        "F" to Capacitance.Farad,
        "Ω" to ElectricalResistance.Ohm,
        "Ohm" to ElectricalResistance.Ohm,
        "S" to ElectricalConductance.Siemens,
        "Wb" to MagneticFlux.Weber,
        "T" to MagneticFluxDensity.Tesla,
        "H" to Inductance.Henry,
        "lm" to LuminousFlux.Lumen,
        "lx" to Illuminance.Lux,
        "Bq" to Activity.Becquerel,
        "Gy" to AbsorbedDose.Gray,
        "Sv" to DoseEquivalent.Sievert,
        "kat" to CatalyticActivity.Katal,
        "dB" to QuantityOfDimensionOne.Decibel,
        "St" to KinematicViscosity.Stokes,
        "sb" to Luminance.Stilb,
        "%" to QuantityOfDimensionOne.Percent,
        "1" to QuantityOfDimensionOne.One,
        "°" to QuantityOfDimensionOne.Degree,
        "deg" to QuantityOfDimensionOne.Degree,
        "rad" to QuantityOfDimensionOne.One,
        "Pi" to QuantityOfDimensionOne.Radiant,
        "pi" to QuantityOfDimensionOne.Radiant,
        "π" to QuantityOfDimensionOne.Radiant,
        "g" to Mass.Gram,
        "DateTime" to Timestamp.UnixTimeStamp,
        "Year" to Timestamp.UnixTimeStamp,
        "Date" to Timestamp.UnixTimeStamp,
        "Month" to Timestamp.UnixTimeStamp,

        //Combined Units
        "m/s^2" to Acceleration.MeterPerSecondSquared,
        "m^2" to Area.SquareMeters,
        "m^3" to Volume.CubicMeter,
        "kg/m^3" to Density.KilogramPerCubicMeter,
        "N/C" to ElectricField.NewtonPerCoulomb,
        "J/m^2" to EnergyDensity.JoulePerCubicMeter,
        "J/K" to Entropy.JoulePerKelvin,
        "lm/W" to LuminousEfficacy.LumenPerWatt,
        "lm s" to LuminousEnergy.LumenSecond,
        "g/s" to MassFlow.KilogramPerSecond,
        "Nm" to MomentOfForce.NewtonMeter,
        "Ws" to Energy.WattSecond,
        "Wh" to Energy.WattHour,
        "kg m^2" to MomentOfInertia.KilogramSquareMeter,
        "kg m/s" to Momentum.KilogramMeterPerSecond,
        "F/s" to Permittivity.FaradPerSecond,
        "W/m^2" to PowerDensity.WattPerSquremeter,
        "m/s" to Speed.MeterPerSeconds,
    )

    var dimensions = hashMapOf(
        "radius" to Length.Meter,

        )
}

/**
 * Adds a new Unit to the ConversionTable
 */
fun addUnit(name: String, symbol: String, baseUnit: String, dimension: String, convFac: Double, isLogarithmic: Boolean = false) {
    //Generate unit and convert it to Base Units. After that get the BaseUnits
    val newUnit = Unit(baseUnit)
    val unitSet=newUnit.toSI().unitSet
    //Add Base units to a set of BaseUnits
    val newBaseUnitSet = mutableSetOf<BaseUnit>()
    unitSet.forEach { newBaseUnitSet.add(it as BaseUnit) } // can be done, because newUnit is in SI
    // Generate unitOfMeasurement and add it to ConversionTables unitsMap
    val unitOfMeasurement = DerivedUnit(name, symbol, NoPrefix, dimension, newBaseUnitSet, convFac, isLogarithmic = isLogarithmic)
    if(symbol !in ConversionTables.unitsMap.keys) //only add if symbol not in conversion table
        ConversionTables.unitsMap[symbol] = unitOfMeasurement
}