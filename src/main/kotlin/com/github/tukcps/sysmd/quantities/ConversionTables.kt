package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.quantities.baseUnits.AmountOfMoney
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
        "μ" to Micro, // greek small letter mu U+03BC
        "µ" to Micro, // micro sign U+00B5
        "n" to Nano,
        "p" to Pico,
        "f" to Femto,
        "a" to Atto,
        "z" to Zepto,
        "y" to Yocto,
        "" to NoPrefix
    )

    private val _unitsMap = mutableMapOf(
        // Base Units
        "m" to Length.Meter,
        "kg" to Mass.Kilogram,
        "s" to Duration.Second,
        "A" to ElectricCurrent.Ampere,
        //"K"     to Kelvin,
        "mol" to AmountOfSubstance.Mole,
        "cd" to LuminousIntensity.Candela,
        "bit" to StorageCapacity.Bit,
        "Bit" to StorageCapacity.Bit,
        "K" to ThermodynamicTemperature.Kelvin,
        "?" to EmptyUnit.Empty,
        "EUR" to AmountOfMoney.Euro,
        "USD" to AmountOfMoney.Dollar,
        "GBP" to AmountOfMoney.Pound,

        // Units in use with SI System
        "l" to Volume.Liter,
        "pt" to Volume.Pint,
        "qt" to Volume.Quart,
        "gal" to Volume.Gallon,
        "bbl" to Volume.Barrel,
        "min" to Duration.Minute,
        "h" to Duration.Hour,
        "d" to Duration.Day,
        "a" to Duration.Year,
        "y" to Duration.Year,
        "yr" to Duration.Year,
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
        "PS" to Power.PferdeStaerke,
        "B" to StorageCapacity.Byte,
        "Byte" to StorageCapacity.Byte,
        "°C" to ThermodynamicTemperature.Celsius,
        "°F" to ThermodynamicTemperature.Fahrenheit,
        "ac" to Area.Acre,
        "ha" to Area.Hectare,
        "mph" to Speed.MilesPerHour,
        "kt" to Speed.Knot,

        //Derived units of SI System
        "sr" to SolidAngularMeasure.Steradian,
        "Hz" to Frequency.Hertz,
        "FLOPS" to Frequency.FLOPS,
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
        "V" to ElectricPotentialDifferenceValue.Volt,
        "F" to Capacitance.Farad,
        "Ω" to Resistance.Ohm, // actual dedicated ohm symbol
        "Ω" to Resistance.Ohm, // uppercase greek letter
        "Ohm" to Resistance.Ohm,
        "S" to Conductance.Siemens,
        "Wb" to MagneticFlux.Weber,
        "T" to MagneticFluxDensity.Tesla,
        "H" to Inductance.Henry,
        "lm" to LuminousFlux.Lumen,
        "lx" to Illuminance.Lux,
        "Bq" to NuclearActivity.Becquerel,
        "Gy" to AbsorbedDose.Gray,
        "Sv" to DoseEquivalent.Sievert,
        "dB" to DimensionOne.Decibel,
        "FLOPs" to DimensionOne.FLOPs,
        "St" to KinematicViscosity.Stokes,
        "sb" to Luminance.Stilb,
        "%" to DimensionOne.Percent,
        "1" to DimensionOne.One,
        "°" to DimensionOne.Degree,
        "deg" to DimensionOne.Degree,
        "rad" to DimensionOne.One,
        "Pi" to DimensionOne.Pi,
        "pi" to DimensionOne.Pi,
        "π" to DimensionOne.Pi,
        "g" to Mass.Gram,
        "DateTime" to Timestamp.UnixTimeStamp,
        "Year" to Timestamp.UnixTimeStamp,
        "Date" to Timestamp.UnixTimeStamp,
        "Month" to Timestamp.UnixTimeStamp,

        //Combined Units
        "m/s^2" to Acceleration.MeterPerSecondSquared,
        "m^2" to Area.SquareMeters,
        "m^3" to Volume.CubicMeter,
        "kg/m^3" to MassDensity.KilogramPerCubicMeter,
        "N/C" to ElectricFieldStrength.NewtonPerCoulomb,
        "J/m^3" to EnergyDensity.JoulePerCubicMeter,
        "J/K" to Entropy.JoulePerKelvin,
        "lm/W" to LuminousEfficacy.LumenPerWatt,
        "lm s" to LuminousEnergy.LumenSecond,
        "kg/s" to MassFlow.KilogramPerSecond,
        "Nm" to MomentOfForce.NewtonMeter,
        "Ws" to Energy.WattSecond,
        "Wh" to Energy.WattHour,
        "bps" to BitRate.BitsPerSecond,
        "Bps" to BitRate.BytesPerSecond,
        "kg m^2" to MomentOfInertia.KilogramSquareMeter,
        "kg m/s" to Momentum.KilogramMeterPerSecond,
        "F/m" to Permittivity.FaradPerMeter,
        "m/s" to Speed.MeterPerSeconds,
    )

    val unitsMap : Map<String, UnitOfMeasurement> = _unitsMap

    fun addUnit(unit : DerivedUnit)
    {
        _unitsMap.putIfAbsent(unit.symbol, unit)
    }
}
