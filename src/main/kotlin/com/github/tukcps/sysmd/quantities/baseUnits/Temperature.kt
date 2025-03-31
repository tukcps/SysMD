package com.github.tukcps.sysmd.quantities.baseUnits

import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.functions.div
import io.github.tukcps.aadd.functions.minus
import io.github.tukcps.aadd.functions.plus
import io.github.tukcps.aadd.functions.times
import com.github.tukcps.sysmd.quantities.BaseUnit
import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Prefix


open class Temperature(name: String, symbol: String, prefix: Prefix, convFac: Double = 1.0, exponent: Int = 1) :
    BaseUnit(name, symbol, prefix, "Temperature", convFac, exponent) {

    /**
     * generate UnitObjects and add them to the UnitList
     */
    object Kelvin : Temperature("kelvin", "K", NoPrefix)
    object Celsius : Temperature("degree celsius", "°C", NoPrefix)
    object Fahrenheit : Temperature("degree fahrenheit", "°F", NoPrefix)

    override fun copy(exponentValue: Int): Temperature {
        return Temperature(name, symbol, prefix, convFac, exponentValue)
    }

    fun convertTo(inputValue: DD<*>, destinationTemperature: Temperature): DD<*> {
        return when (destinationTemperature.name) {
            "degree celsius" -> {
                (inputValue - 273.15) / destinationTemperature.prefix.factor
            }
            "degree fahrenheit" -> {
                ((inputValue - 273.15) * 9.0 / 5.0 + 32.0) / destinationTemperature.prefix.factor
            }
            else -> {
                inputValue
            }
        }
    }

    fun toKelvin(value: DD<*>): DD<*> {
        if (name == "degree fahrenheit")
            return (value - 32.0) * 5.0 / 9.0 + 273.15
        if (name == "degree celsius")
            return value + 273.15
        //is already in Kelvin
        return value
    }
}
