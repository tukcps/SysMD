package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.aadd.StrDD
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.Range
import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.exceptions.ExpressionError
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.exceptions.TypeExpected
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.report
import java.util.*


@Suppress("UNCHECKED_CAST")
open class VariableImplementation (
    override var feature: Feature,
    override val baseType: BaseType = BaseType.Unknown,
    override var updated: Boolean = true,
    override var hasBeenChanged: Boolean = true
): Variable {

    override var name: String? = null
        get() = if (field == null) feature.qualifiedName else field
        set(value) { field = value }

    var variable: Variable? = this

    val qualifiedName
        get() = feature.qualifiedName

    override var elementId: UUID
        get() = feature.elementId
        set(value) = TODO("Not needed and should not be used")

    override var dependency: String
        get() = feature.expression?:""
        set(value) { TODO("Not needed and should not be used") }

    override var valueSpecs: MutableList<Any?> = mutableListOf()

    override val unitSpec: String
        get() = feature.unitConstraint?:""

    /** access methods for the valueSpec field; returns different types */
    override val rangeSpecs: MutableList<Range>
        get() = if(valueSpecs.size>0 && valueSpecs[0]!=null) valueSpecs as MutableList<Range> else mutableListOf(Range.Reals)

    override val boolSpecs: MutableList<XBool>
        get() {
            if (valueSpecs.firstOrNull() is XBool)
                return if(valueSpecs.firstOrNull() !=null) valueSpecs as MutableList<XBool> else mutableListOf(XBool.X)
            else
                throw SysMDError(message = "Invalid bool spec", element = feature)
        }

    override val intSpecs: MutableList<IntegerRange>
        get() = if(valueSpecs.size>0 && valueSpecs[0]!=null) valueSpecs as MutableList<IntegerRange> else mutableListOf(IntegerRange.Integers)

    /** A getter for a string representation of the value, with field for serialization. */
    override var valueStr: String = ""
        get() {
            field = vectorQuantity.values.toString()
            return field
        }

    /** indicator for constraint propagation that shows stability in iterations. */
    override var stable: Boolean = false          // For use in numerical iterations

    /** value and unit for constraint propagation */
    override lateinit var vectorQuantity : VectorQuantity   // actually possible values; intersection of up/downValue

    override val isVectorQuantityInitialized: Boolean by lazy { this::vectorQuantity.isInitialized }

    /** old value and unit for constraint propagation, to detect stability */
    override var oldVectorQuantity: VectorQuantity? = null  // previous VectorQuantity for event detection

    /**
     * The AstRoot for computation of the variable.
     * We keep it in the feature of the model.
     */
    override var ast: AstRoot?
        get() = feature.featureWithValue as AstRoot?
        set(value) { feature.featureWithValue = value}

    /**
     * This method initializes the transient fields based on the specified value and unit
     */
    override fun initVectorQuantity(): Variable {
        if (feature.model == null)
            throw ExpressionError(msg="INTERNAL: Attempt to initialize variable without model", element=feature)
        else
            when  {
                // must go to feature, classifier
                feature.type.firstOrNull()?.ref !is Type -> {
                    feature.model!!.report(TypeExpected("expected specialization of ScalarValue, but got: '${feature.type.firstOrNull()?.str}'", element=feature))
                    valueSpecs = mutableListOf(Range.Reals)
                    val values = mutableListOf<AADD>()
                    rangeSpecs.forEach{values.add(feature.model!!.builder.range(it,elementId.toString()))}
                    vectorQuantity = VectorQuantity(values)
                }

                baseType == BaseType.Real -> {
                    if (feature.typeConstraint.isNotEmpty()) {
                        valueSpecs = if (feature.typeConstraint[0] == "Real" || feature.typeConstraint.isEmpty()) {
                            mutableListOf(Range.Reals)
                        } else {
                            val ranges = mutableListOf<Any?>()
                            feature.typeConstraint.forEach { if (it.isNotBlank()) ranges.add(Range(it)) else ranges.add(Range.Reals)}
                            ranges
                        }
                    }
                    val values = mutableListOf<AADD>()
                    rangeSpecs.forEach{values.add(feature.model!!.builder.range(it,elementId.toString()))}
                    vectorQuantity = VectorQuantity(values, unitSpec)
                }

                baseType == BaseType.Int -> {
                    if (feature.typeConstraint.isNotEmpty()) {
                        val ranges = mutableListOf<Any?>()
                        feature.typeConstraint.forEach {
                            ranges.add(IntegerRange(it.trim('[', ']', ' ')))
                        }
                        valueSpecs = ranges
                    }
                    val values = mutableListOf<IDD>()
                    intSpecs.forEach{ values.add(feature.model!!.builder.range(it)) }
                    vectorQuantity = VectorQuantity(values)
                }

                baseType == BaseType.Bool -> {
                    val values = mutableListOf<BDD>()
                    val ranges = mutableListOf<Any?>()
                    if(feature.typeConstraint.isEmpty()){
                        ranges.add(XBool.X)
                        values.add(feature.model!!.builder.variable(elementId.toString(), elementId.toString()))
                    }
                    feature.typeConstraint.forEach {
                        when (it.trim()) {
                            "True", "true" -> {
                                ranges.add(XBool.True)
                                values.add(feature.model!!.builder.True)
                            }

                            "False", "false" -> {
                                ranges.add(XBool.False)
                                values.add(feature.model!!.builder.False)
                            }

                            "null", "X", "Unknown" -> {
                                ranges.add(XBool.X)
                                values.add(feature.model!!.builder.variable(elementId.toString(), elementId.toString()))
                            }

                            else ->  // throw Error("Forbidden boolSpec value in ValueFeature $id: $valueSpec")
                            {
                                ranges.add(XBool.X)
                                values.add(feature.model!!.builder.variable(elementId.toString(), elementId.toString()))
                            }
                        }
                    }
                    valueSpecs = ranges
                    vectorQuantity = VectorQuantity(values)
                }

                baseType == BaseType.Str ->{
                    val values = mutableListOf<StrDD>()
                    if(valueSpecs.isEmpty())
                        values.add(feature.model!!.builder.Strings)
                    valueSpecs.forEach{ values.add(feature.model!!.builder.string(it as String)) }
                    vectorQuantity = VectorQuantity(values)
                }

                else -> {
                    feature.model!!.report(feature, "no suitable type found for value $qualifiedName; assuming Real")
                    vectorQuantity = Quantity(feature.model!!.builder.Reals, unitSpec)
                }
            }
        stable = false
        updated = true
        oldVectorQuantity = vectorQuantity.clone()
        return this
    }


    /** Setter from a boolean string representation */
    override fun boolSpec(str: String?): Variable {
        updated = true
        stable = false
        if (str == null) return this
        when(str.toString().trim().lowercase(Locale.US)) {
            "true"          -> valueSpecs = mutableListOf(XBool.True)
            "false"         -> valueSpecs = mutableListOf(XBool.False)
            "x", "unknown"  -> valueSpecs = mutableListOf(XBool.X)
            "nab"           -> valueSpecs = mutableListOf(XBool.NaB)
            ""              -> { /* no update */ }
            else -> throw ExpressionError("boolean constraint must be true, false or x/unknown.")
        }
        return this
    }

    /** Setter for a real ValueFeature */
    override fun rangeSpec(lbs: String?, ubs: String?): Variable {
        updated = true
        if (lbs == null || ubs == null) return this

        var lb = -Double.MAX_VALUE
        var ub = Double.MAX_VALUE

        if (lbs.isNotBlank()) lb = lbs.toDouble()
        if (ubs.isNotBlank()) ub = ubs.toDouble()

        if (lb > ub)
            throw ExpressionError("in range subtype constraint, lower bound must be less or equal upper bound.")
        valueSpecs = mutableListOf(Range(lb, ub))
        return this
    }

    /** Setter for a real ValueFeature */
    override fun rangeSpec(lb: Double?, ub: Double?): Variable {
        updated = true
        if (lb == null || ub == null) return this
        if (lb > ub)
            throw ExpressionError("in range subtype constraint, lower bound must be less or equal upper bound.")
        valueSpecs = mutableListOf(Range(lb, ub))
        return this
    }

    /** Setter for a real ValueFeature */
    override fun rangeSpec(init : Range): Variable {
        updated = true
        if (init.min > init.max)
            throw ExpressionError("in range subtype constraint, lower bound must be less or equal upper bound.")
        valueSpecs = mutableListOf(Range(init.min, init.max))
        return this
    }


    /** Setter for range specification that also initializes the value */
    override fun intSpec(init: IntegerRange) : Variable {
        updated = true
        if (init.min > init.max)
            throw ExpressionError("in integer range subtype constraint, lower bound must be less or equal upper bound.")
        valueSpecs = mutableListOf(IntegerRange(init.min, init.max))
        return this
    }


    /**
     * Casts the quantity value to AADD and returns it.
     */
    override fun aadd(): AADD {
        if (vectorQuantity.values[0] is AADD) return vectorQuantity.valuesIn(unitSpec)[0] as AADD
        else throw SysMDError("Expression value cannot be cast to AADD", element = feature)
    }

    override fun bdd(): BDD {
        if (vectorQuantity.values[0] is BDD) return vectorQuantity.values[0] as BDD
        else
            throw SysMDError("Expression value cannot be cast to BDD", element = feature)
    }

    override fun idd(): IDD {
        if (vectorQuantity.values[0] is IDD) return vectorQuantity.values[0] as IDD
        else throw SysMDError("Expression value cannot be cast to IDD", element = feature)
    }


    /** Creates a compact string, skipping fields not relevant, incl. doc */
    override fun toString(): String =
        "Variable { feature=${feature.qualifiedName}, type=$baseType, unitSpec=$unitSpec, valueSpecs=$valueSpecs, value = $vectorQuantity }"

    /**
     * Returns min of first value of VectorQuantity
     */
    override fun min(): Double =
        when (vectorQuantity.values[0]) {
            is AADD -> (vectorQuantity.valuesIn(unitSpec)[0] as AADD).getRange().min
            is IDD -> (vectorQuantity.valuesIn(unitSpec)[0] as IDD).getRange().min.toDouble()
            else -> throw SemanticError(".min can only be applied on properties of type Integer or Real", element=feature)
        }

    /**
     * Returns max of VectorQuantity's first value
     */
    override fun max(): Double =
        when (vectorQuantity.values[0]) {
            is AADD -> (vectorQuantity.valuesIn(unitSpec)[0] as AADD).getRange().max
            is IDD -> (vectorQuantity.valuesIn(unitSpec)[0] as IDD).getRange().max.toDouble()
            else -> throw InternalError(".max can only be applied on properties of type Integer or Real")
        }

    override fun updateFrom(template: Element) {
        TODO()
    }
}
