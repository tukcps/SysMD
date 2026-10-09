package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.*
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.quantities.baseUnits.ThermodynamicTemperature
import io.github.tukcps.aadd.*
import io.github.tukcps.aadd.DDBuilder.BoolMath.and
import io.github.tukcps.aadd.DDBuilder.BoolMath.not
import io.github.tukcps.aadd.DDBuilder.BoolMath.or
import io.github.tukcps.aadd.DDBuilder.BoolMath.xor
import io.github.tukcps.aadd.DDBuilder.IntMath
import io.github.tukcps.aadd.DDBuilder.IntMath.abs
import io.github.tukcps.aadd.DDBuilder.IntMath.exp
import io.github.tukcps.aadd.DDBuilder.IntMath.ln
import io.github.tukcps.aadd.DDBuilder.IntMath.log
import io.github.tukcps.aadd.DDBuilder.IntMath.sqrt
import io.github.tukcps.aadd.DDBuilder.RealMath
import io.github.tukcps.aadd.DDBuilder.RealMath.acos
import io.github.tukcps.aadd.DDBuilder.RealMath.exp
import io.github.tukcps.aadd.DDBuilder.RealMath.ln
import io.github.tukcps.aadd.DDBuilder.RealMath.log
import io.github.tukcps.aadd.DDBuilder.RealMath.negate
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.util.Tolerance
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.bounds.*
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.abs
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import io.github.tukcps.aadd.values.real.ia.RealRange
import kotlin.math.*

private val equalsTolerance = Tolerance(relative = 1e-6)

/**
 * Tolerance for the comparisons gt, lt, ge, le, eq and neq, to catch values that differ only by rounding noise.
 * It is mainly relative, because quantities are stored in SI and can be of very different magnitude (e.g. 1e-20 F or
 * 1e12 Hz). The tiny absolute part is only a noise floor for values that should be 0 but result from cancellation.
 */
private val comparisonTolerance = Tolerance(relative = 1e-12, absolute = 1e-15)

/**
 * A quantity that consists of a value that is represented by a DD<*> instance, and
 * a unit that is represented by SI units fraction. The unit is transformed to SI, so that
 * calculations are more efficient
 */
class VectorQuantity private constructor(
    val values: List<DD<*>>,
    val unit: Unit,
    val unitSpec: String,
    /** True if unitSpec was explicitly specified by the user; false if inherited from a datatype default. */
    val userWantedUnitSpec: Boolean
) : Cloneable
{
    init {
        require(values.isNotEmpty()) { "Empty values for VectorQuantity is not supported" }
        if (hasUnit(unit, unitSpec) && values.any { it !is AADD }) {
            throw DDError("VectorQuantity can only have a Unit when value is AADD")
        }
    }

    companion object {
        private data class CanonicalData(val values: List<DD<*>>, val unit: Unit)

        private fun hasUnit(unit: Unit, unitSpec: String = ""): Boolean =
            unit.unitSet.isNotEmpty() || unit.unitDomain.isNotEmpty() || unit.calculatedUnitSymbol.isNotEmpty() || unitSpec.isNotEmpty()

        private fun validateAndCopy(values: List<DD<*>>): List<DD<*>> {
            if (values.isEmpty())
                throw DDError(msg = "Empty values for VectorQuantity is not supported")
            when (values[0]) {
                is Integer -> values.forEach { if (it !is Integer) throw DDError("Different value types in vector are not supported") }
                is Real    -> values.forEach { if (it !is Real)    throw DDError("Different value types in vector are not supported") }
                is Bool    -> values.forEach { if (it !is Bool)    throw DDError("Different value types in vector are not supported") }
                is StrDD   -> values.forEach { if (it !is StrDD)   throw DDError("Different value types in vector are not supported") }
            }
            return values.toList()
        }

        private fun validateAndCopyAadd(values: List<AADD>): List<AADD> {
            if (values.isEmpty())
                throw DDError(msg = "Empty values for VectorQuantity is not supported")
            return values.toList()
        }

        val NO_UNIT = Unit("")

        private fun removePrefixes(values: List<AADD>, unit: Unit): Pair<List<DD<*>>, Unit> {
            var factor = 1.0
            for (element in unit.unitSet) {
                factor *= element.prefix.factor.pow(element.exponent)
            }
            val resultingValues = if (unit.unitSet.isNotEmpty()) {
                if (factor != 1.0) values.map { it * factor } else values
            } else {
                values
            }
            var newSet = emptySet<UnitOfMeasurement>()
            for (element in unit.unitSet) {
                newSet = Unit.addUnitOfMeasurementToSet(newSet, element.copyWith(prefix = NoPrefix))
            }
            val newUnit = Unit(
                unitSet = newSet,
                unitDomain = unit.unitDomain,
                calculatedUnitSymbol = unit.calculatedUnitSymbol,
                isLogarithmic = unit.isLogarithmic,
                isDifference = unit.isDifference,
                unitStr = unit.unitStr
            )
            return Pair(resultingValues, newUnit)
        }

        private fun toSI(values: List<AADD>, unit: Unit): Pair<List<DD<*>>, Unit> {
            val (noPrefixValues, noPrefixUnit) = removePrefixes(values, unit)
            val resultingValues = noPrefixValues.toMutableList()
            if (noPrefixUnit.isLogarithmic) { // Logarithmic quantity is transformed to not logarithmic
                for (i in resultingValues.indices) {
                    val ten = resultingValues[i].builder.real(10.0)
                    // 10^n is exactly representable for integer n up to 22: no rounding widening needed (10 dB = 10 exactly)
                    val range = resultingValues[i].asAadd().getRange()
                    val exact = range.min == range.max && range.min.toDouble().let {
                        val n = it / 10.0
                        n == Math.rint(n) && n * 10.0 == it && kotlin.math.abs(n) <= 22.0
                    }
                    resultingValues[i] = if (exact) ten.builder.real(Math.pow(10.0, range.min.toDouble() / 10.0))
                    else RealMath.pow(ten, RealMath.divide(resultingValues[i].asAadd(), ten))
                }
            }
            var resultUnit = Unit(
                unitSet = emptySet(),
                unitDomain = noPrefixUnit.unitDomain,
                calculatedUnitSymbol = noPrefixUnit.calculatedUnitSymbol,
                isDifference = noPrefixUnit.isDifference,
                isLogarithmic = false,
                unitStr = noPrefixUnit.unitStr
            )
            // The offset of °C/°F only applies to an absolute temperature. In compound units (J/°C), powers and
            // temperature differences only the scale counts.
            val hasOffset = noPrefixUnit.singleTemperature != null && !noPrefixUnit.isDifference
            for (currentUnit in noPrefixUnit.unitSet) {
                // Change Unit to SI
                for (it in currentUnit.getBaseUnits()) {
                    val newUnitElement = it.copyWith(exponent = currentUnit.exponent * it.exponent)
                    resultUnit = resultUnit.addUnitOfMeasurement(newUnitElement)
                }
                // Update values
                for (i in resultingValues.indices) {
                    if (hasOffset && currentUnit is ThermodynamicTemperature)
                        resultingValues[i] = currentUnit.toKelvin(resultingValues[i])
                    else if (currentUnit.convFac.pow(currentUnit.exponent) != 1.0)
                        resultingValues[i] = resultingValues[i] * currentUnit.convFac.pow(currentUnit.exponent)
                }
            }
            return Pair(resultingValues, resultUnit.reduceRedundantUnits())
        }

        private fun canonicalize(
            values: List<DD<*>>,
            unit: Unit,
            unitSpec: String,
            userWantedUnitSpec: Boolean
        ): CanonicalData {
            if (hasUnit(unit, unitSpec) && values.any { it !is AADD }) {
                throw DDError("VectorQuantity can only have a Unit when value is AADD")
            }
            if (!hasUnit(unit, unitSpec)) {
                return CanonicalData(values, NO_UNIT)
            }
            if (unitSpec.isNotEmpty()) {
                Unit(unitSpec)
            }
            @Suppress("UNCHECKED_CAST")
            val (siValues, siUnit) = toSI(values as List<AADD>, unit)
            val finalUnit = siUnit.reduceRedundantUnits()
                .calculateUnitDomain(unitSpec)
                .calculateUnitSymbol(if (userWantedUnitSpec) unitSpec else "")
            return CanonicalData(siValues, finalUnit)
        }

        /**
         * Creates a VectorQuantity directly from already-canonical values and unit,
         * bypassing redundant SI transformation and prefix reduction.
         */
        fun fromCanonical(
            values: List<DD<*>>,
            unit: Unit = NO_UNIT,
            unitSpec: String = "",
            userWantedUnitSpec: Boolean = false
        ): VectorQuantity {
            if (hasUnit(unit, unitSpec) && values.any { it !is AADD }) {
                throw DDError("VectorQuantity can only have a Unit when value is AADD")
            }
            return VectorQuantity(validateAndCopy(values), unit, unitSpec, userWantedUnitSpec)
        }

        fun fromCanonical(
            value: DD<*>,
            unit: Unit = NO_UNIT,
            unitSpec: String = "",
            userWantedUnitSpec: Boolean = false
        ): VectorQuantity {
            if (hasUnit(unit, unitSpec) && value !is AADD) {
                throw DDError("VectorQuantity can only have a Unit when value is AADD")
            }
            return VectorQuantity(listOf(value.clone()), unit, unitSpec, userWantedUnitSpec)
        }
    }

    private val builder get() = values.first().builder

    val isScalar: Boolean get() = values.size == 1
    val isReal: Boolean get() = values.first() is Real
    val isInt: Boolean get() = values.first() is Integer
    val isBool: Boolean get() = values.first() is Bool
    val isString: Boolean get() = values.first() is StrDD

    val value: DD<*>  // Returns first value as DD<*>; throws if vector has size > 1
        get() {
            if (values.size != 1)
                throw VectorDimensionError("value is only supported for scalar VectorQuantity (size 1), but vector has size ${values.size}")
            return values[0]
        }

    fun type() = when(values.first()) {
        is Real -> Variable.BaseType.Real
        is Integer -> Variable.BaseType.Int
        is Bool  -> Variable.BaseType.Bool
        is StrDD   -> Variable.BaseType.String
    }

    private constructor(canonical: CanonicalData, unitSpec: String, userWantedUnitSpec: Boolean) :
        this(canonical.values, canonical.unit, unitSpec, userWantedUnitSpec)

    /** Unitless vector quantity. */
    constructor(values: List<DD<*>>) : this(validateAndCopy(values), NO_UNIT, "", false)

    /** Unitless scalar quantity (Bool, Integer, StrDD, or unitless Real). */
    constructor(value: DD<*>) : this(listOf(value.clone()), NO_UNIT, "", false)

    /**
     * Master constructor for unit-bearing quantities that converts into SI units.
     * Called when parsing from SysML or when raw non-SI units are specified.
     * Can only be called with a Unit when values are AADD.
     *
     * @param values Values of the VectorQuantity represented as a list of DD<*>
     * @param unitObject Unit, which should be added to the new VectorQuantity
     * @param unitSpec The wanted representation of the Unit, toString converts the Unit to this representation
     * @param unitDomain Optional domain string
     * @param userWantedUnitSpec True if [unitSpec] was explicitly specified by the user; false if inherited from a datatype
     */
    constructor(
        values: List<DD<*>>,
        unitObject: Unit,
        unitSpec: String = "",
        unitDomain: String = "",
        userWantedUnitSpec: Boolean = false
    ) : this(
        canonicalize(
            validateAndCopy(values),
            if (unitDomain.isNotEmpty() && unitObject.unitDomain.isEmpty()) unitObject.copy(unitDomain = unitDomain) else unitObject,
            unitSpec,
            userWantedUnitSpec
        ),
        unitSpec,
        userWantedUnitSpec
    )

    /**
     * @param value Value of the VectorQuantity represented as a DD<*>
     * @param unitObject Unit, which should be added to the new VectorQuantity
     * @param unitSpec The wanted representation of the Unit, toString converts the Unit to this representation
     * @param unitDomain Optional domain string
     * @param userWantedUnitSpec True if [unitSpec] was explicitly specified by the user; false if inherited from a datatype
     */
    constructor(
        value: DD<*>,
        unitObject: Unit,
        unitSpec: String = "",
        unitDomain: String = "",
        userWantedUnitSpec: Boolean = false
    ) : this(listOf(value.clone()), unitObject, unitSpec, unitDomain, userWantedUnitSpec)

    /**
     * @param values Values of the VectorQuantity represented as a list of DD<*>
     * @param unitString String representation of the Unit
     * @param unitDomain Optional domain string
     * @param userWantedUnitSpec True if [unitString] was explicitly specified by the user; false if inherited from a datatype
     */
    constructor(
        values: List<DD<*>>,
        unitString: String,
        unitDomain: String = "",
        userWantedUnitSpec: Boolean = false
    ) : this(values, Unit(unitString, unitDomain), unitString, unitDomain, userWantedUnitSpec)

    /**
     * @param value Value of the VectorQuantity represented as a DD<*>
     * @param unitString String representation of the Unit
     * @param unitDomain Optional domain string
     * @param userWantedUnitSpec True if [unitString] was explicitly specified by the user; false if inherited from a datatype
     */
    constructor(
        value: DD<*>,
        unitString: String,
        unitDomain: String = "",
        userWantedUnitSpec: Boolean = false
    ) : this(listOf(value.clone()), unitString, unitDomain, userWantedUnitSpec)

    fun copy(
        values: List<DD<*>> = this.values,
        unit: Unit = this.unit,
        unitSpec: String = this.unitSpec,
        userWantedUnitSpec: Boolean = this.userWantedUnitSpec
    ): VectorQuantity {
        if (unitSpec.isNotEmpty()) {
            Unit(unitSpec)
        }
        val newUnit = if (userWantedUnitSpec != this.userWantedUnitSpec || unitSpec != this.unitSpec) {
            unit.calculateUnitSymbol(if (userWantedUnitSpec) unitSpec else "")
        } else {
            unit
        }
        val newValues = if (values !== this.values) validateAndCopy(values) else values
        return VectorQuantity(newValues, newUnit, unitSpec, userWantedUnitSpec)
    }

    fun copy(value : DD<*>) = copy(values = listOf(value))

    /** Returns this VectorQuantity since it is immutable. */
    public override fun clone(): VectorQuantity = this

    //--------------Arithmetic operations--------------------------------

    /**
     * Scalar multiplication of vector and scalar or scalar and scalar
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    operator fun times(quantity: VectorQuantity): VectorQuantity {
        if (isBool) {
            throw BDDError("Multiplication not allowed for BDDs")
        }
        val resultingValues = when {
            isScalar -> quantity.values.map { values[0] * it } //left scalar multiplication
            quantity.isScalar -> values.map { it * quantity.values[0] } //right scalar multiplication
            else -> throw VectorDimensionError("It is not possible to multiply vectors of size ${values.size} and ${quantity.values.size}. For scalar multiplication use 'dot' instead of '*'")
        }
        return if (isReal) {
            val resultUnit = unit * quantity.unit
            fromCanonical(resultingValues, resultUnit)
        } else {
            VectorQuantity(resultingValues)
        }
    }

    /**
     * Multiplies quantities with dot product
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    infix fun dot(quantity: VectorQuantity): VectorQuantity {
        if (isBool) {
            throw BDDError("Multiplication not allowed for BDDs")
        }
        if (values.size != quantity.values.size) {
            throw VectorDimensionError("Dot product is not defined for vectors of size ${values.size} and ${quantity.values.size}")
        }
        if (isInt) {
            val sum = values.indices.fold(values[0].builder.integer(0) as DD<*>) { acc, i -> acc.plus(values[i] * quantity.values[i]) }
            return VectorQuantity(sum)
        }
        if (!isReal) throw DDError("Wrong type for dot product")
        val resultUnit = unit * quantity.unit
        val sum = values.indices.fold(values[0].builder.real(0.0) as DD<*>) { acc, i -> acc.plus(values[i] * quantity.values[i]) }
        return fromCanonical(sum, resultUnit)
    }

    /**
     * Multiplies quantities with cross-product (only possible for vectors with size 3)
     * @param quantity is multiplied to the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    infix fun cross(quantity: VectorQuantity): VectorQuantity {
        if (isBool) throw BDDError("Multiplication not allowed for BDDs")
        if (values.size != 3 || quantity.values.size != 3)
            throw VectorDimensionError("Cross product is only defined for vectors of size 3, not of size ${values.size} and ${quantity.values.size}.")

        val resultingValues = listOf(
            values[1] * quantity.values[2] - values[2] * quantity.values[1],
            values[2] * quantity.values[0] - values[0] * quantity.values[2],
            values[0] * quantity.values[1] - values[1] * quantity.values[0]
        )
        return if (isReal) {
            val resultUnit = unit * quantity.unit
            fromCanonical(resultingValues, resultUnit)
        } else {
            VectorQuantity(resultingValues)
        }
    }

    /**
     * Divides quantities. A vector divided by a scalar is divided element wise.
     * A vector divided by a vector of the same size is the inverse of the scalar multiplication: the result is
     * the scalar range that encloses all element-wise quotients (used to propagate `vector = scalar * vector` down).
     * @param quantity is the divisor of the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    operator fun div(quantity: VectorQuantity): VectorQuantity {
        if (isBool) throw BDDError("Division not allowed for BDDs")

        val resultingValues = when {
            quantity.isScalar -> values.map { it / quantity.values[0] }
            values.size == quantity.values.size -> {
                val results = values.indices.map { values[it].div(quantity.values[it]) }
                when (values[0]) {
                    is Real -> {
                        val min = results.minOf { it.asAadd().min }
                        val max = results.maxOf { it.asAadd().max }
                        listOf(values[0].builder.real(min..max))
                    }
                    is Integer -> {
                        val min = results.minOf { it.asIdd().min }
                        val max = results.maxOf { it.asIdd().max }
                        listOf(values[0].builder.integer(min..max))
                    }
                    else -> emptyList()
                }
            }
            else -> throw VectorDimensionError("It is not possible to divide vectors of size ${values.size} and ${quantity.values.size}")
        }
        return if (isReal) {
            val resultUnit = unit / quantity.unit
            fromCanonical(resultingValues, resultUnit)
        } else {
            VectorQuantity(resultingValues)
        }
    }


    /**
     * Adds quantities.
     * @param quantity is added to the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity
     */
    operator fun plus(quantity: VectorQuantity): VectorQuantity {
        if (isBool) throw BDDError("Addition not allowed for BDDs")
        if (isInt) {
            if (isScalar && values[0].asIdd().min eq 0L && values[0].asIdd().max eq 0L) return quantity
            if (quantity.isScalar && quantity.values[0].asIdd().min eq 0L && quantity.values[0].asIdd().max eq 0L) return this
            if (values.size != quantity.values.size)
                throw VectorDimensionError("It is not possible to add vectors of different size (${values.size} and ${quantity.values.size})")
            val resultingValues = values.indices.map { values[it] + quantity.values[it] }
            return VectorQuantity(resultingValues)
        }
        if (!isReal) throw DDError("Unsupported type for Addition: ${values[0]}")

        // A scalar that is exactly 0 is the neutral element, also for vectors
        val thisIsZero = isNeutralForAddition()
        val otherIsZero = quantity.isNeutralForAddition()

        if (values.size != quantity.values.size && !thisIsZero && !otherIsZero)
            throw VectorDimensionError("It is not possible to add vectors of different size (${values.size} and ${quantity.values.size})")

        if (!hasSameDimensionAs(quantity))
            throw AdditionError("${this.unit} and ${quantity.unit}")

        if (thisIsZero) return quantity
        if (otherIsZero) return this

        // absolute + difference is absolute again; only difference + difference stays a difference
        val isDifference = unit.isDifference && quantity.unit.isDifference
        val resultingUnit = (if (unit.toString() == "?") quantity.unit else unit).let {
            if (it.isDifference != isDifference) it.copy(isDifference = isDifference) else it
        }
        val resultingUnitSpec = if (unit.toString() == "?") quantity.unitSpec else unitSpec
        val resultingValues = values.indices.map { values[it] + quantity.values[it] }
        return fromCanonical(resultingValues, resultingUnit, resultingUnitSpec, userWantedUnitSpec || quantity.userWantedUnitSpec)
    }

    /**
     * Subtracts quantities.
     * @param quantity is the subtrahend of the current VectorQuantity
     * @return VectorQuantity with resulting Real/Integer value and Unit as a new VectorQuantity.
     */
    operator fun minus(quantity: VectorQuantity): VectorQuantity {
        if (isBool) throw SemanticError("Subtraction not allowed on BDDs")
        if (values.size != quantity.values.size)
            throw VectorDimensionError("It is not possible to subtract vectors of different size (${values.size} and ${quantity.values.size})")

        if (isInt) {
            val resultingValues = values.indices.map { values[it] - quantity.values[it] }
            return VectorQuantity(resultingValues)
        }
        if (!isReal) throw DDError("Unsupported type for Subtraction: ${values[0]}")

        if (!unit.isCompatibleWith(quantity.unit))
            throw SubtractionError("${this.unit} and ${quantity.unit}")

        // Subtracting two quantities gives a difference. Exception: exactly one operand is an absolute temperature
        // in a unit with an offset (20 °C - 5 K), then the other one is the difference and the result stays absolute.
        val isDifference = isOffsetTemperature() == quantity.isOffsetTemperature()
        val resultingUnit = (if (unit.toString() == "?") quantity.unit else unit).copy(isDifference = isDifference)
        val resultingUnitSpec = if (unit.toString() == "?") quantity.unitSpec else unitSpec
        val resultingValues = values.indices.map { values[it] - quantity.values[it] }
        return fromCanonical(resultingValues, resultingUnit, resultingUnitSpec, userWantedUnitSpec || quantity.userWantedUnitSpec)
    }

    /** True for a scalar Real that is exactly 0. */
    private fun isExactZero(): Boolean =
        isScalar && isReal && values[0].asAadd().getRange().let {
            !it.isEmpty() && it.min.toDouble() == 0.0 && it.max.toDouble() == 0.0
        }

    /**
     * True for a scalar Real that is skipped by plus: an exact 0 and, for historic reasons, an empty range.
     * Constraint propagation relies on `empty + x = x` (evalDown of a subtraction adds a down quantity that may be empty).
     */
    private fun isNeutralForAddition(): Boolean =
        isExactZero() || (isScalar && isReal && values[0].asAadd().getRange().isEmpty())

    /** True for an absolute temperature that is given in a unit with an offset (°C, °F). */
    private fun isOffsetTemperature(): Boolean =
        unit.singleTemperature != null && !unit.isDifference && unitSpec.isNotEmpty() &&
            Unit(unitSpec).singleTemperature?.hasOffset == true

    /** True for the plain number 0 without unit, which is zero in every unit: x [m] + 0 and x [m] > 0 are allowed. */
    private fun isPlainZero(): Boolean = isExactZero() && unit.unitSet.isEmpty()

    private fun hasSameDimensionAs(other: VectorQuantity): Boolean =
        unit.isCompatibleWith(other.unit) || isPlainZero() || other.isPlainZero()

    /** Comparing or selecting between Real quantities is only possible if they have the same dimension. */
    internal fun requireSameDimension(other: VectorQuantity) {
        if (isReal && other.isReal && !hasSameDimensionAs(other))
            throw TransformationError("$unit and ${other.unit}")
    }

    /**
     * Negates sign of Real or Integer-Valued Variable
     */
    fun negate(): VectorQuantity {
        return if (isReal) {
            fromCanonical(values.map { negate(it as Real) }, unit, unitSpec, userWantedUnitSpec)
        } else if (isInt) {
            VectorQuantity(values.map { -(it as IDD) })
        } else {
            throw SemanticError("negate only applicable on values of type Real or Integer")
        }
    }

    /**
     * Infix fun for power
     * @param quantity Exponent for the Pow function
     * @return result of the calculation
     */
    infix fun pow(quantity: VectorQuantity): VectorQuantity {
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
    infix fun gt(quantity: VectorQuantity): VectorQuantity = gt(quantity, comparisonTolerance)

    fun gt(quantity : VectorQuantity, t : Tolerance?) : VectorQuantity = when {
        values.size != 1 || quantity.values.size != 1 -> throw VectorDimensionError("Cannot compare vectors")
        else -> {
            requireSameDimension(quantity)
            VectorQuantity(value.greaterThan(quantity.value, t))
        }
    }

    /**
     * Compares quantities with "less than"
     * For vectors compare the abs values
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun lt(quantity: VectorQuantity): VectorQuantity = lt(quantity, comparisonTolerance)

    fun lt(quantity : VectorQuantity, t : Tolerance?) : VectorQuantity = when {
        values.size != 1 || quantity.values.size != 1 -> throw VectorDimensionError("Cannot compare vectors")
        else -> {
            requireSameDimension(quantity)
            VectorQuantity(value.lessThan(quantity.value, t))
        }
    }

    /**
     * Compares quantities with "greater equals"
     * For vectors compare the abs values
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun ge(quantity: VectorQuantity): VectorQuantity = ge(quantity, comparisonTolerance)

    fun ge(quantity : VectorQuantity, t : Tolerance?) : VectorQuantity = when {
        values.size != 1 || quantity.values.size != 1 -> throw VectorDimensionError("Cannot compare vectors")
        else -> {
            requireSameDimension(quantity)
            VectorQuantity(value.greaterThanOrEquals(quantity.value, t))
        }
    }

    /**
     * Compares quantities with "less equals"
     * For vectors compare the abs values
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun le(quantity: VectorQuantity): VectorQuantity = le(quantity, comparisonTolerance)

    fun le(quantity : VectorQuantity, t : Tolerance?) : VectorQuantity = when {
        values.size != 1 || quantity.values.size != 1 -> throw VectorDimensionError("Cannot compare vectors")
        else -> {
            requireSameDimension(quantity)
            VectorQuantity(value.lessThanOrEquals(quantity.value, t))
        }
    }

    /**
     * Compares quantities with "equals"
     * @return VectorQuantity with the result as Bool as a new VectorQuantity
     */
    infix fun eq(quantity: VectorQuantity): VectorQuantity = eq(quantity, comparisonTolerance)

    fun eq(quantity: VectorQuantity, t : Tolerance?): VectorQuantity = when {
        values.size != quantity.values.size -> throw VectorDimensionError("For == both values should be Vectors of the same size")
        else -> values.zip(quantity.values).also { requireSameDimension(quantity) }.filter { (x,y) -> x != y }.ifEmpty {
            return VectorQuantity(builder.boolean(true))
        }.map { (l,r) ->
            when(l) {
                is StrDD -> l.equalValue(r as StrDD)
                is Bool -> l.xor(r as BDD).not()
                else -> l.lessThanOrEquals(r, t) and l.greaterThanOrEquals(r, t)
            }
        }.reduce { l,r ->
            l.and(r)
        }.let {
            VectorQuantity(it)
        }
    }

    infix fun neq(quantity: VectorQuantity): VectorQuantity {
        val quantityEqual = eq(quantity)
        return VectorQuantity(quantityEqual.value.asBdd().not())
    }

//--------------Miscellaneous operations--------------------------------

    /**
     * The Ceil operation on Real, rounds up to next integer value.
     * The value is rounded in the displayed unit ([unitSpec]) if there is one, otherwise in SI:
     * ceil(150.5 cm) is 151 cm and ceil(20.5 °C) is 21 °C.
     * @return VectorQuantity with Real (Real) type as a new VectorQuantity
     */
    fun ceil(): VectorQuantity = roundInDisplayedUnit("Ceil", RealMath::ceil, ::ceil)

    /**
     * Floor operation on Real, rounds down to the next integer value.
     * The value is rounded in the displayed unit ([unitSpec]) if there is one, otherwise in SI:
     * floor(150.5 cm) is 150 cm and floor(20.5 °C) is 20 °C.
     * @return VectorQuantity with Real (Real) type as a new VectorQuantity
     */
    fun floor(): VectorQuantity = roundInDisplayedUnit("Floor", RealMath::floor, ::floor)

    /**
     * The unit in which ceil and floor round: the displayed unit ([unitSpec]), or null for SI if there is none.
     * Logarithmic units (dB) are rounded on the linear SI value, because their conversion is not defined for all Reals.
     */
    private fun displayedUnitForRounding(): Unit? {
        if (unitSpec.isEmpty() || unit.toString() == "?") return null
        val displayedUnit = Unit(unitSpec)
        return when {
            displayedUnit.isLogarithmic -> null
            unit.isDifference -> displayedUnit.copy(isDifference = true)
            else -> displayedUnit
        }
    }

    /**
     * Applies [transform] to the values in the displayed unit ([unitSpec], SI if there is none) and converts the
     * result back to SI. Used for ceil and floor and for their inverses in evalDown, which must use the same unit.
     * @param transform gets the value and whether it was converted from SI to another unit
     */
    private fun mapInDisplayedUnit(name: String, transform: (Real, Boolean) -> Real): VectorQuantity {
        if (!isReal) throw SemanticError("$name must have parameter of type Real")
        val displayedUnit = displayedUnitForRounding()
        val results = if (displayedUnit == null)
            values.map { transform(it as Real, false) }
        else
            VectorQuantity(valuesIn(unitSpec).map { transform(it as Real, true) }, displayedUnit).values
        return fromCanonical(results, unit, unitSpec, userWantedUnitSpec)
    }

    /**
     * Applies an inverse of ceil or floor ([transform]) in the same unit in which [ceil] and [floor] round.
     * This quantity must carry the unitSpec of the parameter of ceil/floor.
     */
    internal fun invertRoundingInDisplayedUnit(name: String, transform: (Real) -> Real): VectorQuantity =
        mapInDisplayedUnit(name) { value, _ -> transform(value) }

    /**
     * Rounds with [round] in the displayed unit. The conversion from SI widens a value by a few ulps
     * (1.5 m is 149.99999999999997..150.00000000000003 cm), which would make ceil(150 cm) = 150..151 cm.
     * Bounds that are an integer up to this conversion noise are therefore taken as that integer.
     */
    private fun roundInDisplayedUnit(name: String, round: (Real) -> Real, roundBound: (Double) -> Double): VectorQuantity =
        mapInDisplayedUnit(name) { value, converted ->
            val rounded = round(value)
            val range = value.getRange()
            if (!converted || range.isEmpty() || range.min.isInfinite || range.max.isInfinite)
                rounded
            else
                rounded.asAadd().constrainTo(RealRange(
                    roundBound(withoutConversionNoise(range.min.toDouble())),
                    roundBound(withoutConversionNoise(range.max.toDouble()))
                ))
        }

    private fun withoutConversionNoise(bound: Double): Double {
        val nearest = round(bound)
        return if (abs(bound - nearest) <= 1e-12 * max(1.0, abs(bound))) nearest else bound
    }

    /**
     * Applies the square root to a VectorQuantity
     * Example: 100 m^2 --> 10 m
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun sqrt(): VectorQuantity {
        val results = values.map {
            when(it) {
                is AADD -> it.builder.realMath { sqrt(it) }
                is IDD -> it.builder.intMath { sqrt(it) }
                else -> throw SemanticError("Sqrt not allowed for any other type than Real or Integer")
            }
        }
        return if (values[0] is Real) fromCanonical(results, unit.sqrt()) else VectorQuantity(results)
    }

    /**
     * Applies the multi-valued inverse of square to a VectorQuantity (returning positive and negative branches)
     * Example: 100 m^2 --> [-10, 10] m
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun inverseSqr(): VectorQuantity {
        val results = values.map {
            when(it) {
                is AADD -> it.builder.realMath { inverseSqr(it) }
                is IDD -> it.builder.intMath { inverseSqr(it) }
                else -> throw SemanticError("InverseSqr not allowed for any other type than Real or Integer")
            }
        }
        return if (values[0] is Real) fromCanonical(results, unit.sqrt()) else VectorQuantity(results)
    }

    /**
     * Applies the square to a VectorQuantity
     * Example: 100 m^2 --> 10 m
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun sqr(): VectorQuantity {
        val results = values.map {
            when(it) {
	            is AADD -> RealMath.sqr(it)
                is IDD -> IntMath.sqr(it)
	            else -> throw SemanticError("sqr() is only defined for Integers and Reals")
            }
        }
        return if (values[0] is Real) fromCanonical(results, unit.sqr()) else VectorQuantity(results)
    }

    /**
     * calculates log base e (ln) of a VectorQuantity
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun ln(): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (! unit.isNumber) throw SemanticError("Log with units is not allowed")

        return fromCanonical(values.map {
            when(it) {
                is AADD -> ln(it)
                is IDD -> ln(it)
                else -> throw SemanticError("Ln not allowed for any other type than Real or Integer")
            }
        })
    }

    /**
     * calculates log base e (ln) of a VectorQuantity
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun log2(): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if (! unit.isNumber) throw SemanticError("Log2 with units is not allowed")
        return fromCanonical(values.map {
            when(it) {
	            is AADD -> RealMath.log2(it)
                is IDD -> IntMath.log2(it)
                else -> throw SemanticError("log2 is only defined for Real and Integer")
            }
        })
    }

    /**
     * calculates log of a VectorQuantity with a given base
     * @param base base value for the logarithm
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun log(base: DD<*>): VectorQuantity {
        //Test if unit is 1, otherwise it is not possible
        if(! unit.isNumber)
            throw SemanticError("Log with units is not allowed")

        return fromCanonical(values.map {
            when(it) {
                is AADD -> when {
                    base is AADD -> log(it, base)
                    else -> throw SemanticError("Log may not mix Reals and Integers")
                }
                is IDD -> when {
                    base is IDD -> log(it, base)
                    else -> throw SemanticError("Log may not mix Reals and Integers")
                }
                else -> throw SemanticError("Log not allowed for any other type than Real or Integer")
            }
        })
    }

    fun log(bases: VectorQuantity): VectorQuantity {
        if(!bases.unit.isNumber || !unit.isNumber)
            throw SemanticError("Log with units is not allowed")

        bases.values.singleOrNull()?.let { return log(it) }

        if(bases.values.size != values.size)
            throw SemanticError("Log only possible with same-sized vectors")

        return fromCanonical((values zip bases.values).map { (v,b) ->
            when {
                (v !is Real && v !is Integer) || (b !is Real && b !is Integer) -> throw DDError("Log only possible for Integer and Real")
                v is Real && b is Real -> log(value = v, base = b)
                v is Integer && b is Integer -> log(value = v, base =b)
                else -> throw SemanticError("Log may not mix Reals and Integers")
            }
        })
    }

    /**
     * calculates exp of a VectorQuantity
     * It is used for the following function: f(x) = e^x
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun exp(): VectorQuantity {
        if (values[0] is Real && !unit.isNumber)
            throw SemanticError("Exp with units is not allowed")

        val results = values.map {
            when(it) {
                is Real -> exp(it)
                is Integer -> exp(it)
                else -> throw SemanticError("Exp only possible with Integer and Real")
            }
        }
        return VectorQuantity(results)
    }

    /**
     * Calculates Pow2 for Quantities
     * It is used for the following function: f(x) = 2^x
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun pow2(): VectorQuantity {
        if (values[0] is Real && !unit.isNumber)
            throw SemanticError("Pow2 with units is not allowed")

        val results = values.map {
            when(it) {
                is IDD -> IntMath.pow2(it)
                is AADD -> RealMath.pow2(it)
                else -> throw SemanticError("pow2 is only defined for Integers and Reals")
            }
        }
        return VectorQuantity(results)
    }

    /**
     * Calculates Pow for Quantities: f(x,y) = x^y
     * @param exponent Exponent for the Pow function
     * @return VectorQuantity with the result as a new VectorQuantity
     */
    fun pow(exponent: DD<*>): VectorQuantity {
        if (values[0] is Real && !unit.isNumber)
            throw SemanticError("Power with units is not allowed")

        val results = values.map {
            when(it) {
                is Real -> when(exponent) {
                    is Real -> RealMath.pow(value = it, exponent = exponent)
                    else -> throw SemanticError("Power may not mix Reals and Integers")
                }
                is Integer -> when(exponent) {
                    is Integer -> IntMath.pow(value = it, exponent = exponent)
                    else -> throw SemanticError("Power may not mix Reals and Integers")
                }
                else -> throw SemanticError("Power only possible with Integer and Real")
            }
        }
        return VectorQuantity(results)
    }

    /** Lifts a unary trigonometric function to quantities. The result is a plain number without unit. */
    private inline fun trig(name: String, transform: (Real) -> Real): VectorQuantity {
        if (!unit.isNumber)
            throw SemanticError("$name with units is not allowed")

        return fromCanonical(values.map {
            if (it !is Real)
                throw SemanticError("$name only possible with Real")
            transform(it)
        })
    }

    fun sin() = trig("Sin", RealMath::sin)
    fun arcsin() = trig("Arcsin", RealMath::asin)
    fun cos() = trig("Cos", RealMath::cos)
    fun arccos() = trig("Arccos", RealMath::acos)
    fun tan() = trig("Tan", RealMath::tan)
    fun arctan() = trig("Arctan", RealMath::atan)

    /**
     * Calculates the angle between two vectors of the same size.
     * @param other vector for angle calculation
     * @return Angle as a Quantity with Real value in radiant representation
     */
    fun angle(other: VectorQuantity): VectorQuantity {
        if (values.size != other.values.size)
            throw VectorDimensionError("For the angle calculation vectors must have the same size, not ${values.size} and ${other.values.size}")

        val quantity1 = if (values[0] is Integer) intAsAADDQuantity() else this
        val quantity2 = if (other.values[0] is Integer) other.intAsAADDQuantity() else other

        if (quantity1.values[0] !is Real || quantity2.values[0] !is Real)
            throw DDError("Vector operations are only supported for Integer and Real")

        val dot = quantity1.dot(quantity2)
        val squares = quantity1.dot(quantity1) * quantity2.dot(quantity2)
        // acos is infinitely steep at +-1: rounding noise of 1 ULP would give an angle of 1e-8 for parallel vectors.
        // If dot^2 = |a|^2 |b|^2 holds exactly for exact (point) values, the vectors are (anti)parallel and cos is exactly +-1.
        val dotRange = dot.aadd().getRange()
        val squaresRange = squares.aadd().getRange()
        val exactlyParallel = dotRange.min == dotRange.max && squaresRange.min == squaresRange.max &&
            dotRange.min.toDouble().let { it * it == squaresRange.min.toDouble() && it != 0.0 }
        val cosine = if (exactlyParallel)
            dot.aadd().builder.real(if (dotRange.min.toDouble() > 0) 1.0 else -1.0)
        else (dot / (quantity1.abs() * quantity2.abs())).aadd()
        return fromCanonical(acos(cosine), Unit("rad"), "rad")
    }

    private fun intAsAADDQuantity(): VectorQuantity {
        val results = values.map {
            if (it !is Integer)
                throw DDError("Only possible for Integer")

            it.builder.real(convexHull(it.asIdd().getRange()))
        }

        return fromCanonical(results, unit, unitSpec, userWantedUnitSpec)
    }

    fun norm(): VectorQuantity {
        if (values[0] is Integer)
            throw DDError("Normalizing vectors with Integers is not supported")
        return this.div(abs())
    }

    /**
     * Transforms the VectorQuantity to a String with the unit as a value and a fraction of units
     * If the VectorQuantity contains a unitSpec, the unit is transformed to this VectorQuantity before returning the string.
     * @Return a string representation of the VectorQuantity
     */
    override fun toString(): String
        = Representer.default.represent(this)

    /**
     * return first value
     */
    fun bdd(): Bool {
        if (values.size != 1)
            throw VectorDimensionError("bdd() is only supported for scalar VectorQuantity (size 1), but vector has size ${values.size}")
        return values[0] as Bool
    }

    fun aadd(): Real {
        if (values.size != 1)
            throw VectorDimensionError("aadd() is only supported for scalar VectorQuantity (size 1), but vector has size ${values.size}")
        return values[0] as Real
    }

    fun idd(): Integer {
        if (values.size != 1)
            throw VectorDimensionError("idd() is only supported for scalar VectorQuantity (size 1), but vector has size ${values.size}")
        return values[0] as Integer
    }

    /**
     * return all values
     */
    fun bdds(): List<Bool> {
        if (values.isNotEmpty() && values[0] !is Bool) {
            throw DDError("Vector elements are not of type Bool")
        }
        @Suppress("UNCHECKED_CAST")
        return values as List<Bool>
    }

    fun aadds(): List<Real> {
        if (values.isNotEmpty() && values[0] !is Real) {
            throw DDError("Vector elements are not of type Real")
        }
        @Suppress("UNCHECKED_CAST")
        return values as List<Real>
    }

    fun idds(): List<Integer> {
        if (values.isNotEmpty() && values[0] !is Integer) {
            throw DDError("Vector elements are not of type Integer")
        }
        @Suppress("UNCHECKED_CAST")
        return values as List<Integer>
    }

    /**
     * Converts this unit to the expected unit representation and returns the value of the conversion.
     * "1" (or "") is the dimensionless unit: 50 % in "1" is 0.5, and 5 km in "1" is an error.
     * For the plain SI values of any quantity use [valuesInSI].
     * @param wantedRepresentation String of the wanted representation of the Unit
     * @throws TransformationError if the quantity cannot be given in the wanted unit
     * @Return value in Real/Integer of the result
     */
    fun valuesIn(wantedRepresentation: String): List<DD<*>> {
        if (values.isEmpty()) return emptyList()
        if (values[0] !is Real) return values

        val expectedUnit = Unit(wantedRepresentation)
        if (!unit.isCompatibleWith(expectedUnit))
            throw TransformationError("$expectedUnit and $unit")
        if (expectedUnit.isLogarithmic) {
            return values.map {
                val ten = it.builder.real(10.0)
                ten * ln(it.asAadd()) / ln(ten)
            }
        }

        // the dimensionless unit "1" is the SI unit of all quantities without dimension (%, rad, ...)
        if (expectedUnit.toString() == "1" || unit.toString() == "?") return values

        // Special case for absolute temperature conversion from K to °C/°F, which have an offset.
        // Compound units (J/°C), powers and temperature differences are only scaled (see below).
        val unit1 = unit.singleTemperature
        val unit2 = expectedUnit.singleTemperature
        if (unit1 != null && unit2 != null && unit2.hasOffset && !unit.isDifference) {
            val factor = unit1.prefix.factor
            return values.map { unit1.convertTo(it * factor, unit2) }
        }

        // Vectors always have exactly one unit across all elements:
        // calculate the scale of the expected unit once outside the loop
        val temporaryExpected = VectorQuantity(values[0].builder.real(1.0), expectedUnit.copy(isDifference = true))
        val correlationFac = temporaryExpected.value
        return values.map { it.div(correlationFac) }
    }

    /** The values in SI base units (m, kg, s, K, ...), as they are stored. No conversion and no unit check. */
    fun valuesInSI(): List<DD<*>> = values

    /** The values in the displayed unit ([unitSpec]), or in SI if there is none. */
    fun valuesInUnitSpec(): List<DD<*>> = if (unitSpec.isEmpty()) valuesInSI() else valuesIn(unitSpec)

    fun getDomain(): String {
        return unit.effectiveDomain()
    }

    /**
     * Intersects a VectorQuantity with another VectorQuantity of the same property
     * (e.g., upVectorQuantity with downVectorQuantity)
     * @param q Intersect the current VectorQuantity with this VectorQuantity
     * @return Intersected VectorQuantity as a new VectorQuantity
     **/
    fun intersect(q: VectorQuantity): VectorQuantity {
        if (values.size != q.values.size) {
            throw VectorDimensionError("Not possible to intersect VectorQuantities of different size (${values.size} and ${q.values.size})")
        }
        if (!isReal) {
            val resultingValues = values.indices.map { i ->
                when (val v = values[i]) {
                    is Integer -> v.asIdd() intersect q.values[i].asIdd()
                    is Bool -> v.asBdd() intersect q.values[i].asBdd()
                    else -> throw DDError("Intersection only possible for Integer, Bool, and ADD")
                }
            }
            return VectorQuantity(resultingValues)
        }
        if (unit.toString() == "?") return this
        val resultingValues = values.indices.map { i -> values[i].asAadd() intersect q.values[i].asAadd() }
        return fromCanonical(resultingValues, unit, unitSpec, userWantedUnitSpec = this.userWantedUnitSpec || q.userWantedUnitSpec)
    }

    fun constrainString(stringSpecs: List<String>): VectorQuantity {
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

    fun constrainString(quantity: VectorQuantity): VectorQuantity {
        val resultingValues = mutableListOf<DD<*>>()
        if (values.size == quantity.values.size)
            values.indices.forEach {
                val str = values[it].asStrDD().toString()
                val qStr = quantity.values[it].toString()
                if (str.isEmpty())
                    resultingValues.add(values[it].builder.string(qStr))
                else if (qStr == "String")
                    resultingValues.add(values[it])
                else if (qStr == str)
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
    fun constrain(intSpecs: MutableList<IntegerRange>): VectorQuantity {
        val resultingValues = mutableListOf<Integer>()
        if (values.size == intSpecs.size)
            values.indices.forEach { resultingValues.add(values[it].asIdd() intersect values[it].builder.integer(intSpecs[it])) }
        else
            if (intSpecs.size == 1 && intSpecs[0] == IntegerRange.All) // Special case, if there is no definition of boolSpec, use always rangeSpecs[0]
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
    fun constrain(q: VectorQuantity, rangeSpecs: List<RealRange>, unitSpec: String): VectorQuantity {
        if (values.size != q.values.size)
            throw VectorDimensionError("Not possible to intersect VectorQuantities of different size (${values.size} and ${q.values.size})")

        if (unitSpec != "" && !unit.isCompatibleWith(VectorQuantity(values.map { it.asAadd() }, Unit(unitSpec)).unit))
            throw TransformationError("$unit cannot be transferred to ${Unit(unitSpec)}")

        // calculate intersection of propagated and specified values
        val resultingValues = mutableListOf<AADD>()
        if (values.size == rangeSpecs.size)
            values.indices.forEach {
                if (rangeSpecs[it] == RealRange.Reals)
                    resultingValues.add(values[it].asAadd())
                else
                    resultingValues.add(values[it].asAadd().constrainTo(VectorQuantity(values[0].builder.real(rangeSpecs[it]), Unit(unitSpec)).getRange()))
            }
        else
            if (rangeSpecs.size == 1 && rangeSpecs[0] == RealRange.Reals) // Special case, if there is no definition of rangeSpecs, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asAadd()) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${rangeSpecs.size}")

        for (i in values.indices) {
            val qVal = q.values[i]
            if (qVal.isFeasible() && qVal is Real && !qVal.isEmpty()) {
                if (! (qVal.min.isInfinite && qVal.max.isInfinite) ) {
                    resultingValues[i] = resultingValues[i].asAadd().constrainTo(qVal)
                }
            }
        }
        val isUserWanted = this.userWantedUnitSpec || q.userWantedUnitSpec
        val canonical = canonicalize(resultingValues, unit, unitSpec, isUserWanted)
        return fromCanonical(canonical.values, canonical.unit, unitSpec, isUserWanted)
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
                    resultingValues[i] = (values[i].asAadd().constrainTo((q.values[i] as Real).getRange()))
                }
                val resultingUnit = if (unit.toString() == "?") q.unit else unit
                return fromCanonical(resultingValues, resultingUnit, unitSpec, userWantedUnitSpec = this.userWantedUnitSpec || q.userWantedUnitSpec)
            }

            is Integer -> {
                for (i in values.indices) {
                    resultingValues[i] = (values[i].asIdd() constrainTo (q.values[i] as Integer).getRange())
                }
                return VectorQuantity(resultingValues)
            }

            else -> throw SemanticError("Constrain only for Integer and Real")
        }
    }

    /**
     * Returns a new quantity constrained to spec.
     * @param constraints: the constraints to be applied.
     * @param isBoolean Shows that the constraint function should work with boolean. Needed for compiler to separate it from Integer constrain
     * @return a new quantity that is q, constrained to constraint.
     */
    fun constrain(constraints: MutableList<XBool>, isBoolean: Boolean): VectorQuantity {
        val resultingValues = mutableListOf<Bool>()
        if (values.size == constraints.size)
            values.indices.forEach { resultingValues.add(values[it].asBdd() intersect values[it].builder.constant(constraints[it])) }
        else
            if (constraints.size == 1 && constraints[0] == XBool.All) // Special case, if there is no definition of boolSpec, use always rangeSpecs[0]
                values.indices.forEach { resultingValues.add(values[it].asBdd() intersect values[0].builder.constant(constraints[0])) }
            else
                throw VectorDimensionError("Vector size of ${values.size} does not match Constraint size of ${constraints.size}")

        return VectorQuantity(resultingValues)
    }

    /**
     * Compares value types, exact ranges or values, and SI dimensions.
     * Display metadata does not affect equality.
     * @param other the other object
     * @return true, if equal
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VectorQuantity || type() != other.type() || values.size != other.values.size)
            return false
        if (isReal && !unit.hasSameDimension(other.unit)) return false
        return values.indices.all { valueKey(values[it]) == valueKey(other.values[it]) }
    }

    /** Compares quantities with an explicit tolerance rather than object equality. */
    fun isApproximatelyEqualTo(other: VectorQuantity, tolerance: Tolerance = equalsTolerance): Boolean =
        tolerance.areEqual(this, other)

    private fun valueKey(value: DD<*>): Any = when (value) {
        is AADD -> value.getRange()
        is IDD -> value.getRange()
        is BDD -> value.value
        is StrDD -> value.toString()
    }

    fun contains(other: VectorQuantity): Boolean {
        if (values.size != other.values.size)
            throw VectorDimensionError("Not possible to compare VectorQuantities of different size (${values.size} and ${other.values.size})")
        for (i in values.indices) {
            when (val v = values[i]) {
                is Real -> {
                    if (!v.asAadd().contains(other.values[i].asAadd())) return false
                }
                is Integer -> {
                    if (!v.asIdd().contains(other.values[i].asIdd())) return false
                }
                else -> throw DDError("Unsupported value type for contains, only Real and Integer are supported")
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = type().hashCode()
        for (value in values) result = 31 * result + valueKey(value).hashCode()
        if (isReal) result = 31 * result + unit.hashCode()
        return result
    }

    /**
     * Length of vector
     * @return VectorQuantity with length and unit of vector
     */
    fun abs(): VectorQuantity {
        return when (values[0]) {
            is Integer -> {
                var squareSum = values[0].builder.integer(0)
                values.forEach {
                    val min = it.asIdd().min
                    val max = it.asIdd().max
                    val absoluteValue = if (min <= 0 && max >= 0)
                        it.builder.integer(0L .. max(abs(min), max))
                    else if (max < 0)
                        it.builder.integer(abs(max)..abs(min))
                    else
                        it.builder.integer(min..max)
                    IntMath.run { squareSum += sqr(absoluteValue) }
                }
                VectorQuantity(abs(sqrt(squareSum)))
            }

            is Real -> {
                var squareSum = values[0].builder.real(0.0)
                values.forEach {
                    val min = it.asAadd().min
                    val max = it.asAadd().max
                    val absoluteValue = if (min <= 0.0 && max >= 0.0)
                        it.builder.real(0.0 .. max(abs(min), max))
                    else if (max < 0.0)
                        it.builder.real(abs(max) .. abs(min))
                    else
                        it.builder.real(min..max)
                    RealMath.run { squareSum += sqr(absoluteValue) }
                }
                if (squareSum.min < 0.0) {
                    // a sum of squares is never negative: clip numerical noise below zero,
                    // because sqrt of negative values is not possible
                    squareSum = values[0].builder.real(0.0..squareSum.max)
                }
                fromCanonical(RealMath.sqrt(squareSum), unit, unitSpec, userWantedUnitSpec)
            }

            else -> throw DDError("Unsupported value for abs: $values")
        }
    }

    fun size(): VectorQuantity {
        return VectorQuantity(values[0].builder.integer(values.size.toLong()))
    }

    /**
     * Length of vector using the city block distance (Manhattan Distance)
     * @return VectorQuantity with length and unit of vector
     */
    fun cityBlockDistance(param: VectorQuantity): VectorQuantity {
        return when (values[0]) {
            is Integer -> {
                val connectingVector = this - param
                var sum = values[0].builder.integer(0)
                connectingVector.values.forEach {
                    val min = it.asIdd().min
                    val max = it.asIdd().max
                    val absoluteValue = when {
                        min <= 0 && max >= 0 -> it.builder.integer(0L..max(abs(min),max))
                        max < 0 -> it.builder.integer(abs(max)..abs(min))
                        else -> it.builder.integer(min..max)
                    }
                    sum.builder.intMath {
                        sum += absoluteValue
                    }
                }
                VectorQuantity(sum)
            }

            is Real -> {
                val connectingVector = this - param
                var sum = values[0].builder.real(0.0)
                connectingVector.values.forEach {
                    val min = it.asAadd().min
                    val max = it.asAadd().max
                    val absoluteValue = if (min <= 0.0 && max >= 0.0)
                        it.builder.real(0.0..max(abs(min), max))
                    else if (max < 0.0)
                        it.builder.real(abs(max)..abs(min))
                    else
                        it.builder.real(min..max)
                    RealMath.run {
                        sum += absoluteValue
                    }
                }// a sum of absolute values is never negative: clip numerical noise below zero
                if (sum.min < 0.0) sum = values[0].builder.real(0.0..sum.max)
                fromCanonical(sum, unit, unitSpec, userWantedUnitSpec)
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
        if (!isBool) throw BDDError("Boolean \"and\" can only be applied to BDDs")
        return VectorQuantity(values.indices.map { values[it].asBdd() and quantity.values[it].asBdd() })
    }

    /**
     * Applies boolean "or" operation
     * @return VectorQuantity with the with result as a new VectorQuantity
     */
    infix fun or(quantity: VectorQuantity): VectorQuantity {
        if (!isBool) throw BDDError("Boolean \"or\" can only be applied to BDDs")
        return VectorQuantity(values.indices.map { values[it].asBdd() or quantity.values[it].asBdd() })
    }

    fun asQuantity(): VectorQuantity {
        if (!isScalar)
            throw VectorDimensionError("Transform to Quantity only possible for Vectors of size 1, not of size ${values.size}")
        return this
    }

    /**
     * @return the min value of the Range as Double of the first value
     */
    fun getMinAsDouble(): Double = getRange().min.toDouble()

    /**
     * @return the max value of the Range as Double of the first value
     */
    fun getMaxAsDouble(): Double = getRange().max.toDouble()

    fun getRange(): RealRange {
        if (!isScalar) throw VectorDimensionError("getRange() is only supported for scalars")

        return when(val v = values[0]) {
            is AADD -> v.getRange()
            is IDD -> convexHull(v.getRange())
            is StrDD, is BDD -> throw SemanticError("getRange() is only defined for integers or reals")
        }
    }

    fun getIntRange(): IntegerRange {
        if (!isScalar) throw VectorDimensionError("getIntRange() is only supported for scalars")

        return when(val v = values[0]) {
            is AADD -> roundOutwards(v.getRange())
            is IDD -> v.getRange()
            is StrDD, is BDD -> throw SemanticError("getIntRange() is only defined for integers or reals")
        }
    }

    fun valueIn(wantedRepresentation: String): DD<*> {
        if (!isScalar) throw VectorDimensionError("valueIn is only supported for scalar VectorQuantity (size 1)")
        return valuesIn(wantedRepresentation)[0]
    }

    fun constrain(intSpec: IntegerRange): VectorQuantity {
        if (!isScalar) throw VectorDimensionError("constrain(IntegerRange) is only supported for scalar VectorQuantity (size 1)")
        if (unit.toString() == "?") return this.clone()
        return VectorQuantity(value.asIdd().constrainTo(intSpec))
    }

    fun constrain(constraint: XBool): VectorQuantity {
        if (!isScalar) throw VectorDimensionError("constrain(XBool) is only supported for scalar VectorQuantity (size 1)")
        return VectorQuantity((value.builder.constant(constraint) intersect this.value) as Bool)
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
            if (it is Bool && it.value != XBool.All)
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
fun max(a: VectorQuantity, b: VectorQuantity): VectorQuantity {
    if (!a.isScalar || !b.isScalar)
        throw VectorDimensionError("max only possible for Vectors of size 1, not of size ${a.values.size} and ${b.values.size}")
    val af = a.ge(b).bdd().ite(a, b)
    val max = max(a.getMaxAsDouble(), b.getMaxAsDouble())
    val min = max(a.getMinAsDouble(), b.getMinAsDouble())
    return when (af.value) {
        is Real -> VectorQuantity.fromCanonical(af.aadd().constrainTo(RealRange(min, max)), af.unit, af.unitSpec, af.userWantedUnitSpec)
        is Integer -> VectorQuantity(af.idd().constrainTo(IntegerRange(min, max)))
        else -> throw SemanticError("Expect parameters of max to be Real or Integer.")
    }
}

/**
 * Returns the minimum of two quantities a, b.
 */
fun min(a: VectorQuantity, b: VectorQuantity): VectorQuantity {
    if (!a.isScalar || !b.isScalar)
        throw VectorDimensionError("min only possible for Vectors of size 1, not of size ${a.values.size} and ${b.values.size}")
    val af = a.ge(b).bdd().ite(b, a)
    val max = min(a.getMaxAsDouble(), b.getMaxAsDouble())
    val min = min(a.getMinAsDouble(), b.getMinAsDouble())
    return when (af.value) {
        is Real -> VectorQuantity.fromCanonical(af.aadd().constrainTo(RealRange(min, max)), af.unit, af.unitSpec, af.userWantedUnitSpec)
        is Integer -> VectorQuantity(af.idd().constrainTo(IntegerRange(min, max)))
        else -> throw SemanticError("Expect parameters of min to be Real or Integer.")
    }
}

fun Bool.ite(t: VectorQuantity, e: VectorQuantity): VectorQuantity {
    t.requireSameDimension(e)
    val results = t.values.indices.map { this.ite(t.values[it], e.values[it]) }
    return if (t.isReal) {
        VectorQuantity.fromCanonical(results, t.unit, t.unitSpec, t.userWantedUnitSpec || e.userWantedUnitSpec)
    } else {
        VectorQuantity(results)
    }
}
