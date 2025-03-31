package com.github.tukcps.sysmd.quantities

import io.github.tukcps.aadd.AADD
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round

/**
 * Class with functionality to create nice representations of values.
 */
class Representer(
    private var precision: Int = 5,
    private var infinityString:String = "*",
    private var naNString: String = "NaN",
    private var illegalValue: String = "∅"
) {
    enum class InputType {
        NaN, InfinityIncluded, Illegal, NormalNumbers, CloseRange
    }

    /**
     * given a min and max value, representing them in an interval [min,max]
     */
    var min: Double = 0.0
    var max: Double = 0.0
    private var inputType = InputType.NormalNumbers


    /**
     * Determine the type of the value
     */
    private fun findInputType() {
        inputType = if (max.isNaN() || min.isNaN())
            InputType.NaN
        else if (max == Double.POSITIVE_INFINITY || min == Double.NEGATIVE_INFINITY)
            InputType.InfinityIncluded
        else if (max < min)
            InputType.Illegal   //cases for same, close positive and close negative  values
        else if (min == max || max < min * (1 + 10.0.pow(-precision.toDouble())) || min > max * (1 + 10.0.pow(-precision.toDouble())) ||
            0.0==round(min*10.0.pow(precision)) && 0.0==round(max*10.0.pow(precision))) //special case for 0
            InputType.CloseRange
        else
            InputType.NormalNumbers
    }

    /**
     * Do representation of the value
     * @param value AADD representation of a value
     */
    fun represent(value: AADD): String {
        this.min = value.getRange().min
        this.max = value.getRange().max
        findInputType()
        return when (this.inputType) {
            InputType.NaN -> naNString
            InputType.InfinityIncluded -> when {
                min == Double.NEGATIVE_INFINITY && max == Double.POSITIVE_INFINITY -> "$infinityString..$infinityString"
                min == Double.NEGATIVE_INFINITY -> "$infinityString.." + toEngineeringNotation(max)
                else -> toEngineeringNotation(min) + "..$infinityString"
            }
            InputType.CloseRange -> toEngineeringNotation(min)
            InputType.Illegal -> illegalValue
            InputType.NormalNumbers -> if (abs(max / min) > 10.0.pow(precision + 1)) "0.." + toEngineeringNotation(max)
                                       else toEngineeringNotation(min) + ".." + toEngineeringNotation(max)
        }
    }

    /**
     * Returns the InputType of an AADD
     * @param value AADD value
     */
    fun returnInputType(value: AADD): InputType {
        this.min = value.getRange().min
        this.max = value.getRange().max
        findInputType()
        return when (this.inputType) {
            InputType.NaN -> InputType.NaN
            InputType.InfinityIncluded -> InputType.InfinityIncluded
            InputType.CloseRange -> InputType.CloseRange
            InputType.Illegal -> InputType.Illegal
            InputType.NormalNumbers -> InputType.NormalNumbers
        }
    }

    /**
     * Find position of the dot in a string
     * @param plainString string which is used
     */
    private fun findDot(plainString: String): Int {
        var decimalPos = plainString.indexOf(".")
        if (decimalPos == -1) {
            decimalPos = plainString.length
        }
        return decimalPos
    }

    private fun findSigFigRange(plainString: String): Int {
        val found = "[1-9]".toRegex().find(plainString)
        var sigFigPos = 0
        if (found != null) {
            sigFigPos = found.range.first
        }
        return sigFigPos
    }

    /**
     * Find best scale
     * @param s BigDecimal plainString
     */

    private fun findScale(s: String): Array<Int> {
        val decimalPos = findDot(s)
        val sigFigPos = findSigFigRange(s)
        var scale = decimalPos - sigFigPos - 1
        if (scale < 0) {
            scale++
        }
        return arrayOf(decimalPos, sigFigPos, scale)
    }


    /**
     * Transforms the value to the needed representation
     */
    private fun toEngineeringNotation(num: Double): String {
        if (precision <= 0) {
            return "0"
        }
        val plainString1 = BigDecimal(num).toPlainString()

        //Find Scale
        val scales1 = findScale(plainString1) //[DecimalPosition,FirstSigFigure,Scale]
        if (scales1[2] !in -3..5) {
            if (scales1[2] < -200) { // value is close to zero
                return "0"
            }
            //calculate precision for rounding
            var reminder = Math.floorMod(scales1[2], 3)
            var precision = reminder + 1 + precision
            val plainString2 = BigDecimal(num).round(MathContext(precision, RoundingMode.HALF_EVEN)).toPlainString()
            //multiples of 3 only
            val scales2 = findScale(plainString2)
            reminder = Math.floorMod(scales2[2], 3)
            var dotPosition = 1
            scales2[2] = scales2[2] - reminder
            dotPosition += reminder
            precision += reminder + 1

            var sigValues = plainString2.subSequence(
                scales2[1], min(plainString2.length, precision + scales2[1] + 1)
            ).replace("\\.".toRegex(), "")
            sigValues = sigValues.substring(0, dotPosition) + "." + sigValues.substring(dotPosition, sigValues.length)
            
            var endResult: String = sigValues
            endResult = endResult + "e" + scales2[2]
            endResult = endResult.replace("0+e".toRegex(), "e")
            endResult = endResult.replace(".e", "e")
            endResult = endResult.replace("e0$".toRegex(), "")
            endResult = endResult.replace("\\.$".toRegex(), "")
            return endResult
        }
        return BigDecimal(num).setScale(precision, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
    }
}
