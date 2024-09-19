@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.cspsolver

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.Range
import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.parser.size
import com.github.tukcps.sysmd.quantities.VectorQuantity
import java.util.*


/**
 * The ValueFeature is a replacement for the ValueFeature class (Versions before 2.6.15).
 * ValueFeature combines the KerML metamodel classes in a single class:
 * - Feature that models a value.
 * - Expression that models some expressions.
 * - ValueFeature, a relationship that links the value of a Feature with an Expression.
 */
interface Variable: ConstraintPropagation {
    enum class BaseType {Bool, Int, Str, Real, Unknown}

    var feature: Feature
    var elementId: UUID
    var name: String?
    val baseType: BaseType

    /** Holds either an IntegerRange, a Range, or an XBool, depending on the type. */
    var valueSpecs:  MutableList<Any?>
    val unitSpec:   String       // Specified unit as string

    /*  For quantities, constraints, performances: an equation as text, right side of eqn for parser! */
    var dependency:   String     // equation as text, right side of eqn for parser!

    /** access methods for the valueSpec field; returns different types */
    val rangeSpecs: MutableList<Range>
        get() = if(valueStr.size()>0) valueSpecs as MutableList<Range> else mutableListOf(Range.Reals)

    val boolSpecs: MutableList<XBool>
        get() = if(valueStr.size()>0) valueSpecs as MutableList<XBool> else mutableListOf(XBool.X)

    val intSpecs: MutableList<IntegerRange>
        get() = if(valueStr.size()>0) valueSpecs as MutableList<IntegerRange> else mutableListOf(IntegerRange.Integers)

    val stringSpecs: MutableList<String>
        get() = if(valueStr.size()>0) valueSpecs as MutableList<String> else mutableListOf("")

    /** A getter for a string representation of the value, with field for serialization. */
    @get:JsonIgnore
    var valueStr: String

    /** indicator for constraint propagation that shows stability in iterations. */
    var stable: Boolean                 // For use in numerical iterations

    /** value and unit for constraint propagation */
    var vectorQuantity : VectorQuantity   // actually possible values; intersection of up/downValue

    val isVectorQuantityInitialized: Boolean

    /** old value and unit for constraint propagation, to detect stability */
    var oldVectorQuantity: VectorQuantity?   // previous quantity for event detection

    /** The AstRoot for computation of the ValueFeature. */
    var ast: AstRoot?

    /**
     * This method initializes the transient fields based on the specified value and unit
     */
    fun initVectorQuantity(): Variable

    /** Setter from a boolean string representation */
    fun boolSpec(str: String?): Variable

    /** Setter for a real ValueFeature */
    fun rangeSpec(lbs: String?, ubs: String?): Variable

    /** Setter for a real ValueFeature */
    fun rangeSpec(lb: Double?, ub: Double?): Variable

    /** Setter for a real ValueFeature */
    fun rangeSpec(init : Range): Variable

    /** Setter for range specification that also initializes the value */
    fun intSpec(init: IntegerRange) : Variable


    /** Casts the quantity value to AADD and returns it. */
    fun aadd(): AADD
    fun bdd(): BDD
    fun idd(): IDD
    fun min(): Double
    fun max(): Double
}
