package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.baseUnits.ThermodynamicTemperature
import io.github.tukcps.aadd.*
import io.github.tukcps.aadd.functions.*
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.Range
import io.github.tukcps.aadd.values.XBool
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

/**
 * A quantity that consists of a value that is represented by a DD<*> instance, and
 * a unit that is represented by SI units fraction. The unit is transformed to SI, so that
 * calculations are more efficient
 */
class Quantity : VectorQuantity {

    override var value: DD<*>

    constructor(value: Bool) : super(value) {
        this.value = value.clone()
        this.unit = Unit("")
    }

    constructor(value: Integer) : super(value) {
        this.value = value.clone()
        this.unit = Unit("")
    }

    constructor(value: StrDD) : super(value) {
        this.value = value.clone()
        this.unit = Unit("")
    }

    /**
     * Constructor only for Real
     * @param value Value of the Quantity represented as a Real, so that possible errors are considered
     * @param unitString String representation of the Unit
     */
    constructor(value: Real, unitString: String) : super(value, unitString) {
        this.value = value.clone()
        unitSpec = unitString // use unitStr as unitSpec
        this.unit = Unit(unitString)
        makeCanonical()
    }

    /**
     * Constructor
     * @param value Value of the Quantity represented as a DD<*>, so that possible errors are considered
     * @param unitObject Unit, which should be added to the new Quantity
     * @param unitSpec The wanted representation of the Unit, toString converts the Unit to this representation
     */
    constructor(value: DD<*>, unitObject: Unit, unitSpec: String = "") : super(value, unitObject, unitSpec) {
        this.value = value.clone()
        this.unit = unitObject.clone()
        this.unitSpec = unitSpec
        makeCanonical()
    }

    /** Makes a perfect clone of a unit with new references of all objects */
    override fun clone(): Quantity = Quantity(value.clone(), unit.clone(), unitSpec.plus(""))

    /**
     *  Transforms the Unit to a canonical SI representation with the right UnitDomain
     */
    private fun makeCanonical() {
        toSI()
        unit.reduceRedundantUnits()
        unit.calculateUnitDomain(unitSpec)
        unit.calculateUnitSymbol(unitSpec)
    }


    //--------------Arithmetic operations--------------------------------

    /**
     * Multiplies quantities
     * @param quantity is multiplied to the current Quantity
     * @return Quantity with resulting Real/Integer value and Unit as a new Quantity
     */
    operator fun times(quantity: Quantity): Quantity {
        if (value is Bool) throw BDDError("Multiplication not allowed for BDDs")
        var resultUnit = Unit()
        val quantity1 = this.clone()
        val quantity2 = quantity.clone()
        if (quantity1.unit.toString() == "?" || quantity2.unit.toString() == "?")
            resultUnit = Unit("?")
        else {
            // add all units of quantity1 and quantity2 to the unitSet of resultUnit
            quantity1.unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
            quantity2.unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
        }
        return Quantity(quantity1.value * quantity2.value, resultUnit.clone())
    }

    /**
     * Divides quantities
     * @param quantity is the divisor of the current Quantity
     * @return Quantity with resulting Real/Integer value and Unit as a new Quantity
     */
    operator fun div(quantity: Quantity): Quantity {
        if (value is Bool) throw BDDError("Division not allowed for BDDs")
        var resultUnit = Unit()
        val quantity1 = this.clone()
        val quantity2 = quantity.clone()
        if (quantity1.unit.toString() == "?" || quantity2.unit.toString() == "?")
            resultUnit = Unit("?")
        else {
            // negate all exponents of units in Quantity2 because of division
            quantity2.unit.negateExponentsOfUnits()
            // add all units of quantity1 to the unitSet of quantity2
            quantity1.unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
            quantity2.unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
        }
        return Quantity(quantity1.value / quantity2.value, resultUnit.clone())
    }


    /**
     * Adds quantities.
     * @param quantity is added to the current Quantity
     * @return Quantity with resulting Real/Integer value and Unit as a new Quantity
     */
    operator fun plus(quantity: Quantity): Quantity {
        if (value is Bool) throw BDDError("Addition not allowed for BDDs")

        return if (unit == quantity.unit || quantity.unit.toString() == "?")
            Quantity(quantity.value + value, unit, unitSpec)
        else if (unit.toString() == "?")
            Quantity(quantity.value + value, quantity.unit, quantity.unitSpec)
        else {
            // Possibility to add 0 to a Quantity with a unit.
            // Error range for 0, like for equals.
            // Ugly, but we need to assume even small numbers as 0 s ...
            if (Range(-0.0001, 0.0001).contains(getRange())) quantity.clone()
            else if (Range(-0.0001, 0.0001).contains(quantity.getRange())) this.clone()
            else {
                if (!value.asAadd().getRange().isFinite())
                    return quantity
                if (!quantity.value.asAadd().getRange().isFinite())
                    return quantity
                throw AdditionError("${this.unit} and ${quantity.unit}")
            }
        }
    }

    /**
     * Subtracts quantities.
     * @param quantity is the subtrahend of the current Quantity
     * @return Quantity with resulting Real/Integer value and Unit as a new Quantity.
     */
    operator fun minus(quantity: Quantity): Quantity {
        if (value is Bool) throw SemanticError("Subtraction not allowed on BDDs")
        // Set isDifference of resultUnit to true
        val resultingUnit = if (unit == quantity.unit || quantity.unit.toString() == "?")
            unit.clone()
        else if (unit.toString() == "?")
            quantity.unit.clone()
        else
            throw SubtractionError("${this.unit} and ${quantity.unit}")
        resultingUnit.isDifference = true
        return Quantity(value - quantity.value, resultingUnit, unitSpec)
    }

    /**
     * Negates sign of Real or Integer-Valued Variable
     */
    override fun negate(): Quantity {
        return when (this.value) {
            is Real -> Quantity((value as Real).negate(), unit, unitSpec)
            is Integer -> Quantity((value as Integer).negate())
            else -> throw SemanticError("unary minus only applicable on values of type Real or Integer")
        }
    }

    /**
     * Infix fun for power
     * @param quantity Exponent for the Pow function
     * @return result of the calculation
     */
    infix fun pow(quantity: Quantity): Quantity {
        if (value is Bool) throw BDDError("Pow not allowed for BDDs")
        val quantity1 = this.clone()
        val quantity2 = quantity.clone()
        return quantity1.pow(quantity2.value)
    }

//--------------Compare operations--------------------------------

    /**
     * Compares quantities with "greater than"
     * @return Quantity with the result as Bool as a new Quantity
     */
    infix fun gt(quantity: Quantity): Quantity {
        if (value is Bool) throw BDDError("Greater than not allowed for BDDs")
        return Quantity(this.value greaterThan quantity.value)
    }

    /**
     * Compares quantities with "less than"
     * @return Quantity with the result as Bool as a new Quantity
     */
    infix fun lt(quantity: Quantity): Quantity {
        if (value is Bool) throw BDDError("Less than not allowed for BDDs")
        return Quantity(this.value lessThan quantity.value)
    }

    /**
     * Compares quantities with "greater equals"
     * @return Quantity with the result as Bool as a new Quantity
     */
    infix fun ge(quantity: Quantity): Quantity {
        if (value is Bool) throw BDDError("Greater equals not allowed for BDDs")
        return Quantity(this.value greaterThanOrEquals quantity.value)
    }

    /**
     * Compares quantities with "less equals"
     * @return Quantity with the result as Bool as a new Quantity
     */
    infix fun le(quantity: Quantity): Quantity {
        if (value is Bool) throw BDDError("Less equals not allowed for BDDs")
        return Quantity(this.value lessThanOrEquals quantity.value)
    }

    /**
     * Compares quantities with "equals"
     * @return Quantity with the result as Bool as a new Quantity
     */
    infix fun eq(quantity: Quantity): Quantity {
        return when (value) {
            is Bool -> Quantity((value as Bool).xor(quantity.value.asBdd()).not()) // and is the same as not xor
            is Integer -> Quantity((value lessThanOrEquals quantity.value).and(value greaterThanOrEquals quantity.value))
            is Real -> Quantity((value lessThanOrEquals quantity.value).and(value greaterThanOrEquals quantity.value))
            is StrDD -> Quantity((value as StrDD).equalValue(quantity.value.asStrDD()))
            else -> throw BDDError("equals only allowed for Real, Bool, Integer and StrDD")
        }


    }

    infix fun neq(quantity: Quantity): VectorQuantity {
        if (value is Bool) throw BDDError("Not equals not allowed for BDDs")
        val equalsValue = (this eq quantity).value
        return VectorQuantity(equalsValue.asBdd().not())
    }

//--------------Boolean operations--------------------------------

    /**
     * Applies boolean "and" operation
     * @return Quantity with the result as Bool as a new Quantity
     */
    infix fun and(quantity: Quantity): Quantity {
        if (value !is Bool) throw BDDError("Boolean \"and\" can only be applied to BDDs")
        return Quantity(this.value.asBdd() and quantity.value.asBdd())
    }

    /**
     * Applies boolean "or" operation
     * @return Quantity with the result as a new Quantity
     */
    infix fun or(quantity: Quantity): Quantity {
        if (value !is Bool) throw BDDError("Boolean \"or\" can only be applied to BDDs")
        //return this.value.ite(this, quantity)
        //return Quantity(this.value.builder.variable(QualifiedName()).ite(this.value, quantity.value), this.unit)


        return Quantity(this.value.asBdd() or quantity.value.asBdd())
    }

//--------------Miscellaneous operations--------------------------------

    /**
     * The Ceil operation on Real, rounds up to the next integer value
     * @return Quantity with Real (Real) type as a new Quantity
     */
    override fun ceil(): Quantity {
        if (value !is Real)
            throw SemanticError("Ceil must have parameter of type Real")
        return Quantity((this.value as Real).ceil(), unit, unitSpec)
    }

    /** Floor operation on Real just rounds up to the next integer value
     * @return Quantity with Real (Real) type as a new Quantity
     */
    override fun floor(): Quantity {
        if (value !is Real)
            throw SemanticError("Floor must have parameter of type Real")
        return Quantity((this.value as Real).floor(), unit, unitSpec)
    }

    /**
     * Applies the square root to a Quantity
     * Example: 100 m^2 --> 10 m
     * @return Quantity with the result as a new Quantity
     */
    override fun sqrt(): Quantity {
        //calculate final sqrt value
        val finalValue: DD<*> = when (value) {
            is Real -> (value as Real).sqrt()
            is Integer -> (value as Integer).sqrt()
            else -> throw SemanticError("Sqrt not allowed for any other type than Real or Integer")
        }

        //Calculating sqrt of unit exponents and adding to result unit
        var resUnit = Unit()
        if (unit.toString() == "?")
            resUnit = Unit("?")
        else {
            for (element in unit.unitSet) {
                if (element.exponent % 2 != 0)
                    throw SquareRootError(unit.toString())
                val currentElement = element.clone()
                currentElement.exponent /= 2
                resUnit.unitSet.add(currentElement)
            }
        }
        return Quantity(finalValue, resUnit)
    }

    /**
     * Applies the square to a Quantity
     * Example: 100 m^2 --> 10 m
     * @return Quantity with the result as a new Quantity
     */
    override fun sqr(): Quantity {
        //calculate final sqrt value
        val finalValue = when (value) {
            is Real -> value.asAadd().pow(value.builder.real(2.0))
            is Integer -> value.asIdd().sqr()
            else -> throw BDDError("Sqr not allowed for any other type than Real or Integer")
        }

        var resUnit = Unit()
        if (unit.toString() == "?")
            resUnit = Unit("?")
        else for (element in unit.unitSet) {
            val currentElement = element.clone()
            currentElement.exponent *= 2
            resUnit.unitSet.add(currentElement)
        }
        return Quantity(finalValue, resUnit)
    }

    /**
     * calculates log base e (ln) of a Quantity
     * @return Quantity with the result as a new Quantity
     */
    override fun ln(): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Log with units is not allowed")
        val finalValue: DD<*> = when (value) {
            is Real -> (value as Real).log()
            is Integer -> (value as Integer).log()
            else -> throw SemanticError("Ln not allowed for any other type than Real or Integer.")
        }
        return Quantity(finalValue, unit)
    }

    /**
     * calculates log of a Quantity with a given base
     * @param base base value for the logarithm
     * @return Quantity with the result as a new Quantity
     */
    override fun log(base: DD<*>): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Log with units is not allowed")
        val finalValue: DD<*> = when (value) {
            is Real -> (value as Real).log() / (base as Real).log()
            is Integer -> (value as Integer).log(base as Integer)
            else -> throw SemanticError("Log not allowed for any other type than Real or Integer.")
        }
        return Quantity(finalValue, this.unit.clone())
    }

    /**
     * calculates exp of a Quantity
     * It is used for the following function: f(x) = e^x
     * @return Quantity with the result as a new Quantity
     */
    override fun exp(): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() == "?") throw SemanticError("Exp with units is not allowed")
        val resultValue: DD<*> = when (value) {
            is Real -> value.asAadd().exp()
            is Integer -> value.asIdd().exp()
            else -> throw SemanticError("Exp only possible with Integer and Real")
        }
        return Quantity(resultValue, this.unit.clone())
    }

    /**
     * Calculates Pow2 for Quantities
     * It is used for the following function: f(x) = 2^x
     * @return Quantity with the result as a new Quantity
     */
    override fun pow2(): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Pow2 with units is not allowed")
        val resultValue: DD<*> = when (value) {
            is Real -> value.asAadd().power2()
            is Integer -> value.asIdd().power2()
            else -> throw SemanticError("Pow2 only possible with Integer and Real")
        }
        return Quantity(resultValue, unit.clone())
    }

    /**
     * Calculates Pow for Quantities: f(x,y) = x^y
     * @param exponent Exponent for the Pow function
     * @return Quantity with the result as a new Quantity
     */
    override fun pow(exponent: DD<*>): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() !in setOf("1", "?", "dB"))
            throw SemanticError("Power with units is not allowed")
        val resultValue: DD<*> = when (value) {
            is Real -> value.asAadd() power exponent.asAadd()
            is Integer -> pow(value.asIdd(), exponent.asIdd())
            else -> throw SemanticError("Power only possible with Integer and Real")
        }
        return Quantity(resultValue, unit.clone())
    }

    override fun sin(): Quantity {
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Sin with units is not allowed")
        val resultValue: DD<*> = when (value) {
            is Real -> value.asAadd().sin()
            else -> throw SemanticError("Sin only possible with Real")
        }
        return Quantity(resultValue, unit.clone())
    }

    override fun arcsin(): Quantity {
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("arcsin with units is not allowed")
        val resultValue: DD<*> = when (value) {
            is Real -> value.asAadd().arcsin()
            else -> throw SemanticError("arcsin only possible with Real")
        }
        return Quantity(resultValue, unit.clone())
    }

    override fun cos(): Quantity {
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Cos with units is not allowed")
        val resultValue: DD<*> = when (value) {
            is Real -> value.asAadd().cos()
            else -> throw SemanticError("Cos only possible with Real")
        }
        return Quantity(resultValue, unit.clone())
    }

    override fun arccos(): Quantity {
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("arccos with units is not allowed")
        val resultValue: DD<*> = when (value) {
            is Real -> value.asAadd().arccos()
            else -> throw SemanticError("arccos only possible with Real")
        }
        return Quantity(resultValue, unit.clone())
    }

    /**
     * @return the min value of the Range as Double
     */
    override fun getMinAsDouble(): Double {
        if (value is Bool) throw BDDError("No min value for BDDs")
        if (value is Real) return aadd().getRange().min
        if (value is Integer) return idd().getRange().min.toDouble()
        throw SemanticError("Expect either Real or Integer")
    }

    /**
     * @return the max value of the Range as Double
     */
    override fun getMaxAsDouble(): Double {
        if (value is Bool) throw BDDError("No max value for BDDs")
        if (value is Real) return aadd().getRange().max
        if (value is Integer) return idd().getRange().max.toDouble()
        throw SemanticError("Expect either Real or Integer")
    }

    /**
     * @return the Range of the Real/Integer
     */
    fun getRange(): Range {
        return when (value) {
            is Bool -> throw BDDError("No range value for BDDs")
            is Real -> aadd().getRange()
            is Integer -> Range(getMinAsDouble(), getMaxAsDouble())
            else -> throw BDDError("No conversion to range possible.")
        }
    }

    /**
     * @return the IntegerRange of the Real/Integer
     */
    fun getIntRange(): IntegerRange {
        return when (value) {
            is Bool -> throw BDDError("No range value for BDDs")
            is Integer -> idd().getRange()
            is Real -> aadd().getRange().toIntegerRange()
            else -> throw BDDError("No conversion to range possible.")
        }
    }

    /**
     * Transforms the Quantity to a String with the unit as a value and a fraction of units
     * If the Quantity contains a unitSpec, the unit is transformed to this Quantity before returning the string.
     * @Return a string representation of the Quantity
     */
   override fun toString(): String {
        when (value) {
            is Integer -> {
                val range = (value as Integer).getRange()
                return when {
                    range.min == range.max -> range.min.toString()
                    range.min > range.max -> "∅"
                    else -> {
                        val min = if (range.min == Long.MIN_VALUE) "*" else range.min.toString()
                        val max = if (range.max == Long.MAX_VALUE) "*" else range.max.toString()
                        "$min..$max"
                    }
                }
            }
            is Bool -> return (value as Bool).toString()
            is StrDD -> return (value as StrDD).toString()
            is Real -> {
                if (value.isInfeasible) return Representer().represent(value.asAadd())
                if (unitSpec == "DateTime" && value.asAadd().getRange().isFinite()) {
                    val min = timeToString(round(value.asAadd().getRange().min))
                    val max = timeToString(round(value.asAadd().getRange().max))
                    return if (min == max) max else "$min .. $max"
                }
                if (unitSpec == "Date" && value.asAadd().getRange().isFinite()) return dateToString(value.asAadd().getRange().max)
                if (unitSpec == "Month" && value.asAadd().getRange().isFinite()) return monthToString(value.asAadd().getRange().max)
                if (unitSpec == "Year" && value.asAadd().getRange().isFinite()) return yearToString(value.asAadd().getRange().max)

                val (transformedValue, transformedUnitString) = when {
                    unitSpec.isNotEmpty() -> valueIn(unitSpec) to unitSpec
                    unit.toString() == "?" -> value to unit.toString()
                    unit.calculatedUnitSymbol.isNotEmpty() -> {
                        val unitSymbol = unit.calculatedUnitSymbol
                        //find the best prefix by trying every prefix
                        // and use the one with a value bigger than one and the maximum prefix factor
                        val quantityCalc = Quantity(value, Unit(unitSymbol))
                        val bestSolution = ConversionTables.prefixes
                            .filter { it.key.isEmpty() || it.key.last() != 'i' }
                            .maxByOrNull { prefix ->
                                val valueInPrefix = quantityCalc.valueIn(prefix.key + unitSymbol)
                                val min = valueInPrefix.asAadd().min
                                val max = valueInPrefix.asAadd().max // * 1.0001 to avoid rounding errors to cause problems, value should be bigger than one or a too small value for prefixes
                                if ((abs(min) * 1.0001 >= 1 || abs(max) < 0e-24) && abs(max / min) <= 10.0.pow(24)) prefix.value.factor else Double.MIN_VALUE
                            }?.value ?: NoPrefix
                        val finalUnitSymbol = when { //some special cases for ha, l, cm and ml
                            unitSymbol == "m^2" && bestSolution == Hecto -> "ha"
                            unitSymbol == "m^3" && bestSolution == Deci -> "l"
                            unitSymbol == "m" && (bestSolution == Centi || bestSolution == Deci) -> "cm"
                            bestSolution.symbol !in listOf("d", "c", "da", "h") || unitSymbol in listOf("m^2", "m^3") -> bestSolution.symbol + unitSymbol
                            else -> unitSymbol
                        }
                        quantityCalc.valueIn(finalUnitSymbol) to finalUnitSymbol
                    }
                    else -> value.clone() to unit.toString()  //No unit is given or can be calculated.
                                                             // So, better use the given unit and value
                }

                val valueStr = Representer().represent(transformedValue.asAadd())
                return if (transformedUnitString == "1" || valueStr == "∅") valueStr else "$valueStr $transformedUnitString"
            }
            else -> throw DDError("Unsupported value type for toString: $value")
        }
    }

    /**
     * Converts a timestamp to a DateTime string
     */
    override fun timeToString(timestamp: Double): String {
        if (timestamp.isFinite())
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.toLong()), UTC).toString()
        return "Infinity"
    }

    /**
     * Converts a timestamp to a date string
     */
    override fun dateToString(timestamp: Double): String {
        if (timestamp.isFinite()) {
            var result = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.toLong()), UTC)
            //round date to next day if time is after 12, because date is only used at start of day
            if (result.hour >= 12) result = result.plusDays(1)
            return result.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
        return "Infinity"
    }

    /**
     * Converts a timestamp to a month string
     */
    override fun monthToString(timestamp: Double): String {
        if (timestamp.isFinite()) {
            var result = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.toLong()), UTC)
            //round day to next month if day is after 15, because it is used only at start of month
            if (result.dayOfMonth >= 15) result = result.plusMonths(1)
            val date = result.format(DateTimeFormatter.ISO_LOCAL_DATE)
            return date.substring(0..6) //remove the day, because only the month should be considered
        }
        return "Infinity"
    }

    /**
     * Converts a timestamp to a month string
     */
    override fun yearToString(timestamp: Double): String {
        if (timestamp.isFinite()) {
            var result = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.toLong()), UTC)
            //round month to next year if month is after 7, because it is used only at start of year
            if (result.month.value >= 7) result = result.plusYears(1)
            val date = result.format(DateTimeFormatter.ISO_LOCAL_DATE)
            return date.substring(0..3) //remove the day and month, because only the year should be considered
        }
        return "Infinity"
    }

    override fun bdd(): Bool = value as Bool
    override fun aadd(): Real = value as Real
    override fun idd(): Integer = value as Integer

    /**
     * Converts this unit to the expected unit representation and returns the value of the conversion
     * @param wantedRepresentation String of Unit's wanted representation
     * @Return value in Real/Integer of the result
     */
    fun valueIn(wantedRepresentation: String): DD<*> {
        val quantity = this.clone()
        val expectedUnit = Unit(wantedRepresentation)
        // Unit "1" means there is no unit to transform to
        if (expectedUnit.toString() == "1" || unit.toString() == "?")
            return quantity.value
        // Logarithmic quantity is transformed to not logarithmic Quantity
        if (expectedUnit.isLogarithmic) {
            val ten = value.builder.real(10.0)
            return ten * quantity.value.asAadd().log() / ten.log()
        }
        //Special case for temperature to temperature conversion From K to °C/°F
        if (quantity.unit.unitSet.isNotEmpty() && expectedUnit.unitSet.isNotEmpty()) {
            val unit1 = quantity.unit.unitSet.elementAt(0)
            val unit2 = expectedUnit.unitSet.elementAt(0)
            if (unit1 is ThermodynamicTemperature && unit2 is ThermodynamicTemperature && unit2.name != "kelvin")
                return unit1.convertTo(quantity.value * unit1.prefix.factor, unit2)
        }
        //1) Make expected unit canonical and calculate correlationFac
        val temporaryExpected = Quantity(value.builder.real(1.0), expectedUnit.clone())
        temporaryExpected.toSI()
        val correlationFac = temporaryExpected.value

        //2) Compare them
        if (quantity.unit == temporaryExpected.unit) return quantity.value.div(correlationFac)
        else throw TransformationError("$expectedUnit and $unit")
    }


    /**
     * Removes all Prefixes from a Quantity and updates the values
     */
    private fun removePrefixes() {
        var resultValue = value
        for (element in unit.unitSet) {
            resultValue *= element.prefix.factor.pow(element.exponent)
            element.prefix = NoPrefix
        }
        value = resultValue
    }

    /**
     * Transforms Quantity to the SI System
     */
    private fun toSI() {
        removePrefixes()
        if (unit.isLogarithmic) { // Logarithmic quantity is transformed to not logarithmic
            val ten = value.builder.real(10.0)
            value = ten.power(value.asAadd() / ten)
            unit.isLogarithmic = false
        }
        var resultValue = value
        val resultUnit = unit.clone()
        resultUnit.unitSet = mutableSetOf() // make resultSet empty
        for (currentUnit in unit.unitSet) {
            // Change Unit to SI
            (currentUnit.getBaseUnits()).forEach {
                val newUnitElement = it.clone()
                //change exponent of derived unit
                newUnitElement.exponent = currentUnit.exponent * it.exponent
                resultUnit.addUnitOfMeasurement(newUnitElement)
            }
            // Update value
            if (currentUnit is ThermodynamicTemperature) resultValue = currentUnit.toKelvin(resultValue)
            resultValue *= currentUnit.convFac.pow(currentUnit.exponent)
        }

        //make unit canonical
        value = resultValue
        unit = resultUnit
        unit.reduceRedundantUnits()
    }

    override fun getDomain(): String = unit.getUnitDomain(value.asAadd().getRange().min)

    /**
     * Intersects a Quantity with another Quantity of the same property (e.g., upQuantity with downQuantity)
     * @param q Intersect the current Quantity with this Quantity
     * @return Intersected Quantity as a new Quantity
     **/
    fun intersect(q: Quantity): Quantity {
        val thisClone = this.clone()
        if (unit.toString() == "?") return thisClone
        val newQuantity = q.clone()
        // Do intersection for REAL or INT
        return when (thisClone.value) {
            is Real -> Quantity(thisClone.value.asAadd() intersect newQuantity.aadd(), unit, unitSpec)
            is Integer -> Quantity(thisClone.value.asIdd() intersect newQuantity.idd())
            is Bool -> Quantity(thisClone.value.asBdd() intersect newQuantity.bdd())
            else -> throw DDError("Intersection only possible for Integer, Bool, and ADD, not $thisClone.propertyKind")
        }
    }

    /**
     * Constrain a Quantity with the interval of this property (intSpec)
     * @param intSpec the current Quantity should be constrained to this interval
     * @return Constrained Quantity as a new Quantity
     **/
    fun constrain(intSpec: IntegerRange): Quantity {
        if (unit.toString() == "?") return this.clone()
        //calculate constrainTo (only if no units are used)
        return Quantity(value.asIdd().constrainTo(intSpec))
    }

    /**
     * Constrain a Quantity with the interval of this valueFeature (only for Real)
     * TODO: define strategy that ensures that intersect in evalUp/Down does not introduce
     *  arbitrary many comparisons and hence growing size of Bool/Real
     * @param q The Quantity, which should be constrained to
     * @param rangeSpec the specified range
     * @param unitSpec the wanted representation of the Unit
     * @return Constrained Quantity as a new Quantity
     **/
    fun constrain(q: Quantity, rangeSpec: Range, unitSpec: String): Quantity {
        val thisClone = this.clone()

        val newQuantity = q.clone()
        // convert RangeSpec, which is in UnitSpec into SI Unit, by using a new Quantity
        val rangeQuantity = Quantity(value.builder.real(rangeSpec), Unit(unitSpec))

        // calculate intersection of propagated and specified values
        var result = thisClone.value.asAadd() constrainTo rangeQuantity.getRange()

        // only constrain to newQ if not infinite and not empty
        if (newQuantity.value.toString() != "Real" && !(newQuantity.value as Real).isEmpty())
            result = result.asAadd() constrainTo newQuantity.getRange()

        return Quantity(result, unit, unitSpec)
    }

    /**
     * Constrain function for two Quantities with the same unit (normally SI), for Integer and Real
     */
    fun constrain(q: Quantity): Quantity {
        val newQuantity = q.clone()
        var result = this.clone().value
        // only constrain to newQ if not infinite and not empty
        when (newQuantity.value) {
            is Real -> if (!(newQuantity.value as Real).isEmpty())
                result = result.asAadd() constrainTo newQuantity.getRange()

            is Integer -> if (!(newQuantity.value as Integer).isEmpty())
                result = result.asIdd() constrainTo newQuantity.getIntRange()

            else -> throw SemanticError("Constrain only for Integer and Real")
        }
        // Iff one of the results was NaN, continue with the other (???)
        return Quantity(result, unit, unitSpec)
    }

    /**
     * Returns a new quantity constrained to spec.
     * @param constraint: the constraint to be applied.
     * @return a new quantity that is q, constrained to constraint.
     */
    fun constrain(constraint: XBool): Quantity =
        Quantity((value.builder.constant(constraint) intersect this.value) as Bool)

    /**
     * Compares a quantity with another object.
     * @param other the other object
     * @return true, if equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Quantity) return false

        if (unit.toString() != other.unit.toString()) return false

        when (value) {
            is Real -> {
                val min1 = value.asAadd().getRange().min
                val max1 = value.asAadd().getRange().max
                val min2 = other.value.asAadd().getRange().min
                val max2 = other.value.asAadd().getRange().max
                //if the difference is too big, they are different
                if (abs(min1 - min2) > abs(min1) * 0.0001) return false
                if (abs(max1 - max2) > abs(max1) * 0.0001) return false
            }

            is Integer -> {
                val min1 = value.asIdd().getRange().min
                val max1 = value.asIdd().getRange().max
                val min2 = other.value.asIdd().getRange().min
                val max2 = other.value.asIdd().getRange().max
                // For infinite values compare borders
                if (min1 == Long.MIN_VALUE || min1 == Long.MAX_VALUE)
                    return min1 == min2 && max1 == max2
                //if the difference is too big, they are different
                if (abs(min1 - min2) > abs(min1) * 0.0001) return false
                if (abs(max1 - max2) > abs(max1) * 0.0001) return false
            }
            // No units ...
            is Bool,
            is StrDD -> return value.toString() == other.value.toString()

            else -> {}
        }
        return true
    }

    fun contains(other: Quantity): Boolean {
        return when (value) {
            is Real -> value.asAadd().contains(other.value.asAadd())
            is Integer -> value.asIdd().contains(other.value.asIdd())
            else -> throw DDError("Unsupported value type for contains, only Real and Integer are supported")
        }
    }

    /**
     * Length of vector
     * @return Quantity with length and unit of vector
     */
    override fun abs(): Quantity {
        val result = this.clone()
        when (value) {
            is Integer -> {
                val min = value.asIdd().getRange().min
                val max = value.asIdd().getRange().max
                if (min <= 0 && max >= 0)  // interval has different signs
                    result.value = value.builder.integer(0..max(abs(min), max))
                else if (max < 0)  //everything is negative
                    result.value = value.builder.integer(abs(max)..abs(min))
                else  //everything is positive
                    result.value = value.builder.integer(min..max)
            }

            is Real -> {
                val min = getMinAsDouble()
                val max = getMaxAsDouble()
                if (min <= 0.0 && max >= 0.0) // interval has different signs
                    result.value = value.builder.real(0.0..max(abs(min), max))
                else if (max < 0.0) //everything is negative
                    result.value = value.builder.real(abs(max)..abs(min))
                else  //everything is positive
                    result.value = value.builder.real(min..max)
            }

            else -> throw DDError("Abs is only available for Integer and Real")
        }
        return result
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + unitSpec.hashCode()
        return result
    }
}

fun Bool.ite(t: Quantity, e: Quantity): Quantity {
    return Quantity(this.ite(t.value, e.value), t.unit)
}

/**
 * Returns the maximum of two quantities a, b.
 */
fun max(a: Quantity, b: Quantity): Quantity = a.ge(b).bdd().ite(a, b)

/**
 * Returns the minimum of two quantities a, b.
 */
fun min(a: Quantity, b: Quantity): Quantity = a.le(b).bdd().ite(a, b)
