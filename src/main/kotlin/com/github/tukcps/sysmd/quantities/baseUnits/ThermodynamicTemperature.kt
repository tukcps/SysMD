package com.github.tukcps.sysmd.quantities.baseUnits

import com.github.tukcps.sysmd.quantities.*
import io.github.tukcps.aadd.DDBuilder.RealMath.div
import io.github.tukcps.aadd.DDBuilder.RealMath.minus
import io.github.tukcps.aadd.DDBuilder.RealMath.plus
import io.github.tukcps.aadd.DDBuilder.RealMath.times
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.util.toDoubleExact
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.div

data object ThermodynamicTemperature : BaseQuantityKind("ThermodynamicTemperature")
{
    val CELSIUS_OFFSET = "273.15".toDoubleExact()

    val Kelvin = unit("kelvin", "K")
    val Celsius = unit("degree celsius", "°C")
    val Fahrenheit = unit(
        "degree fahrenheit", "°F",
        RealRange(5.0) / RealRange(9.0),
    )

    override val baseUnit = setOf(Kelvin)

    /** Converts a kelvin value to a different temperature unit */
    fun fromKelvin(kelvin: AADD, targetUnit: UnitOfTemperature): AADD = when(targetUnit.name) {
        Celsius.name -> (kelvin - CELSIUS_OFFSET) / targetUnit.prefix.factor
        Fahrenheit.name -> ((kelvin - CELSIUS_OFFSET) * 9.0 / 5.0 + 32.0) / targetUnit.prefix.factor
        else -> kelvin
    }

    /** Converts a temperature value to kelvins */
    fun toKelvin(value: AADD, currentUnit : UnitOfTemperature): AADD = when(currentUnit.name) {
        Fahrenheit.name -> (value - 32.0) * 5.0 / 9.0 + CELSIUS_OFFSET
        Celsius.name -> value + CELSIUS_OFFSET
        // assume kelvin for invalid name
        else -> value
    }

    fun convert(value : AADD, from : UnitOfTemperature, to : UnitOfTemperature) : AADD = when {
        from == to -> value
        else -> fromKelvin(toKelvin(value, from), to)
    }
}
