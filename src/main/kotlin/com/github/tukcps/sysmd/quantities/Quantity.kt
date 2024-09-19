package com.github.tukcps.sysmd.quantities

import com.github.tukcps.aadd.*
import com.github.tukcps.aadd.functions.*
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.Range
import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.baseUnits.Temperature
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

/**
 * A quantity that consists of a value that is represented by a DD instance, and
 * a unit that is represented by SI units fraction. The unit is transformed to SI, so that
 * calculations are much efficient
 */
class Quantity : VectorQuantity {

    override var value: DD

    constructor(value: BDD) : super(value) {
        this.value = value.clone()
        this.unit = Unit("")
    }

    constructor(value: IDD) : super(value) {
        this.value = value.clone()
        this.unit = Unit("")
    }

    constructor(value: StrDD) : super(value) {
        this.value = value.clone()
        this.unit = Unit("")
    }

    /**
     * Constructor only for AADD
     * @param value Value of the Quantity represented as a AADD, so that possible errors are considered
     * @param unitString String representation of the Unit
     */
    constructor(value: AADD, unitString: String) : super(value, unitString) {
        this.value = value.clone()
        unitSpec = unitString // use unitStr as unitSpec
        this.unit = Unit(unitString)
        makeCanonical()
    }

    /**
     * Constructor
     * @param value Value of the Quantity represented as a DD, so that possible errors are considered
     * @param unitObject Unit, which should be added to the new Quantity
     * @param unitSpec The wanted representation of the Unit, toString converts the Unit to this representation
     */
    constructor(value: DD, unitObject: Unit, unitSpec: String = "") : super(value, unitObject, unitSpec) {
        this.value = value.clone()
        this.unit = unitObject.clone()
        this.unitSpec = unitSpec
        makeCanonical()
    }

    /** Makes a perfect clone of a unit with new references of all objects */
    override fun clone(): Quantity = Quantity(value.clone(), unit.clone(), unitSpec.plus(""))

    /**
     *  Transforms the Unit to a canonical SI representation with the right UnitDimension
     */
    private fun makeCanonical() {
        toSI()
        unit.reduceRedundantUnits()
        unit.calculateUnitDimension(unitSpec)
        unit.calculateUnitSymbol(unitSpec)
    }


    //--------------Arithmetic operations--------------------------------

    /**
     * Multiplies quantities
     * @param quantity is multiplied to the current Quantity
     * @return Quantity with resulting AADD/IDD value and Unit as a new Quantity
     */
    operator fun times(quantity: Quantity): Quantity {
        if (value is BDD) throw BDDError("Multiplication not allowed for BDDs")
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
     * @return Quantity with resulting AADD/IDD value and Unit as a new Quantity
     */
    operator fun div(quantity: Quantity): Quantity {
        if (value is BDD) throw BDDError("Division not allowed for BDDs")
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
     * @return Quantity with resulting AADD/IDD value and Unit as a new Quantity
     */
    operator fun plus(quantity: Quantity): Quantity {
        if (value is BDD) throw BDDError("Addition not allowed for BDDs")

        return if (unit == quantity.unit || quantity.unit.toString() == "?")
            Quantity(quantity.value + value, unit, unitSpec)
        else return if (unit.toString() == "?")
            Quantity(quantity.value + value, quantity.unit, quantity.unitSpec)
        else {
            // Possibility to add 0 to a Quantity with a unit. Error range for 0 same as for equals.
            // Ugly, but we need to assume even small numbers as 0s ...
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
     * @return Quantity with resulting AADD/IDD value and Unit as a new Quantity.
     */
    operator fun minus(quantity: Quantity): Quantity {
        if (value is BDD) throw SemanticError("Subtraction not allowed on BDDs")
        // Set isDifference of resultUnit to true
        unit.isDifference = true
        quantity.unit.isDifference = true
        return if (unit == quantity.unit || quantity.unit.toString() == "?")
            Quantity(value - quantity.value, unit, unitSpec)
        else return if (unit.toString() == "?")
            Quantity(value - quantity.value, quantity.unit, quantity.unitSpec)
        else
            throw SubtractionError("${this.unit} and ${quantity.unit}")
    }

    /**
     * Negates sign of Real or Integer-Valued Variable
     */
    override fun negate(): Quantity {
        return when (this.value) {
            is AADD -> Quantity((value as AADD).negate(), unit, unitSpec)
            is IDD -> Quantity((value as IDD).negate())
            else -> throw SemanticError("unary minus only applicable on values of type Real or Integer")
        }
    }

    /**
     * Infix fun for power
     * @param quantity Exponent for the Pow function
     * @return result of the calculation
     */
    infix fun pow(quantity: Quantity): Quantity {
        if (value is BDD) throw BDDError("Pow not allowed for BDDs")
        val quantity1 = this.clone()
        val quantity2 = quantity.clone()
        return quantity1.pow(quantity2.value)
    }

//--------------Compare operations--------------------------------

    /**
     * Compares quantities with "greater than"
     * @return Quantity with the with result as BDD as a new Quantity
     */
    infix fun gt(quantity: Quantity): Quantity {
        if (value is BDD) throw BDDError("Greater than not allowed for BDDs")
        return Quantity(this.value greaterThan quantity.value)
    }

    /**
     * Compares quantities with "less than"
     * @return Quantity with the with result as BDD as a new Quantity
     */
    infix fun lt(quantity: Quantity): Quantity {
        if (value is BDD) throw BDDError("Less than not allowed for BDDs")
        return Quantity(this.value lessThan quantity.value)
    }

    /**
     * Compares quantities with "greater equals"
     * @return Quantity with the with result as BDD as a new Quantity
     */
    infix fun ge(quantity: Quantity): Quantity {
        if (value is BDD) throw BDDError("Greater equals not allowed for BDDs")
        return Quantity(this.value greaterThanOrEquals quantity.value)
    }

    /**
     * Compares quantities with "less equals"
     * @return Quantity with the with result as BDD as a new Quantity
     */
    infix fun le(quantity: Quantity): Quantity {
        if (value is BDD) throw BDDError("Less equals not allowed for BDDs")
        return Quantity(this.value lessThanOrEquals quantity.value)
    }

    /**
     * Compares quantities with "equals"
     * @return Quantity with the with result as BDD as a new Quantity
     */
    infix fun eq(quantity: Quantity): Quantity {
        return when (value) {
            is BDD -> Quantity((value as BDD).xor(quantity.value.asBdd()).not()) // and is the same as not xor
            is IDD -> Quantity((value lessThanOrEquals quantity.value).and(value greaterThanOrEquals quantity.value))
            is AADD -> Quantity((value lessThanOrEquals quantity.value).and(value greaterThanOrEquals quantity.value))
            is StrDD -> Quantity((value as StrDD).equalValue(quantity.value.asStrDD()))
            else -> throw BDDError("equals only allowed for AADD, BDD, IDD and StrDD")
        }


    }

    infix fun neq(quantity: Quantity): VectorQuantity {
        if (value is BDD) throw BDDError("Not equals not allowed for BDDs")
        val equalsValue = (this eq quantity).value
        return VectorQuantity(equalsValue.asBdd().not())
    }

//--------------Boolean operations--------------------------------

    /**
     * Applies boolean "and" operation
     * @return Quantity with the with result as BDD as a new Quantity
     */
    infix fun and(quantity: Quantity): Quantity {
        if (value !is BDD) throw BDDError("Boolean \"and\" can only be applied to BDDs")
        return Quantity(this.value.asBdd() and quantity.value.asBdd())
    }

    /**
     * Applies boolean "or" operation
     * @return Quantity with the with result as a new Quantity
     */
    infix fun or(quantity: Quantity): Quantity {
        if (value !is BDD) throw BDDError("Boolean \"or\" can only be applied to BDDs")
        //return this.value.ite(this, quantity)
        //return Quantity(this.value.builder.variable(QualifiedName()).ite(this.value, quantity.value), this.unit)


        return Quantity(this.value.asBdd() or quantity.value.asBdd())
    }

//--------------Miscellaneous operations--------------------------------

    /**
     * The Ceil operation on AADD, rounds up to next integer value,
     * @return Quantity with Real (AADD) type as a new Quantity
     */
    override fun ceil(): Quantity {
        if (value !is AADD)
            throw SemanticError("Ceil must have parameter of type Real")
        return Quantity((this.value as AADD).ceil(), unit, unitSpec)
    }

    /** Floor operation on AADD just rounds up to next integer value,
     * @return Quantity with Real (AADD) type as a new Quantity
     */
    override fun floor(): Quantity {
        if (value !is AADD)
            throw SemanticError("Floor must have parameter of type Real")
        return Quantity((this.value as AADD).floor(), unit, unitSpec)
    }

    /**
     * Applies the square root to a Quantity
     * Example: 100 m^2 --> 10 m
     * @return Quantity with result as a new Quantity
     */
    override fun sqrt(): Quantity {
        //calculate final sqrt value
        val finalValue: DD = when (value) {
            is AADD -> { // either bigger or equal than zero or not defined
                require((value as AADD).min >= -0.00001 || (value as AADD).min.isInfinite()) { "Sqrt only possible for values greater or equal than zero" }
                (value as AADD).sqrt()
            }

            is IDD -> { // either bigger or equal than zero or not defined
                require((value as IDD).min >= 0 || (value as IDD).min == value.builder.Integers.min) { "Sqrt only possible for values greater or equal than zero" }
                (value as IDD).sqrt()
            }

            else -> throw SemanticError("Sqrt not allowed for any other type than AADD or IDD")
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
     * @return Quantity with result as a new Quantity
     */
    override fun sqr(): Quantity {
        //calculate final sqrt value
        val finalValue = when (value) {
            is AADD -> value.asAadd().pow(value.builder.scalar(2.0))
            is IDD -> value.asIdd().sqr()
            else -> throw BDDError("Sqr not allowed for any other type than AADD or IDD")
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
     * @return Quantity with result as a new Quantity
     */
    override fun ln(): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Log with units is not allowed")
        val finalValue: DD = when (value) {
            is AADD -> (value as AADD).log()
            is IDD -> (value as IDD).log()
            else -> throw SemanticError("Log not allowed for any other type than AADD or IDD.")
        }
        return Quantity(finalValue, unit)
    }

    /**
     * calculates log of a Quantity with a given base
     * @param base base value for the logarithm
     * @return Quantity with result as a new Quantity
     */
    override fun log(base: DD): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Log with units is not allowed")
        val finalValue: DD = when (value) {
            is AADD -> (value as AADD).log() / (base as AADD).log()
            is IDD -> (value as IDD).log(base as IDD)
            else -> throw SemanticError("Log not allowed for any other type than AADD or IDD.")
        }
        return Quantity(finalValue, this.unit.clone())
    }

    /**
     * calculates exp of a Quantity
     * It is used for the following function: f(x) = e^x
     * @return Quantity with result as a new Quantity
     */
    override fun exp(): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() == "?") throw SemanticError("Exp with units is not allowed")
        val resultValue: DD = when (value) {
            is AADD -> value.asAadd().exp()
            is IDD -> value.asIdd().exp()
            else -> throw SemanticError("Exp only possible with IDD and AADD")
        }
        return Quantity(resultValue, this.unit.clone())
    }

    /**
     * Calculates Pow2 for Quantities
     * It is used for the following function: f(x) = 2^x
     * @return Quantity with result as a new Quantity
     */
    override fun pow2(): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Pow2 with units is not allowed")
        val resultValue: DD = when (value) {
            is AADD -> value.asAadd().power2()
            is IDD -> value.asIdd().power2()
            else -> throw SemanticError("Pow2 only possible with IDD and AADD")
        }
        return Quantity(resultValue, unit.clone())
    }

    /**
     * Calculates Pow for Quantities: f(x,y) = x^y
     * @param exponent Exponent for the Pow function
     * @return Quantity with result as a new Quantity
     */
    override fun pow(exponent: DD): Quantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() !in setOf("1", "?", "dB"))
            throw SemanticError("Power with units is not allowed")
        val resultValue: DD = when (value) {
            is AADD -> value.asAadd() power exponent.asAadd()
            is IDD -> pow(value.asIdd(), exponent.asIdd())
            else -> throw SemanticError("Power only possible with IDD and AADD")
        }
        return Quantity(resultValue, unit.clone())
    }

    override fun sin(): Quantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Sin with units is not allowed")
        val resultValue: DD = when (value) {
            is AADD -> value.asAadd().sin()
            else -> throw SemanticError("Sin only possible with AADD")
        }
        return Quantity(resultValue, unit.clone())
    }

    override fun arcsin(): Quantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("arcsin with units is not allowed")
        val resultValue: DD = when (value) {
            is AADD -> value.asAadd().arcsin()
            else -> throw SemanticError("arcsin only possible with AADD")
        }
        return Quantity(resultValue, unit.clone())
    }

    override fun cos(): Quantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Cos with units is not allowed")
        val resultValue: DD = when (value) {
            is AADD -> value.asAadd().cos()
            else -> throw SemanticError("Cos only possible with AADD")
        }
        return Quantity(resultValue, unit.clone())
    }

    override fun arccos(): Quantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("arccos with units is not allowed")
        val resultValue: DD = when (value) {
            is AADD -> value.asAadd().arccos()
            else -> throw SemanticError("arccos only possible with AADD")
        }
        return Quantity(resultValue, unit.clone())
    }

    /**
     * @return the min value of the Range as Double
     */
    override fun getMinAsDouble(): Double {
        if (value is BDD) throw BDDError("No min value for BDDs")
        if (value is AADD) return aadd().getRange().min
        if (value is IDD) return idd().getRange().min.toDouble()
        throw SemanticError("Expect either Real or Integer")
    }

    /**
     * @return the max value of the Range as Double
     */
    override fun getMaxAsDouble(): Double {
        if (value is BDD) throw BDDError("No max value for BDDs")
        if (value is AADD) return aadd().getRange().max
        if (value is IDD) return idd().getRange().max.toDouble()
        throw SemanticError("Expect either Real or Integer")
    }

    /**
     * @return the Range of the AADD/IDD
     */
    fun getRange(): Range {
        return when (value) {
            is BDD -> throw BDDError("No range value for BDDs")
            is AADD -> aadd().getRange()
            is IDD -> Range(getMinAsDouble(), getMaxAsDouble())
            else -> throw BDDError("No conversion to range possible.")
        }
    }

    /**
     * @return the IntegerRange of the AADD/IDD
     */
    fun getIntRange(): IntegerRange {
        return when (value) {
            is BDD -> throw BDDError("No range value for BDDs")
            is IDD -> idd().getRange()
            is AADD -> aadd().getRange().toIntegerRange()
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
            is IDD -> {
                val value = (value as IDD).getRange()
                return if (value.min == value.max) value.min.toString() else {
                    val min = if (value.min == Long.MIN_VALUE) "*" else value.min.toString()
                    val max = if (value.max == Long.MAX_VALUE) "*" else value.max.toString()
                    "$min..$max"
                }
            }

            is BDD -> return (value as BDD).toString()
            is StrDD -> return (value as StrDD).toString()
            is AADD -> {
                if(value.isInfeasible)
                    return  Representer().represent(value.asAadd())
                if (unitSpec == "DateTime" && this.value.asAadd().getRange().isFinite()) {//Transform timestamp to real datetime
                    val min = timeToString(round(this.value.asAadd().getRange().min))
                    val max = timeToString(round(this.value.asAadd().getRange().max))
                    return if (min == max)
                        max
                    else
                        "$min .. $max"
                }
                if (unitSpec == "Date" && this.value.asAadd().getRange().isFinite())//Transform timestamp to real date
                    return dateToString(this.value.asAadd().getRange().max)
                if (unitSpec == "Month" && this.value.asAadd().getRange().isFinite())//Transform timestamp to real date
                    return monthToString(this.value.asAadd().getRange().max)
                if (unitSpec == "Year" && this.value.asAadd().getRange().isFinite())
                    return yearToString(this.value.asAadd().getRange().max)
                //normal Quantity with value and unit
                val transformedValue: DD
                val transformedUnitString: String
                if (unitSpec != "") {
                    transformedValue = valueIn(unitSpec)
                    transformedUnitString = unitSpec
                } else if (unit.toString() == "?") {
                    transformedValue = value
                    transformedUnitString = unit.toString()
                } else if (unit.calculatedUnitSymbol != "") {
                    val unitSymbol = unit.calculatedUnitSymbol //there is no prefix, because all defined units have no prefix
                    //find best prefix by trying every prefix and use the one with a value bigger than one and the maximum prefix factor
                    val quantityCalc = Quantity(value, Unit(unitSymbol))
                    var bestSolution: Prefix = Yocto
                    var bestSolutionFound = false
                    val prefixesNotBinary = ConversionTables.prefixes.filter { it.key == "" || it.key.last() != 'i' }
                    for (prefix in prefixesNotBinary) {  //do not use binary prefixes like Gi
                        if (bestSolution.factor < prefix.value.factor) { //only check for better factors
                            val valueInPrefix = quantityCalc.valueIn(prefix.key + unitSymbol)
                            // * 1.0001 to avoid rounding errors to cause problems, value should be bigger than one or a too small value for prefixes
                            val min = valueInPrefix.asAadd().min
                            val max = valueInPrefix.asAadd().max
                            if ((abs(min) * 1.0001 >= 1 || abs(max) < 0e-24) && abs(max / min) <= 10.0.pow(24)) {
                                bestSolution = prefix.value
                                bestSolutionFound = true
                            }
                        }
                    }
                    if (!bestSolutionFound)
                        bestSolution = NoPrefix//some special cases for ha, l, cm and ml
                    transformedUnitString = if (unitSymbol == "m^2" && bestSolution == Hecto) "ha"
                    else if (unitSymbol == "m^3" && bestSolution == Deci) "l"
                    else if (unitSymbol == "m" && (bestSolution == Centi || bestSolution == Deci)) "cm"
                    // only use small prefixes for length, area or volume
                    else if (bestSolution.symbol !in listOf("d", "c", "da", "h") || unitSymbol in listOf(
                            "m^2",
                            "m^3"
                        )
                    ) bestSolution.symbol + unitSymbol
                    else unitSymbol
                    transformedValue = quantityCalc.valueIn(transformedUnitString)
                } else { // no unit is given or can be calculated. So simply use the given unit and value
                    transformedValue = value.clone()
                    transformedUnitString = unit.toString()
                }
                //use representer to format value
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

    override fun bdd(): BDD = value as BDD
    override fun aadd(): AADD = value as AADD
    override fun idd(): IDD = value as IDD

    /**
     * Converts this unit to the expected unit representation and returns the value of the conversion
     * @param wantedRepresentation String of the wanted representation of the Unit
     * @Return value in AADD/IDD of the result
     */
    fun valueIn(wantedRepresentation: String): DD {
        val quantity = this.clone()
        val expectedUnit = Unit(wantedRepresentation)
        // Unit "1" means there is no unit to transform to
        if (expectedUnit.toString() == "1" || unit.toString() == "?")
            return quantity.value
        // Logarithmic quantity is transformed to not logarithmic Quantity
        if (expectedUnit.isLogarithmic) {
            val ten = value.builder.scalar(10.0)
            return ten * quantity.value.asAadd().log() / ten.log()
        }
        //Special case for temperature to temperature conversion From K to °C/°F
        if (quantity.unit.unitSet.size > 0 && expectedUnit.unitSet.size > 0) {
            val unit1 = quantity.unit.unitSet.elementAt(0)
            val unit2 = expectedUnit.unitSet.elementAt(0)
            if (unit1 is Temperature && unit2 is Temperature && unit2.name != "kelvin")
                return unit1.convertTo(quantity.value * unit1.prefix.factor, unit2)
        }
        //1) Make expected unit canonical and calculate correlationFac
        val temporaryExpected = Quantity(value.builder.scalar(1.0), expectedUnit.clone())
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
            val ten = value.builder.scalar(10.0)
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
            if (currentUnit is Temperature) resultValue = currentUnit.toKelvin(resultValue)
            resultValue *= currentUnit.convFac.pow(currentUnit.exponent)
        }

        //make unit canonical
        value = resultValue
        unit = resultUnit
        unit.reduceRedundantUnits()
    }

    override fun getDimension(): String = unit.getUnitDimension(value.asAadd().getRange().min)

    /**
     * Intersects a Quantity with another Quantity of the same property (e.g. upQuantity with downQuantity)
     * @param q Intersect the current Quantity with this Quantity
     * @return Intersected Quantity as a new Quantity
     **/
    fun intersect(q: Quantity): Quantity {
        val thisClone = this.clone()
        if (unit.toString() == "?") return thisClone
        val newQuantity = q.clone()
        // Do intersection for REAL or INT
        return when (thisClone.value) {
            is AADD -> Quantity(thisClone.value.asAadd() intersect newQuantity.aadd(), unit, unitSpec)
            is IDD -> Quantity(thisClone.value.asIdd() intersect newQuantity.idd())
            is BDD -> Quantity(thisClone.value.asBdd() intersect newQuantity.bdd())
            else -> throw DDError("Intersection only possible for IDD, BDD, and ADD, not $thisClone.propertyKind")
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
     * Constrain a Quantity with the interval of this valueFeature (only for AADD)
     * TODO: define strategy that ensures that intersect in evalUp/Down does not introduce
     *  arbitrary many comparisons and hence growing size of BDD/AADD
     *  TODO: Remove checks for NaN or Empty value if FIX for division by 0 during evalDown ready
     * @param q The Quantity which should be constrained to
     * @param rangeSpec the specified range
     * @param unitSpec the wanted representation of the Unit
     * @return Constrained Quantity as a new Quantity
     **/
    fun constrain(q: Quantity, rangeSpec: Range, unitSpec: String): Quantity {
        val thisClone = this.clone()

        val newQuantity = q.clone()
        // convert RangeSpec, which is in UnitSpec into SI Unit by using a new Quantity
        if (rangeSpec.isNaN()) return thisClone
        val rangeQuantity = Quantity(value.builder.range(rangeSpec), Unit(unitSpec))

        // calculate intersection of propagated and specified values
        var result = thisClone.value.asAadd() constrainTo rangeQuantity.getRange()

        // only constrain to newQ if not infinite and not empty
        if (newQuantity.value.toString() != "Real" && !(newQuantity.value as AADD).isEmpty())
            result = result.asAadd() constrainTo newQuantity.getRange()

        // Iff one of the results was NaN, continue with the other (???)
        if (result.isEmpty())
            return if ((newQuantity.value as AADD).isNaN()) thisClone
            else if ((thisClone.value as AADD).isNaN()) newQuantity
            else return Quantity(result, unit, unitSpec)
                //contains empty set
        return Quantity(result, unit, unitSpec)
    }

    /**
     * Constrain function for two Quantities with the same unit (normally SI), for IDD and AADD
     */
    fun constrain(q: Quantity): Quantity {
        val newQuantity = q.clone()
        var result = this.clone().value
        // only constrain to newQ if not infinite and not empty
        when (newQuantity.value) {
            is AADD -> if (!(newQuantity.value as AADD).isNaN() && !(newQuantity.value as AADD).isEmpty())
                result = result.asAadd() constrainTo newQuantity.getRange()

            is IDD -> if (!(newQuantity.value as IDD).isNaN() && !(newQuantity.value as IDD).isEmpty())
                result = result.asIdd() constrainTo newQuantity.getIntRange()

            else -> throw SemanticError("Constrain only for IDD and AADD")
        }
        // Iff one of the results was NaN, continue with the other (???)
        return Quantity(result, unit, unitSpec)
    }

    /**
     * Returns a new quantity that is constrained to spec.
     * @param constraint: the constraint to be applied.
     * @return a new quantity that is q, constrained to constraint.
     */
    fun constrain(constraint: XBool): Quantity =
        Quantity((value.builder.constant(constraint) intersect this.value) as BDD)

    /**
     * Compares a quantity with another object.
     * TODO: Should return BDD! And comparison should use jAADD comparison function.
     * @param other the other object
     * @return true, if equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Quantity) return false

        if (unit.toString() != other.unit.toString()) return false

        when (value) {
            is AADD -> {
                val min1 = value.asAadd().getRange().min
                val max1 = value.asAadd().getRange().max
                val min2 = other.value.asAadd().getRange().min
                val max2 = other.value.asAadd().getRange().max
                //if difference is too big, they are not the same
                if (abs(min1 - min2) > abs(min1) * 0.0001) return false
                if (abs(max1 - max2) > abs(max1) * 0.0001) return false
            }

            is IDD -> {
                val min1 = value.asIdd().getRange().min
                val max1 = value.asIdd().getRange().max
                val min2 = other.value.asIdd().getRange().min
                val max2 = other.value.asIdd().getRange().max
                // For infinite values compare borders
                if (min1 == value.builder.Integers.min || min1 == value.builder.Integers.max)
                    return min1 == min2 && max1 == max2
                //if difference is too big, they are not the same
                if (abs(min1 - min2) > abs(min1) * 0.0001) return false
                if (abs(max1 - max2) > abs(max1) * 0.0001) return false
            }
            // No units ...
            is BDD,
            is StrDD -> return value.toString() == other.value.toString()

            else -> {}
        }
        return true
    }

    /**
     * Length of vector
     * @return Quantity with length and unit of vector
     */
    override fun abs(): Quantity {
        val result = this.clone()
        when (value) {
            is IDD -> {
                val min = value.asIdd().getRange().min
                val max = value.asIdd().getRange().max
                if (min <= 0 && max >= 0)  // interval has different signs
                    result.value = value.builder.range(0, max(abs(min), max))
                else if (max < 0)  //everything is negative
                    result.value = value.builder.range(abs(max), abs(min))
                else  //everything is positive
                    result.value = value.builder.range(min, max)
            }

            is AADD -> {
                val min = getMinAsDouble()
                val max = getMaxAsDouble()
                if (min <= 0.0 && max >= 0.0) // interval has different signs
                    result.value = value.builder.range(0.0, max(abs(min), max))
                else if (max < 0.0) //everything is negative
                    result.value = value.builder.range(abs(max), abs(min))
                else  //everything is positive
                    result.value = value.builder.range(min, max)
            }
            else -> throw DDError("Abs is only available for IDD and AADD")
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


fun BDD.ite(t: Quantity, e: Quantity): Quantity {
    return Quantity(this.ite(t.value, e.value), t.unit)
}

/**
 * Returns the maximum of two quantities a, b.
 */
fun max(a: Quantity, b: Quantity): Quantity {
    val af = a.ge(b).bdd().ite(a, b)
    val max = max(a.getMaxAsDouble(), b.getMaxAsDouble())
    val min = max(a.getMinAsDouble(), b.getMinAsDouble())
    if (af.value is AADD)
        return Quantity(af.aadd().constrainTo(Range(min, max)), af.unit)
    else if (af.value is IDD)
        return Quantity(af.idd().constrainTo(IntegerRange(min, max)))
    throw SemanticError("Expect parameters of max to be Real or Integer.")
}

/**
 * Returns the minimum of two quantities a, b.
 */
fun min(a: Quantity, b: Quantity): Quantity = a.le(b).bdd().ite(a, b)
