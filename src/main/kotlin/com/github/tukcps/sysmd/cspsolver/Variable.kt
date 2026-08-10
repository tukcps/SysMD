@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.cspsolver

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.Range
import io.github.tukcps.aadd.values.XBool
import kotlin.uuid.Uuid


/**
 * The ValueFeature is a replacement for the ValueFeature class (Versions before 2.6.15).
 * ValueFeature combines the KerML metamodel classes in a single class:
 * - Feature that models a value.
 * - Expression that models some expressions.
 * - ValueFeature, a relationship that links the value of a Feature with an Expression.
 */
interface Variable: ConstraintPropagation {
    enum class BaseType {Bool, Int, String, Real, Unknown}

    val solver: Solver

    val relatedElement: Uuid?
    // @Deprecated("Replace with relatedElement", ReplaceWith("relatedElement"))
    val path: String

    val baseType: BaseType
    val satisfyAll: Boolean
    val domain: String?
    var expression: String?

    /** Holds either an IntegerRange, a Range, or an XBool, depending on the type. */
    val unitSpec:   String       // Specified unit as string

    /** access methods for the valueSpec field; returns different types */
    var rangeSpecs: MutableList<Range>
    var boolSpecs: MutableList<XBool>
    var intSpecs: MutableList<IntegerRange>
    val stringSpecs: MutableList<String>

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
    fun compileExpression()
    fun checkForCyclicDependency()

    /**
     * Methods to get the result as Number.
     */
    fun <T: Number> min(index: Int = 0): T
    fun <T: Number> max(index: Int = 0): T
    fun <T: Comparable<T> > range(index: Int = 0): ClosedRange<T>
    fun bool(): XBool
}
