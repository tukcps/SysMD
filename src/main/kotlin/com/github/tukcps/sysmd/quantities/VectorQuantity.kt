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
import kotlin.math.*

/**
 * A quantity that consists of a value that is represented by a DD instance, and
 * a unit that is represented by SI units fraction. The unit is transformed to SI, so that
 * calculations are more efficient
 */
@Suppress("UNCHECKED_CAST")
open class VectorQuantity : Cloneable {

    var values: List<DD>
    var unit: Unit
    var unitSpec: String = ""
    open val value: DD  // Returns first value as DD
        get() = values[0]

    constructor(value: BDD) {
        this.values = listOf<DD>(value.clone())
        this.unit = Unit("")
    }

    constructor(values: List<DD>) {
        if (values.isEmpty())
            throw DDError(msg = "Empty values for VectorQuantity is not supported")
        val type = values[0]::class.qualifiedName
        values.forEach { if (it::class.qualifiedName != type)
            throw DDError("Different value types in vector are not supported") }
        this.values = values.toList()
        this.unit = Unit("")
    }

    constructor(value: IDD) {
        this.values = listOf<DD>(value.clone())
        this.unit = Unit("")
    }

    constructor(value: StrDD) {
        this.values = listOf<DD>(value.clone())
        this.unit = Unit("")
    }

    /**
     * Constructor only for AADD
     * @param value Value of the VectorQuantity represented as a AADD, so that possible errors are considered
     * @param unitString String representation of the Unit
     */
    constructor(value: AADD, unitString: String) {
        this.values = listOf<DD>(value.clone())
        unitSpec = unitString // use unitStr as unitSpec
        this.unit = Unit(unitString)
        makeCanonical()
    }

    constructor(values: List<AADD>, unitString: String) {
        if (values.isEmpty()) throw DDError(msg = "Empty value for VectorQuantity is not supported")
        this.values = values.toList()
        unitSpec = unitString // use unitStr as unitSpec
        this.unit = Unit(unitString)
        makeCanonical()
    }

    /**
     * Constructor
     * @param value Value of the VectorQuantity represented as a DD, so that possible errors are considered
     * @param unitObject Unit, which should be added to the new VectorQuantity
     * @param unitSpec The wanted representation of the Unit, toString converts the Unit to this representation
     */
    constructor(value: DD, unitObject: Unit, unitSpec: String = "") {
        this.values = listOf<DD>(value.clone())
        this.unit = unitObject.clone()
        this.unitSpec = unitSpec
        makeCanonical()
    }

    constructor(values: List<DD>, unitObject: Unit, unitSpec: String = "") {
        if (values.isEmpty()) throw DDError(msg = "Empty value for VectorQuantity is not supported")
        val type = values[0]::class.qualifiedName
        values.forEach { if (it::class.qualifiedName != type) throw DDError("Different value types in vector are not supported") }
        this.values = values.toList()
        this.unit = unitObject.clone()
        this.unitSpec = unitSpec
        makeCanonical()
    }

    /** Makes a perfect clone of a unit with new references of all objects */
    public override fun clone(): VectorQuantity = VectorQuantity(values.toList(), unit.clone(), unitSpec.plus(""))

    /**
     *  Transforms the Unit to a canonical SI representation with the right UnitDimension
     */
    private fun makeCanonical() {
        toSI()
        unit.reduceRedundantUnits()
        unit.calculateUnitDimension(unitSpec)
        unit.calculateUnitSymbol(unitSpec)
    }

    /**
     * Retrieves the Quantity at the specified position in the vector.
     * @param position The position of the Quantity to retrieve.
     * @return The Quantity at the specified position.
     * @throws VectorDimensionError if the position is out of bounds.
     */
    fun getQuantityAtPosition(position: IntegerRange):VectorQuantity{
        return if(position.min in values.indices && position.max in values.indices)
            VectorQuantity(values.subList(position.min.toInt(),position.max.toInt()+1),unit,unitSpec)
        //negative indices starting at the end of the Vector
        else if(values.size+position.min in values.indices && values.size+position.max in values.indices)
            VectorQuantity(values.subList(position.min.toInt()+values.size,position.max.toInt()+values.size),unit,unitSpec)
        else if(values.size==1 && (value.toString()=="Real" || value.toString()=="Integer"))
            return VectorQuantity(value,unit,unitSpec)
        else
            throw VectorDimensionError("Vector index out of bounds")
    }


    //--------------Arithmetic operations--------------------------------

    /**
     * Scalar multiplication of vector and scalar or scalar and scalar
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting AADD/IDD value and Unit as a new VectorQuantity
     */
    operator fun times(quantity: VectorQuantity): VectorQuantity {
        val thisQuantity = clone()
        val otherQuantity = quantity.clone()
        if (thisQuantity.values[0] is BDD) {
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
        // value calculations
        val resultingValues = mutableListOf<DD>()
        if (thisQuantity.values.size == 1) //left scalar multiplication
            for (value in otherQuantity.values)
                resultingValues.add(values[0] * value)
        else if (otherQuantity.values.size == 1) //right scalar multiplication
            for (value in thisQuantity.values)
                resultingValues.add(value * otherQuantity.values[0])
        else
            throw VectorDimensionError("It is not possible to multiply vectors of size ${values.size} and ${quantity.values.size}. For scalar multiplication use 'dot' insteadt of '*'")
        return VectorQuantity(resultingValues, resultUnit.clone())
    }

    /**
     * Multiplies quantities with dot product
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting AADD/IDD value and Unit as a new VectorQuantity
     */
    infix fun dot(quantity: VectorQuantity): Quantity {
        if (values[0] is BDD) {
            throw BDDError("Multiplication not allowed for BDDs")
        }
        val resultUnit = if (unit.toString() == "?" || quantity.unit.toString() == "?")
            Unit("?")
        else Unit("1")
        // value calculations
        if (values.size == quantity.values.size) {  //Only defined for same length
            var resultingValue: DD = when (values[0]) { //init sum with zero
                is AADD -> values[0].builder.scalar(0.0)
                is IDD -> values[0].builder.scalar(0)
                else -> throw DDError("Wrong type for dot product")
            }
            for (i in values.indices)
                resultingValue = resultingValue.plus(values[i] * quantity.values[i])
            return Quantity(resultingValue, resultUnit.clone())
        } else
            throw VectorDimensionError("Dot  product is not defined for vectors of size ${values.size} and ${quantity.values.size}")
    }

    /**
     * Multiplies quantities with cross product (only possible for vectors with size 3)
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting AADD/IDD value and Unit as a new VectorQuantity
     */
    infix fun cross(quantity: VectorQuantity): VectorQuantity {
        if (values[0] is BDD) throw BDDError("Multiplication not allowed for BDDs")
        var resultUnit = Unit()
        if (unit.toString() == "?" || quantity.unit.toString() == "?") resultUnit = Unit("?")
        else {
            // add all units of quantity1 and quantity2 to the unitSet of resultUnit
            unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
            quantity.unit.unitSet.forEach { resultUnit.addUnitOfMeasurement(it) }
        }
        if (values.size != quantity.values.size)
            throw VectorDimensionError("Cross product is not defined for vectors of size ${values.size} and ${quantity.values.size}")
        if (values.size != 3)
            throw VectorDimensionError("Cross product is only defined for vectors of size 3, not of size ${values.size}.")
        val resultingValues = mutableListOf<DD>()
        //Cross product calculation
        resultingValues.add(values[1] * quantity.values[2] - values[2] * quantity.values[1])
        resultingValues.add(values[2] * quantity.values[0] - values[0] * quantity.values[2])
        resultingValues.add(values[0] * quantity.values[1] - values[1] * quantity.values[0])
        return VectorQuantity(resultingValues, resultUnit)
    }

    /**
     * Divides quantities elementwise
     * @param quantity is the divisor of the current VectorQuantity
     * @return VectorQuantity with resulting AADD/IDD value and Unit as a new VectorQuantity
     */
    operator fun div(quantity: VectorQuantity): VectorQuantity {
        val thisQuantity = clone()
        val otherQuantity = quantity.clone()
        if (thisQuantity.values[0] is BDD) throw BDDError("Division not allowed for BDDs")
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
        // value calculations
        val resultingValues = mutableListOf<DD>()

        if (otherQuantity.values.size == 1) //right scalar division
            for (value in values)
                resultingValues.add(value / otherQuantity.values[0])
        else if (thisQuantity.values.size == otherQuantity.values.size) {  //downwards evaluation of scalar multiplication (vec/vec=scalar)
            //collect all results of all divisions of the different rows
            val results = mutableListOf<DD>()
            for (i in thisQuantity.values.indices) {
                val result = values[i] / otherQuantity.values[i]
                if (result is AADD && !result.isNaN() || result is IDD && !result.isNaN())
                    results.add(result)
            }
            //return as the resulting scalar the value with the minimum and maximum result
            when(value){
                is AADD -> {
                    val min =  results.minOf { it.asAadd().min}
                    val max =  results.minOf { it.asAadd().max}
                    resultingValues.add(value.builder.range(min,max))
                }
                is IDD -> {
                    val min = results.minOf { it.asIdd().min }
                    val max = results.minOf { it.asIdd().max }
                    resultingValues.add(value.builder.range(min,max))
                }
                else -> {}
            }
        }
        else
            throw VectorDimensionError("It is not possible to multiply vectors of size ${thisQuantity.values.size} and ${otherQuantity.values.size}")
        return VectorQuantity(resultingValues, resultUnit.clone())
    }


    /**
     * Adds quantities.
     * @param quantity is added to the current VectorQuantity
     * @return VectorQuantity with resulting AADD/IDD value and Unit as a new VectorQuantity
     */
    operator fun plus(quantity: VectorQuantity): VectorQuantity {
        if (values[0] is BDD) throw BDDError("Addition not allowed for BDDs")
        var resultingUnit = unit
        var resultingUnitSpec = unitSpec
        if (unit.toString() == "?") { //Choose if possible known unit
            resultingUnit = quantity.unit
            resultingUnitSpec = quantity.unitSpec
        }
        val resultingValues = mutableListOf<DD>()
        //Special Case for Addition of 0
        when (values[0]) {
            is AADD -> {
                if (values.size == 1 && Range(-0.0001, 0.0001).contains(values[0].asAadd().getRange())) return quantity.clone()
                if (quantity.values.size == 1 && Range(-0.0001, 0.0001).contains(quantity.values[0].asAadd().getRange())) return this.clone()
            }
            is IDD -> {
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
     * @return VectorQuantity with resulting AADD/IDD value and Unit as a new VectorQuantity.
     */
    operator fun minus(quantity: VectorQuantity): VectorQuantity {
        if (values[0] is BDD) throw SemanticError("Subtraction not allowed on BDDs")
        var resultingUnit = unit
        var resultingUnitSpec = unitSpec
        if (unit.toString() == "?") { //Choose if possible known unit
            resultingUnit = quantity.unit
            resultingUnitSpec = quantity.unitSpec
        }
        val resultingValues = mutableListOf<DD>()
        // Set isDifference of resultUnit to true
        unit.isDifference = true
        if (unit == quantity.unit || unit.toString() == "?" || quantity.unit.toString() == "?") {
            if (values.size == quantity.values.size) // add for vectors only possible with the same dimension
                for (i in values.indices)
                    resultingValues.add(values[i] - quantity.values[i])
            else
                throw VectorDimensionError("It is not possible to subtract vectors of different size (${values.size} and ${quantity.values.size})")
        } else throw SubtractionError("${this.unit} and ${quantity.unit}")
        return VectorQuantity(resultingValues, resultingUnit, resultingUnitSpec)
    }


    /**
     * Negates sign of Real or Integer-Valued Variable
     */
    open fun negate(): VectorQuantity {
        val resultingValues = mutableListOf<DD>()
        return when (values[0]) {
            is AADD -> {
                for (i in values.indices)
                    resultingValues.add((values[i] as AADD).negate())
                VectorQuantity(resultingValues, unit, unitSpec)
            }
            is IDD -> {
                for (i in values.indices)
                    resultingValues.add((values[i] as IDD).negate())
                VectorQuantity(resultingValues)
            }
            else -> throw SemanticError("negate only applicable on values of type Real or Integer")
        }
    }

    /**
     * Infix fun for power
     * @param quantity Exponent for the Pow function
     * @return result of the calculation
     */
    open infix fun pow(quantity: VectorQuantity): VectorQuantity {
        if (quantity.values.size != 1) throw DDError("Power parameter only for Vectors of size one (values)")
        if (values[0] is BDD) throw BDDError("Pow not allowed for BDDs")
        return pow(quantity.values[0])
    }

//--------------Compare operations--------------------------------

    /**
     * Compares quantities with "greater than"
     * @return VectorQuantity with the result as BDD as a new VectorQuantity
     */
    infix fun gt(quantity: VectorQuantity): Quantity {
        if (values[0] is BDD) throw BDDError("Greater than not allowed for BDDs")
        if (values.size > 1 || quantity.values.size > 1) throw DDError("For > both values should not be Vectors")
        return Quantity(value greaterThan quantity.value)
    }

    /**
     * Compares quantities with "less than"
     * @return VectorQuantity with the with result as BDD as a new VectorQuantity
     */
    infix fun lt(quantity: VectorQuantity): Quantity {
        if (values[0] is BDD) throw BDDError("Less than not allowed for BDDs")
        if (values.size > 1 || quantity.values.size > 1) throw DDError("For < both values should not be Vectors")
        return Quantity(value lessThan quantity.value)
    }

    /**
     * Compares quantities with "greater equals"
     * @return VectorQuantity with the with result as BDD as a new VectorQuantity
     */
    infix fun ge(quantity: VectorQuantity): Quantity {
        if (values[0] is BDD) throw BDDError("Greater equals not allowed for BDDs")
        if (values.size > 1 || quantity.values.size > 1) throw DDError("For >= both values should not be Vectors")
        return Quantity(value greaterThanOrEquals quantity.value)
    }

    /**
     * Compares quantities with "less equals"
     * @return VectorQuantity with the with result as BDD as a new VectorQuantity
     */
    infix fun le(quantity: VectorQuantity): Quantity {
        if (values[0] is BDD) throw BDDError("Less equals not allowed for BDDs")
        if (values.size > 1 || quantity.values.size > 1) throw DDError("For <= both values should not be Vectors")
        return Quantity(value lessThanOrEquals quantity.value)
    }

    /**
     * Compares quantities with "equals"
     * @return VectorQuantity with the with result as BDD as a new VectorQuantity
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
        if (values.first() is BDD) {
            var equal = value.asBdd().xor(quantity.value.asBdd()).not()
            for(i in 1 until values.size ){
                if(values[i] != quantity.values[i]){
                    equal = equal.and(values[i].asBdd().xor(quantity.values[i].asBdd()).not())
                }
            }
            return Quantity(equal)
        }
        if (values.size > 1 || quantity.values.size > 1) throw DDError("For = both values should not be Vectors")
        var equal = (value lessThanOrEquals quantity.value).and(value greaterThanOrEquals quantity.value)
        for(i in 1 until values.size ){
            if(values[i] != quantity.values[i]){
                equal = equal.and((values[i] lessThanOrEquals quantity.values[i]).and(values[i] greaterThanOrEquals quantity.values[i]))
            }
        }
        return Quantity(equal)
    }
//--------------Miscellaneous operations--------------------------------

    /**
     * The Ceil operation on AADD, rounds up to next integer value,
     * @return VectorQuantity with Real (AADD) type as a new VectorQuantity
     */
    open fun ceil(): VectorQuantity {
        if (values[0] !is AADD) throw SemanticError("Ceil must have parameter of type Real")
        val resultingValues = values.toMutableList()
        for (i in resultingValues.indices)
            resultingValues[i] = (resultingValues[i] as AADD).ceil()
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /** Floor operation on AADD just rounds up to the next integer value,
     * @return VectorQuantity with Real (AADD) type as a new VectorQuantity
     */
    open fun floor(): VectorQuantity {
        if (values.firstOrNull() !is AADD) throw SemanticError("Floor must have parameter of type Real")
        val resultingValues = values.toMutableList()
        for (i in resultingValues.indices)
            resultingValues[i] = (resultingValues[i] as AADD).floor()
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /**
     * Applies the square root to a VectorQuantity
     * Example: 100 m^2 --> 10 m
     * @return VectorQuantity with result as a new VectorQuantity
     */
    open fun sqrt(): VectorQuantity {
        //calculate final sqrt value
        val resultingValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD -> { // either bigger or equal than zero or not defined
                values.forEach {
                    require((it as AADD).min >= -0.00001 || it.min.isInfinite()) { "Sqrt only possible for values greater or equal than zero" }
                    var valueForSqr = it.asAadd()
                    if (valueForSqr.min in -0.00001..0.0) // if close to zero choose zero for sqrt
                        valueForSqr = it.builder.range(0.0, valueForSqr.max)
                    resultingValues.add(valueForSqr.sqrt())
                }
            }
            is IDD -> { // either bigger or equal than zero or not defined
                values.forEach {
                    require((it as IDD).min >= 0 || it.min == Long.MIN_VALUE) { "Sqrt only possible for values greater or equal than zero" }
                    resultingValues.add(it.sqrt())
                }
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
        return VectorQuantity(resultingValues, resUnit)
    }

    /**
     * Applies the square to a VectorQuantity
     * Example: 100 m^2 --> 10 m
     * @return VectorQuantity with result as a new VectorQuantity
     */
    open fun sqr(): VectorQuantity {
        if (value !is AADD && value !is IDD) {
            throw BDDError("Sqr not allowed for any other type than AADD or IDD")
        }

        val resultValues = mutableListOf<DD>()
        //calculate final sqrt value
        when (value) {
            is AADD -> values.forEach { resultValues.add(it.asAadd().pow(it.builder.scalar(2.0))) }
            is IDD -> values.forEach { resultValues.add(it.asIdd().sqr()) }
            else -> throw BDDError("Sqr not allowed for any other type than AADD or IDD")
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
     * @return VectorQuantity with result as a new VectorQuantity
     */
    open fun ln(): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") {
            throw SemanticError("Log with units is not allowed")
        }
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD -> values.forEach { finalValues.add((it as AADD).log()) }
            is IDD -> values.forEach { finalValues.add((it as IDD).log()) }
            else -> throw SemanticError("Log not allowed for any other type than AADD or IDD.")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    /**
     * calculates log of a VectorQuantity with a given base
     * @param base base value for the logarithm
     * @return VectorQuantity with result as a new VectorQuantity
     */
    open fun log(base: DD): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Log with units is not allowed")
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD -> {
                values.forEach { finalValues.add((it as AADD).log() / (base as AADD).log()) }
                //this avoids NaNs in result. NaN occurs, if 1 is included in base.
                finalValues.indices.forEach { if (finalValues[it].asAadd().isNaN()) finalValues[it] = values[0].builder.Reals }
            }

            is IDD -> {
                values.forEach { finalValues.add((it as IDD).log(base as IDD)) }
                //this avoids NaNs in result. NaN occurs, if 1 is included in base.
                finalValues.indices.forEach { if (finalValues[it].asIdd().isNaN()) finalValues[it] = values[0].builder.Integers }
            }

            else -> throw SemanticError("Log not allowed for any other type than AADD or IDD.")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    fun log(bases: VectorQuantity): VectorQuantity {
        val results = mutableListOf<DD>()
        when (values[0]) {
            is AADD -> {
                values.indices.forEach { results.add(values[it].asAadd().log() / bases.values[it].asAadd().log()) }
                //this avoids NaNs in result. NaN occurs, if 1 is included in base.
                results.indices.forEach { if (results[it].asAadd().isNaN()) results[it] = values[0].builder.Reals }
            }

            is IDD -> {
                values.indices.forEach { results.add(values[it].asIdd().log() / bases.values[it].asIdd().log()) }
                //this avoids NaNs in result. NaN occurs, if 1 is included in base.
                results.indices.forEach { if (results[it].asIdd().isNaN()) results[it] = values[0].builder.Integers }
            }

            else -> throw DDError("Log only possible for IDD and AADD")
        }
        return VectorQuantity(results, unit, unitSpec)
    }

    /**
     * calculates exp of a VectorQuantity
     * It is used for the following function: f(x) = e^x
     * @return VectorQuantity with result as a new VectorQuantity
     */
    open fun exp(): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() == "?") throw SemanticError("Exp with units is not allowed")
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD -> values.forEach { finalValues.add((it as AADD).exp()) }
            is IDD -> values.forEach { finalValues.add((it as IDD).exp()) }
            else -> throw SemanticError("Exp only possible with IDD and AADD")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    /**
     * Calculates Pow2 for Quantities
     * It is used for the following function: f(x) = 2^x
     * @return VectorQuantity with result as a new VectorQuantity
     */
    open fun pow2(): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Pow2 with units is not allowed")
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD -> values.forEach { finalValues.add((it as AADD).power2()) }
            is IDD -> values.forEach { finalValues.add((it as IDD).power2()) }
            else -> throw SemanticError("Pow2 only possible with IDD and AADD")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    /**
     * Calculates Pow for Quantities: f(x,y) = x^y
     * @param exponent Exponent for the Pow function
     * @return VectorQuantity with result as a new VectorQuantity
     */
    open fun pow(exponent: DD): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (unit.toString() !in setOf("1", "?", "dB"))
            throw SemanticError("Power with units is not allowed")
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD -> values.forEach { finalValues.add((it as AADD).pow(exponent.asAadd())) }
            is IDD -> values.forEach { finalValues.add((it as IDD).pow(exponent.asIdd())) }
            else -> throw SemanticError("Power only possible with IDD and AADD")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    open fun sin(): VectorQuantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Sin with units is not allowed")
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD ->  values.forEach { finalValues.add((it as AADD).sin()) }
            else -> throw SemanticError("Sin only possible with AADD")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    open fun arcsin(): VectorQuantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("arcsin with units is not allowed")
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD ->  values.forEach { finalValues.add((it as AADD).arcsin()) }
            else -> throw SemanticError("arcsin only possible with AADD")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    open fun cos(): VectorQuantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("Cos with units is not allowed")
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD ->  values.forEach { finalValues.add((it as AADD).cos()) }
            else -> throw SemanticError("Cos only possible with AADD")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    open fun arccos(): VectorQuantity{
        if (unit.toString() != "1" && unit.toString() != "?") throw SemanticError("arccos with units is not allowed")
        val finalValues = mutableListOf<DD>()
        when (values[0]) {
            is AADD ->  values.forEach { finalValues.add((it as AADD).arccos()) }
            else -> throw SemanticError("arccos only possible with AADD")
        }
        return VectorQuantity(finalValues, unit, unitSpec)
    }

    /**
     * Calculates angle between two vectors of the same size.
     * @param other vector for angle calculation
     * @return Angle as a Quantity with AADD value in radiant representation
     */
    open fun angle(other: VectorQuantity): Quantity {
        if (values.size != other.values.size)
            throw DDError("For the angle calculation vectors must have the same size, not ${values.size} and ${other.values.size}")
        val division = this.dot(other) / (abs() * other.abs())
        return when (division.value) {
            is AADD -> {
                var result1 = acos(division.getMinAsDouble()) //TODO use AF acos Function, this does not work for intervals
                var result2 = acos(division.getMaxAsDouble())
                if (result1.isNaN()) result1 = result2 //If angle 180°, division.min is -1.00000x < -1, which makes result1 NaN
                if (result2.isNaN()) result2 = result1 //If angle 0°, division.max is 1.00000x > 1, which makes result2 NaN
                if (result1 > result2) result1 = result2.also { result2 = result1 } //swap if result1>result2
                Quantity(values[0].builder.range(result1, result2), "rad")
            }

            is IDD -> {
                var result1 = acos(division.value.asIdd().min.toDouble()) //TODO use AF acos Function, this does not work for intervals
                var result2 = acos(division.value.asIdd().max.toDouble())
                if (result1.isNaN()) result1 = result2 //If angle 180°, division.min is -1.00000x < -1, which makes result1 NaN
                if (result2.isNaN()) result2 = result1 //If angle 0°, division.max is 1.00000x > 1, which makes result2 NaN
                if (result1 > result2) result1 = result2.also { result2 = result1 } //swap if result1>result2
                Quantity(values[0].builder.range(result1, result2), "rad")
            }

            else -> throw DDError("Vector operations are only supported for IDD and AADD")
        }
    }

    open fun norm(): VectorQuantity {
        if (values[0] is IDD)
            throw DDError("Normalizing vectors with Integers is not supported")
        return this.div(abs())
    }

    /**
     * Transforms the VectorQuantity to a String with the unit as a value and a fraction of units
     * If the VectorQuantity contains a unitSpec, the unit is transformed to this VectorQuantity before returning the string.
     * @Return a string representation of the VectorQuantity
     */
    override fun toString(): String {
        // For Vectors with one value use toString of Quantity
        if (values.size == 1)
            return Quantity(values[0], unit, unitSpec).toString()
        var resultingString = "("
        when (values[0]) {
            is IDD -> {
                values.forEach {
                    val value = (it as IDD).getRange()
                    resultingString += if (value.min == value.max) {
                        it.min.toString()
                    } else {
                        val min = if (value.min == Long.MIN_VALUE) "*" else value.min.toString()
                        val max = if (value.max == Long.MAX_VALUE) "*" else value.max.toString()
                        "$min..$max"
                    }
                    resultingString += ", " //Add comma between vales
                }
                resultingString = resultingString.dropLast(2) //Remove unnecessary comma and space after last element
                resultingString += ")"
            }

            is BDD -> {
                values.forEach {
                    resultingString += (it as BDD).toString()
                    resultingString += ", " //Add comma between vales
                }
                resultingString = resultingString.dropLast(2) //Remove unnecessary comma and space after last element
                resultingString += ")"
            }

            is StrDD -> {
                values.forEach {
                    resultingString += (it as StrDD).toString()
                    resultingString += ", " //Add comma between vales
                }
                resultingString = resultingString.dropLast(2) //Remove unnecessary comma and space after last element
                resultingString += ")"
            }

            is AADD -> {
                var transformedUnitString = ""
                values.forEach {
                    if (unitSpec == "DateTime" && it.asAadd().getRange().isFinite()) {//Transform timestamp to real datetime
                        val min = timeToString(round(it.asAadd().getRange().min))
                        val max = timeToString(round(it.asAadd().getRange().max))
                        resultingString += if (min == max)
                            max
                        else
                            "$min .. $max"
                    } else if (unitSpec == "Date" && it.asAadd().getRange().isFinite())//Transform timestamp to real date
                        resultingString += dateToString(it.asAadd().getRange().max)
                    else if (unitSpec == "Month" && it.asAadd().getRange().isFinite())//Transform timestamp to real date
                        resultingString += monthToString(it.asAadd().getRange().max)
                    else if (unitSpec == "Year" && it.asAadd().getRange().isFinite())
                        resultingString += yearToString(it.asAadd().getRange().max)
                    //normal VectorQuantity with value and unit
                    val transformedValue: DD
                    if (unitSpec != "") {
                        transformedValue = Quantity(it, unit).valueIn(unitSpec)
                        transformedUnitString = unitSpec
                    } else if (unit.toString() == "?") {
                        transformedValue = it
                        transformedUnitString = unit.toString()
                    } else if (unit.calculatedUnitSymbol != "") {
                        val unitSymbol = unit.calculatedUnitSymbol //there is no prefix, because all defined units have no prefix
                        //find best prefix by trying every prefix and use the one with a value bigger than one and the maximum prefix factor
                        val quantityCalc = Quantity(it, Unit(unitSymbol))
                        var bestSolution: Prefix = Yocto
                        var bestSolutionFound = false
                        val prefixesNotBinary = ConversionTables.prefixes.filter { prefix -> prefix.key == "" || prefix.key.last() != 'i' }
                        for (prefix in prefixesNotBinary) {  //do not use binary prefixes like Gi
                            if (bestSolution.factor < prefix.value.factor) { //only check for better factors
                                val valueInPrefix = quantityCalc.valueIn(prefix.key + unitSymbol)
                                // * 1.0001 to avoid rounding errors to cause problems, value should be bigger than one or a too small value for prefixes
                                val min = valueInPrefix.asAadd().min
                                val max = valueInPrefix.asAadd().max
                                if ((min * 1.0001 >= 1 || max < 0e-24) && abs(max / min) <= 10.0.pow(24)) {
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
                        transformedValue = it
                        transformedUnitString = unit.toString()
                    }
                    //use representer to format value
                    val valueStr = Representer().represent(transformedValue.asAadd())
                    resultingString += valueStr
                    resultingString += ", "
                }
                //Remove unnecessary comma and space after last element
                resultingString = resultingString.dropLast(2)
                resultingString += ")"
                // Add unit to resulting String
                resultingString += if (transformedUnitString != "1") " $transformedUnitString" else ""
            }

            else -> throw DDError("Unsupported value type for toString: $values")
        }
        return resultingString
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
    open fun bdd(): BDD = values[0] as BDD
    open fun aadd(): AADD = values[0] as AADD
    open fun idd(): IDD = values[0] as IDD

    /**
     * return all values
     */
    open fun bdds(): MutableList<BDD> = values as MutableList<BDD>
    open fun aadds(): MutableList<AADD> = values as MutableList<AADD>
    open fun idds(): MutableList<IDD> = values as MutableList<IDD>

    /**
     * Converts this unit to the expected unit representation and returns the value of the conversion
     * @param wantedRepresentation String of the wanted representation of the Unit
     * @Return value in AADD/IDD of the result
     */
    open fun valuesIn(wantedRepresentation: String): List<DD> {
        val expectedUnit = Unit(wantedRepresentation)
        // Unit "1" means there is no unit to transform to
        if (expectedUnit.toString() == "1" || unit.toString() == "?")
            return values
        val resultingList = mutableListOf<DD>()
        values.forEach {
            // Logarithmic quantity is transformed to not logarithmic VectorQuantity
            if (expectedUnit.isLogarithmic) {
                val ten = it.builder.scalar(10.0)
                resultingList.add(ten * it.asAadd().log() / ten.log())
            }
            //Special case for temperature to temperature conversion From K to °C/°F
            else if (unit.unitSet.isNotEmpty() && unit.unitSet.elementAt(0) is Temperature  && expectedUnit.unitSet.isNotEmpty() && expectedUnit.unitSet.elementAt(0) is Temperature) {
                val unit1 = unit.unitSet.elementAt(0)
                val unit2 = expectedUnit.unitSet.elementAt(0)
                if (unit1 is Temperature && unit2 is Temperature && unit2.name != "kelvin")
                    resultingList.add(unit1.convertTo(it * unit1.prefix.factor, unit2))
            }
            else {
                //1) Make expected unit canonical and calculate correlationFac
                val temporaryExpected = Quantity(it.builder.scalar(1.0), expectedUnit.clone()) //also calculates toSI()
                val correlationFac = temporaryExpected.value

                //2) Compare them
                if (unit == temporaryExpected.unit) resultingList.add(it.div(correlationFac)) else
                    throw TransformationError("$expectedUnit and $unit")
                }
        }
        return resultingList
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
        val resultingValues = mutableListOf<DD>()
        if (unit.unitSet.isNotEmpty()) {
            values.forEach { resultingValues.add(it * factor) }
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
                val ten = values[i].builder.scalar(10.0)
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
                    if (currentUnit is Temperature) resultingValues[i] = currentUnit.toKelvin(resultingValues[i])
                    resultingValues[i] = resultingValues[i] * currentUnit.convFac.pow(currentUnit.exponent)
                }
            }
        }
        //make unit canonical
        values = resultingValues
        unit = resultUnit
        unit.reduceRedundantUnits()
    }

    open fun getDimension(): String {
        return unit.getUnitDimension(values[0].asAadd().getRange().min)
    }

    /**
     * Intersects a VectorQuantity with another VectorQuantity of the same property (e.g. upVectorQuantity with downVectorQuantity)
     * @param q Intersect the current VectorQuantity with this VectorQuantity
     * @return Intersected VectorQuantity as a new VectorQuantity
     **/
    fun intersect(q: VectorQuantity): VectorQuantity {
        if (unit.toString() == "?") return this.clone()
        // Do intersection for REAL or INT
        val resultingValues = mutableListOf<DD>()
        if (values.size != q.values.size) {
            throw VectorDimensionError("Not possible to intersect VectorQuantities of different size (${values.size} and ${q.values.size})")
        }
        for (i in values.indices) {
            when (values[i]) {
                is AADD -> resultingValues.add(values[i].asAadd() intersect q.values[i].asAadd())
                is IDD -> resultingValues.add(values[i].asIdd() intersect q.values[i].asIdd())
                is BDD -> resultingValues.add(values[i].asBdd() intersect q.values[i].asBdd())
                else -> throw DDError("Intersection only possible for IDD, BDD, and ADD")
            }
        }
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    open fun constrainString(stringSpecs: List<String>): VectorQuantity {
        val resultingValues = mutableListOf<DD>()
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
        val resultingValues = mutableListOf<DD>()
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
     * @param intSpecs the current VectorQuantity should be constrained to this intervals
     * @return Constrained VectorQuantity as a new VectorQuantity
     **/
    open fun constrain(intSpecs: MutableList<IntegerRange>): VectorQuantity {
        val resultingValues = mutableListOf<IDD>()
        if (values.size == intSpecs.size)
            values.indices.forEach { resultingValues.add(values[it].asIdd() intersect values[it].builder.range(intSpecs[it])) }
        else
            if (intSpecs.size == 1 && intSpecs[0] == IntegerRange.Integers) // Special case, if there is no definition of boolSpec, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asIdd()) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${intSpecs.size}")

        return VectorQuantity(resultingValues)
    }

    /**
     * Constrain a VectorQuantity with the interval of this valueFeature (only for AADD)
     * TODO: define strategy that ensures that intersect in evalUp/Down does not introduce
     *  arbitrary many comparisons and hence growing size of BDD/AADD
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

        // convert RangeSpec, which is in UnitSpec into SI Unit by using a new VectorQuantity
        if (rangeSpecs.any { it.isNaN() }) return this.clone()

        // calculate intersection of propagated and specified values
        val resultingValues = mutableListOf<DD>()
        if (values.size == rangeSpecs.size)
            values.indices.forEach {
                resultingValues.add(values[it].asAadd() constrainTo Quantity(value.builder.range(rangeSpecs[it]), Unit(unitSpec)).getRange())
            }
        else
            if (rangeSpecs.size == 1 && rangeSpecs[0] == Range.Reals) // Special case, if there is no definition of rangeSpecs, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asAadd()) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${rangeSpecs.size}")

        for (i in values.indices) {
            if (q.values[i].toString() != "Real" && !(q.values[i] as AADD).isEmpty()) {
                resultingValues[i] = resultingValues[i].asAadd() constrainTo q.values[i].asAadd()
            }
        }


        // Iff one of the results was NaN, continue with the other (???)
        if (resultingValues.any { (it as AADD).isEmpty() })
            return if (q.values.any { (it as AADD).isNaN() })
                this.clone()
            else if (values.any { (it as AADD).isNaN() })
                q.clone()
            else {
                return VectorQuantity(resultingValues, unit, unitSpec) //contains empty set
            }
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /**
     * Constrain function for two Quantities with the same unit (normally SI), for IDD and AADD
     */
    fun constrain(q: VectorQuantity): VectorQuantity {
        // only constrain to newQ if not infinite and not empty
        val resultingValues = values.toMutableList()
        when (q.values[0]) {
            is AADD -> {
                for (i in values.indices) {
                    if (!(q.values[i] as AADD).isNaN() && !(q.values[i] as AADD).isEmpty())
                        resultingValues[i] = (values[i].asAadd() constrainTo (q.values[i] as AADD).getRange())
                }
            }

            is IDD -> {
                for (i in values.indices) {
                    if (!(q.values[i] as IDD).isNaN() && !(q.values[i] as IDD).isEmpty())
                        resultingValues[i] = (values[i].asIdd() constrainTo (q.values[i] as IDD).getRange())
                }
            }

            else -> throw SemanticError("Constrain only for IDD and AADD")
        }
        // Iff one of the results was NaN, continue with the other (???)
        return VectorQuantity(resultingValues, unit, unitSpec)
    }

    /**
     * Returns a new quantity that is constrained to spec.
     * @param constraints: the constraints to be applied.
     * @param isBoolean Shows that the constraint function should work with boolean. Needed for compiler to separate it from Integer constrain
     * @return a new quantity that is q, constrained to constraint.
     */
    open fun constrain(constraints: MutableList<XBool>, isBoolean: Boolean): VectorQuantity {
        val resultingValues = mutableListOf<BDD>()
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
     * TODO: Should return BDD! And comparison should use jAADD comparison function.
     * @param other the other object
     * @return true, if equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VectorQuantity) return false

        if (unit.toString() != other.unit.toString()) return false
        for (i in values.indices) { //iterate through all element pairs
            when (values) {
                is AADD -> {
                    val min1 = values[i].asAadd().getRange().min
                    val max1 = values[i].asAadd().getRange().max
                    val min2 = other.values[i].asAadd().getRange().min
                    val max2 = other.values[i].asAadd().getRange().max
                    //if difference is too big, they are not the same
                    if (abs(min1 - min2) > abs(min1) * 0.0001) return false
                    if (abs(max1 - max2) > abs(max1) * 0.0001) return false
                }

                is IDD -> {
                    val min1 = values[i].asIdd().getRange().min
                    val max1 = values[i].asIdd().getRange().max
                    val min2 = other.values[i].asIdd().getRange().min
                    val max2 = other.values[i].asIdd().getRange().max
                    // For infinite values compare borders
                    if (min1 == values[i].builder.Integers.min || min1 == values[i].builder.Integers.max)
                        return min1 == min2 && max1 == max2
                    //if difference is too big, they are not the same
                    if (abs(min1 - min2) > abs(min1) * 0.0001) return false
                    if (abs(max1 - max2) > abs(max1) * 0.0001) return false
                }
                // No units ...
                is BDD,
                is StrDD -> return values.toString() == other.values.toString()

                else -> {}
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
            is IDD -> {
                var squareSum = values[0].builder.scalar(0)
                values.forEach {
                    val min = it.asIdd().min
                    val max = it.asIdd().max
                    val absoluteValue = if (min <= 0 && max >= 0)
                        it.builder.range(0, max(abs(min), max))
                    else if (max < 0)
                        it.builder.range(abs(max), abs(min))
                    else
                        it.builder.range(min, max)
                    squareSum += absoluteValue.sqr()
                }
                Quantity(squareSum.sqrt())
            }

            is AADD -> {
                var squareSum = values[0].builder.scalar(0.0)
                values.forEach {
                    val min = it.asAadd().min
                    val max = it.asAadd().max
                    val absoluteValue = if (min <= 0.0 && max >= 0.0)
                        it.builder.range(0.0, max(abs(min), max))
                    else if (max < 0.0)
                        it.builder.range(abs(max), abs(min))
                    else
                        it.builder.range(min, max)
                    squareSum += absoluteValue.sqr()
                }
                if (squareSum.min in -0.000001..0.000001) { // if result is close to zero make it zero, because sqrt of negative values is not possible
                    squareSum = values[0].builder.range(0.0, squareSum.max)
                }
                Quantity(squareSum.sqrt(), unit)
            }

            else -> throw DDError("Unsupported value for abs: $values")
        }
    }

    /**
     * Length of vector using the city block distance (Manhattan Distance)
     * @return Quantity with length and unit of vector
     */
    open fun cityBlockDistance(param: VectorQuantity): Quantity {
        return when (values[0]) {
            is IDD -> {
                val connectingVector = this - param
                var sum = values[0].builder.scalar(0)
                connectingVector.values.forEach {
                    val min = it.asIdd().min
                    val max = it.asIdd().max
                    val absoluteValue = if(min<=0 && max>=0)
                        it.builder.range(0,max(abs(min),max))
                    else if (max<0)
                        it.builder.range(abs(max),abs(min))
                    else
                        it.builder.range(min,max)
                    sum += absoluteValue
                }
                Quantity(sum)
            }

            is AADD -> {
                val connectingVector = this - param
                var sum = values[0].builder.scalar(0.0)
                connectingVector.values.forEach {
                    val min = it.asAadd().min
                    val max = it.asAadd().max
                    val absoluteValue = if(min<=0.0 && max>=0.0)
                        it.builder.range(0.0,max(abs(min),max))
                    else if (max<0.0)
                        it.builder.range(abs(max),abs(min))
                    else
                        it.builder.range(min,max)
                    sum += absoluteValue
                }// if result is close to zero make it zero, because sqrt of negative values is not possible
                if(sum.min in -0.000001..0.000001) sum = values[0].builder.range(0.0,sum.max)
                Quantity(sum, unit)
            }

            else -> throw DDError("Unsupported value for abs: $values")
        }
    }

    //--------------Boolean operations--------------------------------

    /**
     * Applies boolean "and" operation
     * @return VectorQuantity with the with result as BDD as a new VectorQuantity
     */
    infix fun and(quantity: VectorQuantity): VectorQuantity {
        if (values[0] !is BDD) throw BDDError("Boolean \"and\" can only be applied to BDDs")
        val results = mutableListOf<BDD>()
        values.indices.forEach { results.add(values[it].asBdd() and quantity.values[it].asBdd()) }
        return VectorQuantity(results)
    }

    /**
     * Applies boolean "or" operation
     * @return VectorQuantity with the with result as a new VectorQuantity
     */
    infix fun or(quantity: VectorQuantity): VectorQuantity {
        if (values[0] !is BDD) throw BDDError("Boolean \"or\" can only be applied to BDDs")
        val results = mutableListOf<BDD>()
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
        if (value is BDD) throw BDDError("No min value for BDDs")
        if (value is AADD) return aadd().getRange().min
        if (value is IDD) return idd().getRange().min.toDouble()
        throw SemanticError("Expect either Real or Integer")
    }

    /**
     * @return the max value of the Range as Double of the first value
     */
    open fun getMaxAsDouble(): Double {
        if (value is BDD) throw BDDError("No max value for BDDs")
        if (value is AADD) return aadd().getRange().max
        if (value is IDD) return idd().getRange().max.toDouble()
        throw SemanticError("Expect either Real or Integer")
    }

}

/**
 * Returns the maximum of two quantities a, b.
 */
fun max(a: VectorQuantity, b: VectorQuantity): VectorQuantity {
    val af = a.ge(b).bdd().ite(a, b)
    when (a.values[0]) {
        is AADD -> {
            val ranges = mutableListOf<Range>()
            a.values.indices.forEach {
                ranges.add(
                    Range(max(a.values[it].asAadd().min, b.values[it].asAadd().min), max(a.values[it].asAadd().max, b.values[it].asAadd().max))
                )
            }
            val results = mutableListOf<AADD>()
            ranges.indices.forEach { results.add(af.values[it].asAadd().constrainTo(ranges[it])) }
            return VectorQuantity(results, af.unit, af.unitSpec)
        }

        is IDD -> {
            val ranges = mutableListOf<IntegerRange>()
            a.values.indices.forEach {
                ranges.add(
                    IntegerRange(max(a.values[it].asIdd().min, b.values[it].asIdd().min), max(a.values[it].asIdd().max, b.values[it].asIdd().max))
                )
            }
            val results = mutableListOf<IDD>()
            ranges.indices.forEach { results.add(af.values[it].asIdd().constrainTo(ranges[it])) }
            return VectorQuantity(results)
        }
        else -> throw SemanticError("Expect parameters of max to be Real or Integer.")
    }
}

/**
 * Returns the minimum of two quantities a, b.
 */
fun min(a: VectorQuantity, b: VectorQuantity): VectorQuantity {
    val af = a.le(b).bdd().ite(a, b)
    when (a.values[0]) {
        is AADD -> {
            val ranges = mutableListOf<Range>()
            a.values.indices.forEach {
                ranges.add(
                    Range(min(a.values[it].asAadd().min, b.values[it].asAadd().min), min(a.values[it].asAadd().max, b.values[it].asAadd().max))
                )
            }
            val results = mutableListOf<AADD>()
            ranges.indices.forEach { results.add(af.values[it].asAadd().constrainTo(ranges[it]))}
            return VectorQuantity(results, af.unit, af.unitSpec)
        }

        is IDD -> {
            val ranges = mutableListOf<IntegerRange>()
            a.values.indices.forEach {
                ranges.add(
                    IntegerRange(min(a.values[it].asIdd().min, b.values[it].asIdd().min), min(a.values[it].asIdd().max, b.values[it].asIdd().max))
                )
            }
            val results = mutableListOf<IDD>()
            ranges.indices.forEach { results.add(af.values[it].asIdd().constrainTo(ranges[it]))}
            return VectorQuantity(results)
        }

        else -> throw SemanticError("Expect parameters of min to be Real or Integer.")
    }
}

fun BDD.ite(t: VectorQuantity, e: VectorQuantity): VectorQuantity {
    val results = mutableListOf<DD>()
    t.values.indices.forEach { results.add(this.ite(t.values[it], e.values[it])) }
    return VectorQuantity(results, t.unit, t.unitSpec)
}
