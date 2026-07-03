package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.cspsolver.Variable
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
import kotlin.math.min
import kotlin.math.pow

/**
 * A quantity that consists of a value that is represented by a DD<*> instance, and
 * a unit that is represented by SI units fraction. The unit is transformed to SI, so that
 * calculations are more efficient
 */
@Suppress("UNCHECKED_CAST")
open class VectorQuantity : Cloneable {

    var values: List<DD<*>>
    var unit: Unit
    var unitSpec: String = ""
    open val value: DD<*>  // Returns first value as DD<*>
        get() = values[0]

    fun type() = when(values.first()) {
        is Real -> Variable.BaseType.Real
        is Integer  -> Variable.BaseType.Int
        is Bool  -> Variable.BaseType.Bool
        is StrDD-> Variable.BaseType.String
        else    -> Variable.BaseType.Unknown
    }

    constructor(value: Bool) {
        this.values = listOf<DD<*>>(value.clone())
        this.unit = Unit("")
    }

    constructor(values: List<DD<*>>) {
        if (values.isEmpty())
            throw DDError(msg = "Empty values for VectorQuantity is not supported")
        when(values[0]) {
            is Integer -> values.forEach { if (it !is Integer) throw DDError("Different value types in vector are not supported") }
            is Real    -> values.forEach { if (it !is Real)    throw DDError("Different value types in vector are not supported") }
            is Bool    -> values.forEach { if (it !is Bool)    throw DDError("Different value types in vector are not supported") }
            is StrDD   -> values.forEach { if (it !is StrDD)   throw DDError("Different value types in vector are not supported") }
            else       -> {}
        }
        this.values = values.toList()
        this.unit = Unit("")
    }

    constructor(value: Integer) {
        this.values = listOf<DD<*>>(value.clone())
        this.unit = Unit("")
    }

    constructor(value: StrDD) {
        this.values = listOf<DD<*>>(value.clone())
        this.unit = Unit("")
    }

    /**
     * Constructor only for Real
     * @param value Value of the VectorQuantity represented as a Real, so that possible errors are considered
     * @param unitString String representation of the Unit
     */
    constructor(value: Real, unitString: String) {
        this.values = listOf<DD<*>>(value.clone())
        unitSpec = unitString // use unitStr as unitSpec
        this.unit = Unit(unitString)
        makeCanonical()
    }

    constructor(values: List<Real>, unitString: String, unitDomain: String = "") {
        if (values.isEmpty()) throw DDError(msg = "Empty value for VectorQuantity is not supported")
        this.values = values.toList()
        unitSpec = unitString // use unitStr as unitSpec
        this.unit = Unit(unitString, unitDomain)
        makeCanonical()
    }

    /**
     * Constructor
     * @param value Value of the VectorQuantity represented as a DD<*>, so that possible errors are considered
     * @param unitObject Unit, which should be added to the new VectorQuantity
     * @param unitSpec The wanted representation of the Unit, toString converts the Unit to this representation
     */
    constructor(value: DD<*>, unitObject: Unit, unitSpec: String = "", unitDomain: String = "") {
        this.values = listOf<DD<*>>(value.clone())
        this.unit = unitObject.clone()
        this.unit.unitDomain = unitDomain
        this.unitSpec = unitSpec
        makeCanonical()
    }

    constructor(values: List<DD<*>>, unitObject: Unit, unitSpec: String = "", unitDomain: String = "") {
        if (values.isEmpty()) throw DDError(msg = "Empty value for VectorQuantity is not supported")
        when(values[0]) {
            is Integer -> values.forEach { if (it !is Integer) throw DDError("Different value types in vector are not supported") }
            is Real    -> values.forEach { if (it !is Real)    throw DDError("Different value types in vector are not supported") }
            is Bool    -> values.forEach { if (it !is Bool)    throw DDError("Different value types in vector are not supported") }
            is StrDD   -> values.forEach { if (it !is StrDD)   throw DDError("Different value types in vector are not supported") }
            else       -> {}
        }
        this.values = values.toList()
        this.unit = unitObject.clone()
        if(unitDomain!="")
            this.unit.unitDomain = unitDomain
        this.unitSpec = unitSpec
        makeCanonical()
    }

    /** Makes a perfect clone of a unit with new references of all objects */
    public override fun clone(): VectorQuantity = VectorQuantity(values.toList(), unit.clone(), unitSpec.plus(""))

    /**
     *  Transforms the Unit to a canonical SI representation with the right UnitDomain
     */
    private fun makeCanonical() {
        toSI()
        unit.reduceRedundantUnits()
        unit.calculateUnitDomain(unitSpec)
        unit.calculateUnitSymbol(unitSpec)
    }

    /**
     * Retrieves the Quantity at the specified position in the vector.
     * @param position The position of the Quantity to retrieve.
     * @return The Quantity at the specified position.
     * @throws VectorDimensionError if the position is out of bounds.
     */
    fun getQuantityAtPosition(position: IntegerRange): VectorQuantity {
        val startIndex = if (position.min < 0) values.size + position.min else position.min
        val endIndex = if (position.max < 0) values.size + position.max else position.max

        return if (startIndex in values.indices && endIndex in values.indices) {
            VectorQuantity(values.subList(startIndex.toInt(), endIndex.toInt() + 1), unit, unitSpec)
        } else if (values.size == 1 && (value.toString() == "Real" || value.toString() == "Integer")) {
            VectorQuantity(value, unit, unitSpec)
        } else {
            throw VectorDimensionError("Vector index out of bounds")
        }
    }


    //--------------Arithmetic operations--------------------------------

    /**
     * Scalar multiplication of vector and scalar or scalar and scalar
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
  operator fun times(quantity: VectorQuantity): VectorQuantity {
        val thisQuantity = clone()
        val otherQuantity = quantity.clone()
        if (thisQuantity.values[0] is Bool) {
            throw BDDError("Multiplication not allowed for BDDs")
        }
        var resultUnit = Unit()
        if (thisQuantity.unit.toString() == "?" || otherQuantity.unit.toString() == "?")
            resultUnit = Unit("?")
        else {
            // add all units of quantity1 and quantity2 to the unitSet of resultUnit
            thisQuantity.unit.clone().unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
            otherQuantity.unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
        }
        val resultingValues = when {
            thisQuantity.values.size == 1 -> otherQuantity.values.map { values[0] * it } //left scalar multiplication
            otherQuantity.values.size == 1 -> thisQuantity.values.map { it * otherQuantity.values[0] } //right scalar multiplication
            else -> throw VectorDimensionError("It is not possible to multiply vectors of size ${values.size} and ${quantity.values.size}. For scalar multiplication use 'dot' instead of '*'")
        }
        return VectorQuantity(resultingValues, resultUnit)
    }

    /**
     * Multiplies quantities with dot product
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    infix fun dot(quantity: VectorQuantity): Quantity {
        if (values[0] is Bool) {
            throw BDDError("Multiplication not allowed for BDDs")
        }
        val resultUnit = if (unit.toString() == "?" || quantity.unit.toString() == "?") Unit("?") else Unit("1")
        if (values.size != quantity.values.size) {
            throw VectorDimensionError("Dot product is not defined for vectors of size ${values.size} and ${quantity.values.size}")
        }
        val resultingValue: DD<*> = when (values[0]) {
            is Real -> values[0].builder.real(0.0)
            is Integer -> values[0].builder.integer(0)
            else -> throw DDError("Wrong type for dot product")
        }
        val sum = values.indices.fold(resultingValue) { acc, i -> acc.plus(values[i] * quantity.values[i]) }
        return Quantity(sum, resultUnit.clone())
    }

    /**
     * Multiplies quantities with cross-product (only possible for vectors with size 3)
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    infix fun cross(quantity: VectorQuantity): VectorQuantity {
        if (values[0] is Bool) throw BDDError("Multiplication not allowed for BDDs")
        if (values.size != 3 || quantity.values.size != 3)
            throw VectorDimensionError("Cross product is only defined for vectors of size 3, not of size ${values.size} and ${quantity.values.size}.")

        val resultUnit = if (unit.toString() == "?" || quantity.unit.toString() == "?") {
            Unit("?")
        } else {
            // Create a new unit by multiplying the units of both vectors
            val newUnit = Unit()
            unit.unitSet.forEach { newUnit.addUnitOfMeasurement(it.clone()) }
            quantity.unit.unitSet.forEach {  newUnit.addUnitOfMeasurement(it.clone())}
            newUnit
        }
        //Cross-product calculation
        val resultingValues = listOf(
            values[1] * quantity.values[2] - values[2] * quantity.values[1],
            values[2] * quantity.values[0] - values[0] * quantity.values[2],
            values[0] * quantity.values[1] - values[1] * quantity.values[0]
        )
        return VectorQuantity(resultingValues, resultUnit)
    }

    /**
     * Divides quantities element wise
     * @param quantity is the divisor of the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    operator fun div(quantity: VectorQuantity): VectorQuantity {
        val thisQuantity = clone()
        val otherQuantity = quantity.clone()
        if (thisQuantity.values[0] is Bool) throw BDDError("Division not allowed for BDDs")
        var resultUnit = Unit()
        if (thisQuantity.unit.toString() == "?" || otherQuantity.unit.toString() == "?")
            resultUnit = Unit("?")
        else {
            // negate all exponents of units in VectorQuantity2 because of division
            otherQuantity.unit.negateExponentsOfUnits()
            // add all units of quantity1 to the unitSet of quantity2
            thisQuantity.unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
            otherQuantity.unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
        }

        val resultingValues = when {
            otherQuantity.values.size == 1 -> values.map { it / otherQuantity.values[0] }
            thisQuantity.values.size == otherQuantity.values.size -> {
                //collect all results of all divisions of the different rows
                val results = thisQuantity.values.indices.map { values[it] / otherQuantity.values[it] }
                when (value) { //return as the resulting scalar the value with the minimum and maximum result
                    is Real -> {
                        val min = results.minOf { it.asAadd().min }
                        val max = results.minOf { it.asAadd().max }
                        listOf(value.builder.real(min..max))
                    }
                    is Integer -> {
                        val min = results.minOf { it.asIdd().min }
                        val max = results.minOf { it.asIdd().max }
                        listOf(value.builder.integer(min..max))
                    }
                    else -> emptyList()
                }
            }
            else -> throw VectorDimensionError("It is not possible to multiply vectors of size ${thisQuantity.values.size} and ${otherQuantity.values.size}")
        }
        return VectorQuantity(resultingValues, resultUnit.clone())
    }


    /**
     * Adds quantities.
     * @param quantity is added to the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    operator fun plus(quantity: VectorQuantity): VectorQuantity {
        if (values[0] is Bool) throw BDDError("Addition not allowed for BDDs")
        var resultingUnit = unit
        var resultingUnitSpec = unitSpec
        if (unit.toString() == "?") { //Choose if possible known unit
            resultingUnit = quantity.unit
            resultingUnitSpec = quantity.unitSpec
        }
        val resultingValues = mutableListOf<DD<*>>()
        //Special Case for Addition of 0
        when (values[0]) {
            is Real -> {
                if (values.size == 1 && Range(-1e-14, 1e-14).contains(values[0].asAadd().getRange())) return quantity.clone()
                if (quantity.values.size == 1 && Range(-1e-14, 1e-14).contains(quantity.values[0].asAadd().getRange())) return this.clone()
            }
            is Integer -> {
                if (values.size == 1 && values[0].asIdd().min == 0L && values[0].asIdd().max == 0L) return quantity.clone()
                if (quantity.values.size == 1 && quantity.values[0].asIdd().min == 0L && quantity.values[0].asIdd().max == 0L) return this.clone()
            }
            else -> throw DDError("Unsupported type for Addition: ${values[0]}")
        }
        // Calculate result Value
        if (unit == quantity.unit || unit.toString() == "?" || quantity.unit.toString() == "?") {
            if (values.size == quantity.values.size) // add for vectors only possible with the same dimension
                for (i in values.indices)
                    resultingValues.add(values[i] + quantity.values[i])
            else
                throw VectorDimensionError("It is not possible to add vectors of different size (${values.size} and ${quantity.values.size})")
        } else
            throw AdditionError("${this.unit} and ${quantity.unit}")
        return VectorQuantity(resultingValues, resultingUnit, resultingUnitSpec)
    }

    /**
     * Subtracts quantities.
     * @param quantity is the subtrahend of the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity.
     */
    operator fun minus(quantity: VectorQuantity): VectorQuantity {
        if (values[0] is Bool) throw SemanticError("Subtraction not allowed on BDDs")
        val resultingUnit = if (unit.toString() == "?") quantity.unit else unit
        val resultingUnitSpec = if (unit.toString() == "?") quantity.unitSpec else unitSpec
        val resultingValues = mutableListOf<DD<*>>()
        // Set isDifference of resultUnit to true
        unit.isDifference = true
        if (unit == quantity.unit || unit.toString() == "?" || quantity.unit.toString() == "?") {
            if (values.size == quantity.values.size) {  // add for vectors only possible with the same dimension
                for (i in values.indices) {
                    resultingValues.add(values[i] - quantity.values[i])
                }
            } else {
                throw VectorDimensionError("It is not possible to subtract vectors of different size (${values.size} and ${quantity.values.size})")
            }
        } else {
            throw SubtractionError("${this.unit} and ${quantity.unit}")
        }
        return VectorQuantity(resultingValues, resultingUnit, resultingUnitSpec)
    }


    /**
     * Negates sign of Real or Integer-Valued Variable
     */
    open fun negate(): VectorQuantity {
        val resultingValues = values.map {
            when (it) {
                is Real -> it.negate()
                is Integer -> it.negate()
                else -> throw SemanticError("negate only applicable on values of type Real or Integer")
            }
        }
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /**
     * Infix fun for power
     * @param quantity Exponent for the Pow function
     * @return result of the calculation
     */
    open infix fun pow(quantity: VectorQuantity): VectorQuantity {
        if (quantity.values.size != 1) throw DDError("Power parameter only for Vectors of size one (values)")
        if (values[0] is Bool) throw BDDError("Pow not allowed for BDDs")
        return pow(quantity.values[0])
    }

//--------------Compare operations--------------------------------

    /**
     * Compares quantities with "greater than"
     * For vectors compare the abs values
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun gt(quantity: VectorQuantity): Quantity {
        if (values[0] is Bool) throw BDDError("Greater than not allowed for BDDs")
        if(values.size!=1 || quantity.values.size!=1)
            throw DDError("For > both values should be no Vectors")
        else
            return Quantity(value greaterThan quantity.value)
    }

    /**
     * Compares quantities with "less than"
     * For vectors compare the abs values
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun lt(quantity: VectorQuantity): Quantity {
        if (values[0] is Bool) throw BDDError("Less than not allowed for BDDs")
        if(values.size!=1 || quantity.values.size!=1)
            throw DDError("For < both values should be no Vectors")
        else
            return Quantity(value lessThan quantity.value)
    }

    /**
     * Compares quantities with "greater equals"
     * For vectors compare the abs values
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun ge(quantity: VectorQuantity): Quantity {
        if (values[0] is Bool) throw BDDError("Greater equals not allowed for BDDs")
        if(values.size!=1 || quantity.values.size!=1)
            throw DDError("For >= both values should be no Vectors")
        else
            return Quantity(value greaterThanOrEquals quantity.value)
    }

    /**
     * Compares quantities with "less equals"
     * For vectors compare the abs values
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun le(quantity: VectorQuantity): Quantity {
        if (values[0] is Bool) throw BDDError("Less equals not allowed for BDDs")
        if(values.size!=1 || quantity.values.size!=1)
            throw DDError("For <= both values should be no Vectors")
        else
            return Quantity(value lessThanOrEquals quantity.value)
    }

    /**
     * Compares quantities with "equals"
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun eq(quantity: VectorQuantity): Quantity {
        if(values.size!= quantity.values.size)
            throw DDError("For == both values should be Vectors of the same size")
        if(values[0] is StrDD) {
            var equal = values[0].asStrDD().equalValue(quantity.value.asStrDD())
            for(i in 1 until values.size ){
                if(values[i] != quantity.values[i]){
                    equal = equal.and(values[i].asStrDD().equalValue(quantity.values[i].asStrDD()))
                }
            }
            return Quantity(equal)
        }
        if (values.first() is Bool) { // and is the same as not xor
            var equal = value.asBdd().xor(quantity.value.asBdd()).not()
            for(i in 1 until values.size ){
                if(values[i] != quantity.values[i]){
                    equal = equal.and(values[i].asBdd().xor(quantity.values[i].asBdd()).not())
                }
            }
            return Quantity(equal)
        }
        //start with the first element followed by the rest in the loop
        var equal = (value lessThanOrEquals quantity.value).and(value greaterThanOrEquals quantity.value)
        for(i in 1 until values.size ){
            if(values[i] != quantity.values[i]){
                equal = equal.and((values[i] lessThanOrEquals quantity.values[i]).and(values[i] greaterThanOrEquals quantity.values[i]))
            }
        }
        return Quantity(equal)
    }

    infix fun neq(quality: VectorQuantity): VectorQuantity {
        val equalsValues = (this eq quality).values
        val resultingValues = mutableListOf<Bool>()
        //negate the result of the equals operation
        equalsValues.forEach {
            resultingValues.add(it.asBdd().not())
        }
        return VectorQuantity(resultingValues)
    }
//--------------Miscellaneous operations--------------------------------

    /**
     * The Ceil operation on Real, rounds up to next integer value
     * @return VectorQuantity with Real (Real) type as a new VectorQuantity
     */
    open fun ceil(): VectorQuantity {
        if (values[0] !is Real) throw SemanticError("Ceil must have parameter of type Real")
        val resultingValues = values.toMutableList()
        for (i in resultingValues.indices)
            resultingValues[i] = (resultingValues[i] as Real).ceil()
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /**
     * Floor operation on Real just rounds up to the next integer value
     * @return VectorQuantity with Real (Real) type as a new VectorQuantity
     */
    open fun floor(): VectorQuantity {
        if (values.firstOrNull() !is Real) throw SemanticError("Floor must have parameter of type Real")
        val resultingValues = values.toMutableList()
        for (i in resultingValues.indices)
            resultingValues[i] = (resultingValues[i] as Real).floor()
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /**
     * Applies the square root to a VectorQuantity
     * Example: 100 m^2 --> 10 m
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    open fun sqrt(): VectorQuantity {
        //calculate final sqrt value
        val resultingValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real ->  values.forEach { resultingValues.add(it.asAadd().sqrt())}
            is Integer -> values.forEach {resultingValues.add(it.asIdd().sqrt())}
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
        return VectorQuantity(resultingValues, resUnit)
    }

    /**
     * Applies the square to a VectorQuantity
     * Example: 100 m^2 --> 10 m
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    open fun sqr(): VectorQuantity {
        if (value !is Real && value !is Integer) {
            throw BDDError("Sqr not allowed for any other type than Real or Integer")
        }

        val resultValues = mutableListOf<DD<*>>()
        //calculate final sqrt value
        when (value) {
            is Real -> values.forEach { resultValues.add(it.asAadd().pow(it.builder.real(2.0))) }
            is Integer -> values.forEach { resultValues.add(it.asIdd().sqr()) }
            else -> throw BDDError("Sqr not allowed for any other type than Real or Integer")
        }

        var resUnit = Unit()
        if (unit.toString() == "?")
            resUnit = Unit("?")
        else {
            for (element in unit.unitSet) {
                val currentElement = element.clone()
                currentElement.exponent *= 2
                resUnit.unitSet.add(currentElement)
            }
        }
        return VectorQuantity(resultValues, resUnit)
    }

    /**
     * calculates log base e (ln) of a VectorQuantity
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    open fun ln(): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() !in setOf("1", "?", "dB", "%")) {
            throw SemanticError("Log with units is not allowed")
        }
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real -> values.forEach { finalValues.add((it as Real).log()) }
            is Integer -> values.forEach { finalValues.add((it as Integer).log()) }
            else -> throw SemanticError("Log not allowed for any other type than Real or Integer.")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    /**
     * calculates log of a VectorQuantity with a given base
     * @param base base value for the logarithm
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    open fun log(base: DD<*>): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Log with units is not allowed")
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real -> values.forEach { finalValues.add((it as Real).log() / (base as Real).log()) }
            is Integer -> values.forEach { finalValues.add((it as Integer).log(base as Integer)) }
            else -> throw SemanticError("Log not allowed for any other type than Real or Integer.")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    fun log(bases: VectorQuantity): VectorQuantity {
        val results = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real -> values.indices.forEach { results.add(values[it].asAadd().log() / bases.values[it].asAadd().log())}
            is Integer -> values.indices.forEach { results.add(values[it].asIdd().log() / bases.values[it].asIdd().log()) }
            else -> throw DDError("Log only possible for Integer and Real")
        }
        return VectorQuantity(results, unit, unitSpec)
    }

    /**
     * calculates exp of a VectorQuantity
     * It is used for the following function: f(x) = e^x
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    open fun exp(): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() == "?") throw SemanticError("Exp with units is not allowed")
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real -> values.forEach { finalValues.add((it as Real).exp()) }
            is Integer -> values.forEach { finalValues.add((it as Integer).exp()) }
            else -> throw SemanticError("Exp only possible with Integer and Real")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    /**
     * Calculates Pow2 for Quantities
     * It is used for the following function: f(x) = 2^x
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    open fun pow2(): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() !in setOf("1", "?", "%", "dB")) throw SemanticError("Pow2 with units is not allowed")
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real -> values.forEach { finalValues.add((it as Real).power2()) }
            is Integer -> values.forEach { finalValues.add((it as Integer).power2()) }
            else -> throw SemanticError("Pow2 only possible with Integer and Real")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    /**
     * Calculates Pow for Quantities: f(x,y) = x^y
     * @param exponent Exponent for the Pow function
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    open fun pow(exponent: DD<*>): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() !in setOf("1", "?", "dB", "%"))
            throw SemanticError("Power with units is not allowed")
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real -> values.forEach { finalValues.add((it as Real).pow(exponent.asAadd())) }
            is Integer -> values.forEach { finalValues.add((it as Integer).pow(exponent.asIdd())) }
            else -> throw SemanticError("Power only possible with Integer and Real")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    open fun sin(): VectorQuantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Sin with units is not allowed")
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real ->  values.forEach { finalValues.add((it as Real).sin()) }
            else -> throw SemanticError("Sin only possible with Real")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    open fun arcsin(): VectorQuantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("arcsin with units is not allowed")
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real ->  values.forEach { finalValues.add((it as Real).arcsin()) }
            else -> throw SemanticError("arcsin only possible with Real")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    open fun cos(): VectorQuantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Cos with units is not allowed")
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real ->  values.forEach { finalValues.add((it as Real).cos()) }
            else -> throw SemanticError("Cos only possible with Real")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    open fun arccos(): VectorQuantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("arccos with units is not allowed")
        val finalValues = mutableListOf<DD<*>>()
        when (values[0]) {
            is Real ->  values.forEach { finalValues.add((it as Real).arccos()) }
            else -> throw SemanticError("arccos only possible with Real")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    /**
     * Calculates the angle between two vectors of the same size.
     * @param other vector for angle calculation
     * @return Angle as a Quantity with Real value in radiant representation
     */
    open fun angle(other: VectorQuantity): Quantity {
        if (values.size != other.values.size)
            throw DDError("For the angle calculation vectors must have the same size, not ${values.size} and ${other.values.size}")

        val quantity1 = if (value is Integer) intAsAADDQuantity() else this
        val quantity2 = if (other.value is Integer) other.intAsAADDQuantity() else other

        if (quantity1.value !is Real || quantity2.value !is Real)
            throw DDError("Vector operations are only supported for Integer and Real")

        val division = quantity1.dot(quantity2) / (quantity1.abs() * quantity2.abs())
        return Quantity(division.aadd().arccos(), "rad")
    }

    private fun intAsAADDQuantity(): VectorQuantity {
        if(values[0] !is Integer) throw DDError("Only possible for Integer")
        val resultingValues = mutableListOf<DD<*>>()
        for (i in values.indices)
            resultingValues.add(values[i].builder.real(values[i].asIdd().min.toDouble()..values[i].asIdd().max.toDouble()))
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    open fun norm(): VectorQuantity {
        if (values[0] is Integer)
            throw DDError("Normalizing vectors with Integers is not supported")
        return this.div(abs())
    }

    /**
     * Transforms the VectorQuantity to a String with the unit as a value and a fraction of units
     * If the VectorQuantity contains a unitSpec, the unit is transformed to this VectorQuantity before returning the string.
     * @Return a string representation of the VectorQuantity
     */
override fun toString(): String {
    if (values.size == 1) return Quantity(values[0], unit, unitSpec).toString()

    val resultingString = StringBuilder("(")
    when (values[0]) {
        is Integer -> {
            values.joinTo(resultingString, ", ") {
                val value = it.asIdd().getRange()
                val minIsInf = value.min == Long.MIN_VALUE || value.min <= -2147483647L
                val maxIsInf = value.max == Long.MAX_VALUE || value.max >= 2147483647L
                when {
                    minIsInf && maxIsInf -> "*..*"
                    value.min == value.max -> value.min.toString()
                    value.min > value.max -> "∅"
                    else -> {
                        val min = if (minIsInf) "*" else value.min.toString()
                        val max = if (maxIsInf) "*" else value.max.toString()
                        "$min..$max"
                    }
                }
            }
            resultingString.append(")")
        }
        is Bool -> {
            values.joinTo(resultingString, ", ") { it.asBdd().toString() }
            resultingString.append(")")
        }
        is StrDD -> {
            values.joinTo(resultingString, ", ") { it.asStrDD().toString() }
            resultingString.append(")")
        }
        is Real -> {
            var transformedUnitString = ""
            values.joinTo(resultingString, ", ") {
                val transformedValue: DD<*>
                if (unitSpec.isNotEmpty()) {
                    transformedValue = Quantity(it, unit).valueIn(unitSpec)
                    transformedUnitString = unitSpec
                } else if (unit.toString() == "?") {
                    transformedValue = it
                    transformedUnitString = unit.toString()
                } else if (unit.calculatedUnitSymbol.isNotEmpty()) {
                    val unitSymbol = unit.calculatedUnitSymbol
                    val quantityCalc = Quantity(it, Unit(unitSymbol))
                    val bestSolution = ConversionTables.prefixes
                        .filter { prefix -> prefix.key.isEmpty() || prefix.key.last() != 'i' }
                        .maxByOrNull { prefix ->
                            val valueInPrefix = quantityCalc.valueIn(prefix.key + unitSymbol)
                            val min = valueInPrefix.asAadd().min
                            val max = valueInPrefix.asAadd().max
                            if ((min * 1.0001 >= 1 || max < 0e-24) && abs(max / min) <= 10.0.pow(24)) prefix.value.factor else Double.MIN_VALUE
                        }?.value ?: NoPrefix
                    transformedUnitString = when {
                        unitSymbol == "m^2" && bestSolution == Hecto -> "ha"
                        unitSymbol == "m^3" && bestSolution == Deci -> "l"
                        unitSymbol == "m" && (bestSolution == Centi || bestSolution == Deci) -> "cm"
                        bestSolution.symbol !in listOf("d", "c", "da", "h") || unitSymbol in listOf("m^2", "m^3") -> bestSolution.symbol + unitSymbol
                        else -> unitSymbol
                    }
                    transformedValue = quantityCalc.valueIn(transformedUnitString)
                } else {
                    transformedValue = it
                    transformedUnitString = unit.toString()
                }
                Representer().represent(transformedValue.asAadd())
            }
            resultingString.append(")")
            if (transformedUnitString != "1") resultingString.append(" $transformedUnitString")
        }
        else -> throw DDError("Unsupported value type for toString: $values")
    }
    return resultingString.toString()
}

    /**
     * Converts a timestamp to a DateTime string
     */
    open fun timeToString(timestamp: Double): String {
        if (timestamp.isFinite())
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.toLong()), UTC).toString()
        return "Infinity"
    }

    /**
     * Converts a timestamp to a date string
     */
    open fun dateToString(timestamp: Double): String {
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
    open fun monthToString(timestamp: Double): String {
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
    open fun yearToString(timestamp: Double): String {
        if (timestamp.isFinite()) {
            var result = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp.toLong()), UTC)
            //round month to next year if month is after 7, because it is used only at start of year
            if (result.month.value >= 7) result = result.plusYears(1)
            val date = result.format(DateTimeFormatter.ISO_LOCAL_DATE)
            return date.substring(0..3) //remove the day and month, because only the year should be considered
        }
        return "Infinity"
    }

    /**
     * return first value
     */
    open fun bdd(): Bool = values[0] as Bool
    open fun aadd(): Real = values[0] as Real
    open fun idd(): Integer = values[0] as Integer

    /**
     * return all values
     */
    open fun bdds(): MutableList<Bool> = values as MutableList<Bool>
    open fun aadds(): MutableList<Real> = values as MutableList<Real>
    open fun idds(): MutableList<Integer> = values as MutableList<Integer>

    /**
     * Converts this unit to the expected unit representation and returns the value of the conversion
     * @param wantedRepresentation String of the wanted representation of the Unit
     * @Return value in Real/Integer of the result
     */
    open fun valuesIn(wantedRepresentation: String): List<DD<*>> {
        val expectedUnit = Unit(wantedRepresentation)
        if (expectedUnit.toString() == "1" || unit.toString() == "?") return values

        return values.map {
            when {
                expectedUnit.isLogarithmic -> { // Logarithmic quantity is transformed to not logarithmic VectorQuantity
                    val ten = it.builder.real(10.0)
                    ten * it.asAadd().log() / ten.log()
                }
                //Special case for temperature to temperature conversion From K to °C/°F
                unit.unitSet.isNotEmpty() && unit.unitSet.first() is ThermodynamicTemperature &&
                expectedUnit.unitSet.isNotEmpty() && expectedUnit.unitSet.first() is ThermodynamicTemperature -> {
                    val unit1 = unit.unitSet.first() as ThermodynamicTemperature
                    val unit2 = expectedUnit.unitSet.first() as ThermodynamicTemperature
                    if (unit2.name != "kelvin") unit1.convertTo(it * unit1.prefix.factor, unit2) else it
                }
                else -> {
                    //1) Make expected unit canonical and calculate correlationFac
                    val temporaryExpected = Quantity(it.builder.real(1.0), expectedUnit.clone()) //also calculates toSI()
                    val correlationFac = temporaryExpected.value
                    //2) Compare them
                    if (unit == temporaryExpected.unit) it.div(correlationFac)
                    else throw TransformationError("$expectedUnit and $unit")
                }
            }
        }
    }
    /**
     * Removes all Prefixes from a VectorQuantity and updates the values
     */
    private fun removePrefixes() {
        var factor = 1.0
        for (element in unit.unitSet) {
            factor *= element.prefix.factor.pow(element.exponent)
            element.prefix = NoPrefix
        }
        val resultingValues = mutableListOf<DD<*>>()
        if (unit.unitSet.isNotEmpty()) {
            if (factor != 1.0)
                values.forEach { resultingValues.add(it * factor) }
            else
                values.forEach { resultingValues.add(it) }
            values = resultingValues
        }
    }

    /**
     * Transforms VectorQuantity to the SI System
     */
    private fun toSI() {
        removePrefixes()
        val resultingValues = values.toMutableList()
        val resultUnit = unit.clone()
        if (unit.isLogarithmic) { // Logarithmic quantity is transformed to not logarithmic
            for (i in values.indices) {
                val ten = values[i].builder.real(10.0)
                resultingValues[i] = ten.power(resultingValues[i].asAadd() / ten)
            }
            unit.isLogarithmic = false
        } else {
            resultUnit.unitSet = mutableSetOf() // make resultSet empty
            for (currentUnit in unit.unitSet) {
                // Change Unit to SI
                (currentUnit.getBaseUnits()).forEach {
                    val newUnitElement = it.clone()
                    //change exponent of derived unit
                    newUnitElement.exponent = currentUnit.exponent * it.exponent
                    resultUnit.addUnitOfMeasurement(newUnitElement)
                }
                // Update values
                for (i in values.indices) {
                    if (currentUnit is ThermodynamicTemperature) resultingValues[i] = currentUnit.toKelvin(resultingValues[i])
                    if (currentUnit.convFac.pow(currentUnit.exponent) != 1.0)
                        resultingValues[i] = resultingValues[i] * currentUnit.convFac.pow(currentUnit.exponent)
                }
            }
        }
        //make unit canonical
        values = resultingValues
        unit = resultUnit
        unit.reduceRedundantUnits()
    }

    open fun getDomain(): String {
        return unit.getUnitDomain(values[0].asAadd().getRange().min)
    }

    /**
     * Intersects a VectorQuantity with another VectorQuantity of the same property
     * (e.g., upVectorQuantity with downVectorQuantity)
     * @param q Intersect the current VectorQuantity with this VectorQuantity
     * @return Intersected VectorQuantity as a new VectorQuantity
     **/
    fun intersect(q: VectorQuantity): VectorQuantity {
        if (unit.toString() == "?") return this.clone()
        // Do intersection for REAL or INT
        val resultingValues = mutableListOf<DD<*>>()
        if (values.size != q.values.size) {
            throw VectorDimensionError("Not possible to intersect VectorQuantities of different size (${values.size} and ${q.values.size})")
        }
        for (i in values.indices) {
            when (values[i]) {
                is Real -> resultingValues.add(values[i].asAadd() intersect q.values[i].asAadd())
                is Integer -> resultingValues.add(values[i].asIdd() intersect q.values[i].asIdd())
                is Bool -> resultingValues.add(values[i].asBdd() intersect q.values[i].asBdd())
                else -> throw DDError("Intersection only possible for Integer, Bool, and ADD")
            }
        }
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    open fun constrainString(stringSpecs: List<String>): VectorQuantity {
        val resultingValues = mutableListOf<DD<*>>()
        if(stringSpecs.isEmpty())
            return this
        if (values.size == stringSpecs.size)
            values.indices.forEach {
                if(values[it].asStrDD().toString()=="")
                    resultingValues.add(values[it].builder.string(stringSpecs[it]))
                else if (stringSpecs[it] == "String")
                    resultingValues.add(values[it])
                else if(stringSpecs[it] == values[it].asStrDD().toString())
                    resultingValues.add(values[it])
                else
                    resultingValues.add(values[it].builder.string(""))
            }
        else
            if (stringSpecs.size == 1 && stringSpecs[0] == "String") // Special case, if there is no definition of boolSpec, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asStrDD()) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${stringSpecs.size}")
        return VectorQuantity(resultingValues)
    }

    open fun constrainString(quantity: VectorQuantity): VectorQuantity {
        val resultingValues = mutableListOf<DD<*>>()
        if (values.size == quantity.values.size)
            values.indices.forEach {
                if(values[it].asStrDD().toString()=="")
                    resultingValues.add(values[it].builder.string(quantity.values[it].toString()))
                else if (quantity.values[it].toString() == "String")
                    resultingValues.add(values[it])
                else if(quantity.values[it].toString() == quantity.values[it].toString())
                    resultingValues.add(values[it])
                else
                    resultingValues.add(values[it].builder.string(""))
            }
        else
            if (quantity.values.size == 1 && quantity.values[0].toString() == "String") // Special case, if there is no definition of boolSpec, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asStrDD()) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${quantity.values.size}")
        return VectorQuantity(resultingValues)
    }

    /**
     * Constrain a VectorQuantity with the interval of this property (intSpec)
     * @param intSpecs the current VectorQuantity should be constrained to these intervals
     * @return Constrained VectorQuantity as a new VectorQuantity
     **/
    open fun constrain(intSpecs: MutableList<IntegerRange>): VectorQuantity {
        val resultingValues = mutableListOf<Integer>()
        if (values.size == intSpecs.size)
            values.indices.forEach { resultingValues.add(values[it].asIdd() intersect values[it].builder.integer(intSpecs[it])) }
        else
            if (intSpecs.size == 1 && intSpecs[0] == IntegerRange.Integers) // Special case, if there is no definition of boolSpec, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asIdd()) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${intSpecs.size}")

        return VectorQuantity(resultingValues)
    }

    /**
     * Constrain a VectorQuantity with the interval of this valueFeature (only for Real)
     * TODO: define strategy that ensures that intersect in evalUp/Down does not introduce
     *  arbitrary many comparisons and hence growing size of Bool/Real
     *  TODO: Remove checks for NaN or Empty value if FIX for division by 0 during evalDown ready
     * @param q The VectorQuantity which should be constrained to
     * @param rangeSpecs the specified range
     * @param unitSpec the wanted representation of the Unit
     * @return Constrained VectorQuantity as a new VectorQuantity
     **/
    fun constrain(q: VectorQuantity, rangeSpecs: List<Range>, unitSpec: String): VectorQuantity {
        if (values.size != q.values.size)
            throw VectorDimensionError("Not possible to intersect VectorQuantities of different size (${values.size} and ${q.values.size})")

        if (unitSpec != "" && unit != Quantity(value, Unit(unitSpec)).unit)
            throw TransformationError("$unit cannot be transferred to ${Unit(unitSpec)}")

        // calculate intersection of propagated and specified values
        val resultingValues = mutableListOf<DD<*>>()
        if (values.size == rangeSpecs.size)
            values.indices.forEach {
                resultingValues.add(values[it].asAadd() constrainTo Quantity(value.builder.real(rangeSpecs[it]), Unit(unitSpec)).getRange())
            }
        else
            if (rangeSpecs.size == 1 && rangeSpecs[0] == Range.Reals) // Special case, if there is no definition of rangeSpecs, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asAadd()) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${rangeSpecs.size}")

        for (i in values.indices) {
            if (q.values[i].isFeasible && !(q.values[i] as Real).isEmpty()) {
                resultingValues[i] = resultingValues[i].asAadd() constrainTo q.values[i].asAadd()
            }
        }
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /**
     * Constrain function for two Quantities with the same unit (normally SI), for Integer and Real
     */
    fun constrain(q: VectorQuantity): VectorQuantity {
        // only constrain to newQ if not infinite and not empty
        val resultingValues = values.toMutableList()
        when (q.values[0]) {
            is Real -> {
                for (i in values.indices) {
                    resultingValues[i] = (values[i].asAadd() constrainTo (q.values[i] as Real).getRange())
                }
            }

            is Integer -> {
                for (i in values.indices) {
                    resultingValues[i] = (values[i].asIdd() constrainTo (q.values[i] as Integer).getRange())
                }
            }

            else -> throw SemanticError("Constrain only for Integer and Real")
        }
        if(unit.toString()=="?")
            unit = q.unit
        // Iff one of the results was NaN, continue with the other (???)
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /**
     * Returns a new quantity constrained to spec.
     * @param constraints: the constraints to be applied.
     * @param isBoolean Shows that the constraint function should work with boolean. Needed for compiler to separate it from Integer constrain
     * @return a new quantity that is q, constrained to constraint.
     */
    open fun constrain(constraints: MutableList<XBool>, isBoolean: Boolean): VectorQuantity {
        val resultingValues = mutableListOf<Bool>()
        if (values.size == constraints.size)
            values.indices.forEach { resultingValues.add(values[it].asBdd() intersect values[it].builder.constant(constraints[it])) }
        else
            if (constraints.size == 1 && constraints[0] == XBool.X) // Special case, if there is no definition of boolSpec, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asBdd() intersect values[0].builder.constant(constraints[0])) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${constraints.size}")

        return VectorQuantity(resultingValues)
    }

    /**
     * Compares a quantity with another object.
     * @param other the other object
     * @return true, if equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VectorQuantity) return false

        if (unit.toString() != other.unit.toString()) return false
        for (i in values.indices) { //iterate through all element pairs
            when (value) {
                is Real -> {
                    val min1 = values[i].asAadd().getRange().min
                    val max1 = values[i].asAadd().getRange().max
                    val min2 = other.values[i].asAadd().getRange().min
                    val max2 = other.values[i].asAadd().getRange().max
                    //if the difference is too big, they are different
                    if (abs(min1 - min2) > abs(min1) * 0.0001) return false
                    if (abs(max1 - max2) > abs(max1) * 0.0001) return false
                }

                is Integer -> {
                    val min1 = values[i].asIdd().getRange().min
                    val max1 = values[i].asIdd().getRange().max
                    val min2 = other.values[i].asIdd().getRange().min
                    val max2 = other.values[i].asIdd().getRange().max
                    // For infinite values compare borders
                    if (min1 == values[i].builder.Integers.min || min1 == values[i].builder.Integers.max)
                        return min1 == min2 && max1 == max2
                    //if the difference is too big, they are different
                    if (abs(min1 - min2) > abs(min1) * 0.0001) return false
                    if (abs(max1 - max2) > abs(max1) * 0.0001) return false
                }
                // No units ...
                is Bool,
                is StrDD -> return values.toString() == other.values.toString()

                else -> throw DDError("Unsupported value type for equals: $values")
            }
        }
        return true
    }

    fun contains(other: VectorQuantity): Boolean {
        if (values.size != other.values.size)
            throw VectorDimensionError("Not possible to compare VectorQuantities of different size (${values.size} and ${other.values.size})")
        for (i in values.indices) {
            when (values[i]) {
                is Real ->
                    values[i].asAadd().contains(other.values[i].asAadd())
                is Integer -> {
                    if (!values[i].asIdd().contains(other.values[i].asIdd())) return false
                }
                else -> throw DDError("Unsupported value type for contains, only Real and Integer are supported")
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = values.hashCode()
        result = 31 * result + unit.hashCode()
        result = 31 * result + unitSpec.hashCode()
        return result
    }

    /**
     * Length of vector
     * @return Quantity with length and unit of vector
     */
    open fun abs(): Quantity {
        return when (values[0]) {
            is Integer -> {
                var squareSum = values[0].builder.integer(0)
                values.forEach {
                    val min = it.asIdd().min
                    val max = it.asIdd().max
                    val absoluteValue = if (min <= 0 && max >= 0)
                        it.builder.integer(0..max(abs(min), max))
                    else if (max < 0)
                        it.builder.integer(abs(max)..abs(min))
                    else
                        it.builder.integer(min..max)
                    squareSum += absoluteValue.sqr()
                }
                Quantity(squareSum.sqrt())
            }

            is Real -> {
                var squareSum = values[0].builder.real(0.0)
                values.forEach {
                    val min = it.asAadd().min
                    val max = it.asAadd().max
                    val absoluteValue = if (min <= 0.0 && max >= 0.0)
                        it.builder.real(0.0..max(abs(min), max))
                    else if (max < 0.0)
                        it.builder.real(abs(max)..abs(min))
                    else
                        it.builder.real(min..max)
                    squareSum += absoluteValue.sqr()
                }
                if (squareSum.min in -0.000001..0.000001) {
                    // if the result is close to zero and negative, make it zero,
                    // because sqrt of negative values is not possible
                    squareSum = values[0].builder.real(0.0..squareSum.max)
                }
                Quantity(squareSum.sqrt(), unit)
            }

            else -> throw DDError("Unsupported value for abs: $values")
        }
    }

    open fun size(): Quantity {
        return Quantity(value.builder.integer(values.size.toLong()))
    }

    /**
     * Length of vector using the city block distance (Manhattan Distance)
     * @return Quantity with length and unit of vector
     */
    open fun cityBlockDistance(param: VectorQuantity): Quantity {
        return when (values[0]) {
            is Integer -> {
                val connectingVector = this - param
                var sum = values[0].builder.integer(0)
                connectingVector.values.forEach {
                    val min = it.asIdd().min
                    val max = it.asIdd().max
                    val absoluteValue = if(min<=0 && max>=0)
                        it.builder.integer(0..max(abs(min),max))
                    else if (max<0)
                        it.builder.integer(abs(max)..abs(min))
                    else
                        it.builder.integer(min..max)
                    sum += absoluteValue
                }
                Quantity(sum)
            }

            is Real -> {
                val connectingVector = this - param
                var sum = values[0].builder.real(0.0)
                connectingVector.values.forEach {
                    val min = it.asAadd().min
                    val max = it.asAadd().max
                    val absoluteValue = if(min<=0.0 && max>=0.0)
                        it.builder.real(0.0..max(abs(min),max))
                    else if (max<0.0)
                        it.builder.real(abs(max)..abs(min))
                    else
                        it.builder.real(min..max)
                    sum += absoluteValue
                }// if result is close to zero make it zero, because sqrt of negative values is not possible
                if (sum.min in -0.000001..0.000001) sum = values[0].builder.real(0.0..sum.max)
                Quantity(sum, unit)
            }

            else -> throw DDError("Unsupported value for cityBlockDistance: $values")
        }
    }

    //--------------Boolean operations--------------------------------

    /**
     * Applies boolean "and" operation
     * @return VectorQuantity with the with result as Bool as a new VectorQuantity
     */
    infix fun and(quantity: VectorQuantity): VectorQuantity {
        if (values[0] !is Bool) throw BDDError("Boolean \"and\" can only be applied to BDDs")
        val results = mutableListOf<Bool>()
        values.indices.forEach { results.add(values[it].asBdd() and quantity.values[it].asBdd()) }
        return VectorQuantity(results)
    }

    /**
     * Applies boolean "or" operation
     * @return VectorQuantity with the with result as a new VectorQuantity
     */
    infix fun or(quantity: VectorQuantity): VectorQuantity {
        if (values[0] !is Bool) throw BDDError("Boolean \"or\" can only be applied to BDDs")
        val results = mutableListOf<Bool>()
        values.indices.forEach { results.add(values[it].asBdd() or quantity.values[it].asBdd()) }
        return VectorQuantity(results)
    }

    fun asQuantity(): Quantity {
        if (values.size != 1)
            throw VectorDimensionError("Transform to Quantity only possible for Vectors of size 1, not of size ${values.size}")
        return Quantity(values[0], unit, unitSpec)
    }

    /**
     * @return the min value of the Range as Double of the first value
     */
    open fun getMinAsDouble(): Double {
        if (value is Bool) throw BDDError("No min value for BDDs")
        if (value is Real) return aadd().getRange().min
        if (value is Integer) return idd().getRange().min.toDouble()
        throw SemanticError("Expect either Real or Integer")
    }

    /**
     * @return the max value of the Range as Double of the first value
     */
    open fun getMaxAsDouble(): Double {
        if (value is Bool) throw BDDError("No max value for BDDs")
        if (value is Real) return aadd().getRange().max
        if (value is Integer) return idd().getRange().max.toDouble()
        throw SemanticError("Expect either Real or Integer")
    }

    /**
     * Checks if one of the values has been constrained from its base type's value.
     * @return true if one of the values of the vector is not the base type's maximum range.
     * Strings are considered generally as false.
     */
    fun isConstrained(): Boolean {
        values.forEach {
            if (it is Real)
                return true
            if (it is Integer)
                return true
            if (it is Bool && it.value != XBool.X)
                return true
            if (it is StrDD)
                return true
        }
        return false
    }
}

/**
 * Returns the maximum of two quantities a, b.
 */
fun max(a: VectorQuantity, b: VectorQuantity): VectorQuantity{
    if (a.values.size > 1 || b.values.size > 1)
        throw VectorDimensionError("max only possible for Vectors of size 1, not of size ${a.values.size} and ${b.values.size}")
    val af = a.ge(b).bdd().ite(a, b)
    val max = max(a.getMaxAsDouble(), b.getMaxAsDouble())
    val min = max(a.getMinAsDouble(), b.getMinAsDouble())
    val result = when (af.value) {
        is Real -> Quantity(af.aadd().constrainTo(Range(min, max)), af.unit)
        is Integer -> Quantity(af.idd().constrainTo(IntegerRange(min, max)))
        else -> throw SemanticError("Expect parameters of max to be Real or Integer.")
    }
    return result
}

/**
 * Returns the minimum of two quantities a, b.
 */
fun min(a: VectorQuantity, b: VectorQuantity): VectorQuantity {
    if(a.values.size > 1 || b.values.size > 1)
        throw VectorDimensionError("min only possible for Vectors of size 1, not of size ${a.values.size} and ${b.values.size}")
    val af = a.ge(b).bdd().ite(b, a)
    val max = min(a.getMaxAsDouble(), b.getMaxAsDouble())
    val min = min(a.getMinAsDouble(), b.getMinAsDouble())
    if (af.value is Real)
        return Quantity(af.aadd().constrainTo(Range(min, max)), af.unit)
    else if (af.value is Integer)
        return Quantity(af.idd().constrainTo(IntegerRange(min, max)))
    throw SemanticError("Expect parameters of min to be Real or Integer.")
}

fun Bool.ite(t: VectorQuantity, e: VectorQuantity): VectorQuantity {
    val results = mutableListOf<DD<*>>()
    t.values.indices.forEach { results.add(this.ite(t.values[it], e.values[it])) }
    return VectorQuantity(results, t.unit, t.unitSpec)
}
