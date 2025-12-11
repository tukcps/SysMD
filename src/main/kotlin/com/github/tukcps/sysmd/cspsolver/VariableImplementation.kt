package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.exceptions.ExpressionError
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.expression.functions.AstByImplements
import com.github.tukcps.sysmd.model.expression.functions.AstByParts
import com.github.tukcps.sysmd.model.expression.functions.AstBySpecializations
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.UnresolvedFeature
import com.github.tukcps.sysmd.model.kerml.implementation.DataTypeImplementation
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.UnitDomainError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.StrDD
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.Range
import io.github.tukcps.aadd.values.XBool
import java.util.*
import kotlin.collections.contains


/**
 * A variable in the system of inequations / constraints
 * @param membership the membership that links the feature and its element
 */
@Suppress("UNCHECKED_CAST")
open class VariableImplementation (
    override var membership: Membership,
    override var builder: DDBuilder,
    override val baseType: BaseType = BaseType.Unknown,
): Variable {

    override var updated: Boolean = true
    override var hasBeenChanged: Boolean = true

    override val name: String?
        get() {
            if (membership.owningNamespace?.qualifiedName == null) return membership.memberName
            return membership.owningNamespace?.qualifiedName + "::" + (membership.memberName?:membership.memberShortName)
        }

    override val feature: Feature
         = membership.memberElement as Feature

    // The elementId is used to link the variable with an element
    override val elementId: UUID?
         = membership.elementId

    // A list in which the elements of the (Array) Variable may lie.
    override var valueSpecs: MutableList<Any?> = mutableListOf()

    /**
     * The constraints on the unit, given as a owned feature 'unit'.
     * As it is an expression-string, we have to remove the quotation marks.
     */
    override val unitSpec: String =
    // override val unitSpec: String =
        feature.features().firstOrNull { it.name == "unit"}?.expression?.trim('"', ' ')?:""

    /** access methods for the valueSpec field; returns different types */
    override val rangeSpecs: MutableList<Range>
        get() = if(valueSpecs.isNotEmpty() && valueSpecs[0]!=null) valueSpecs as MutableList<Range> else mutableListOf(Range.Reals)

    override val boolSpecs: MutableList<XBool>
        get() {
            if (valueSpecs.firstOrNull() is XBool)
                return if(valueSpecs.firstOrNull() !=null) valueSpecs as MutableList<XBool> else mutableListOf(XBool.X)
            else
                throw SysMDError(message = "Invalid bool spec", element = feature)
        }

    override val intSpecs: MutableList<IntegerRange>
        get() = if(valueSpecs.isNotEmpty() && valueSpecs[0]!=null) valueSpecs as MutableList<IntegerRange> else mutableListOf(IntegerRange.Integers)

    /** A getter for a string representation of the value, with field for serialization. */
    override var valueStr: String = ""
        get() {
            field = if (vectorQuantity.values.size > 1)
                vectorQuantity.values.toString()
            else
                vectorQuantity.value.toString()
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
     * We keep it in the feature of the model. --> TODO
     * We keep it as part of the variable (related to a membership), NOT the feature.
     */
    override var ast: AstRoot? = null


    /**
     * This method initializes the transient fields based on the specified value and unit
     */
    override fun initVectorQuantity(): Variable {
        when (baseType) {
            BaseType.Real -> {
                if (feature.typeConstraint.isNotEmpty()) {
                    valueSpecs = if (feature.typeConstraint[0] == "Real" || feature.typeConstraint.isEmpty()) {
                        mutableListOf(Range.Reals)
                    } else {
                        val ranges = mutableListOf<Any?>()
                        feature.typeConstraint.forEach {
                            if (it.isNotBlank())
                                ranges.add(Range(it))
                            else
                                ranges.add(Range.Reals)
                        }
                        ranges
                    }
                }
                val values = mutableListOf<AADD>()
                rangeSpecs.forEach{values.add(builder.real(it,elementId.toString()))}
                //Test, if there is a domain (in namespace SI) defined in the definition of the


                if(feature.type.firstOrNull()==null)
                    throw UnitDomainError("No domain or domain not from ISQ, Quantities, Ranges or ScalarValues defined for attribute " + feature.name)
                var unitDomain = feature.type.firstOrNull { it.owner?.declaredName == "ISQ" || it.owner?.declaredName == "Quantities"
                        || it.owner?.declaredName == "Ranges"  || it.owner?.declaredName == "ScalarValues"}?.qualifiedName  ?: feature.type.first().qualifiedName!!
                unitDomain = unitDomain.replace("ISQ::","").replace("Quantity::","")
                    .replace("Ranges::","").replace("ScalarValues::","")
                if(!unitDomain.contains("Vector") && values.size>1)
                    throw UnitDomainError("Vectors only allowed with type a type extended from Quantities::VectorQuantityValue")
                vectorQuantity = VectorQuantity(values, unitSpec, unitDomain)

            }
            BaseType.Int -> {
                if (feature.typeConstraint.isNotEmpty()) {
                    val ranges = mutableListOf<Any?>()
                    feature.typeConstraint.forEach {
                        ranges.add(IntegerRange(it.trim('[', ']', ' ')))
                    }
                    valueSpecs = ranges
                }
                val values = mutableListOf<IDD>()
                intSpecs.forEach{ values.add(builder.integer(it)) }
                vectorQuantity = VectorQuantity(values)
            }
            BaseType.Bool -> {
                val values = mutableListOf<BDD>()
                val ranges = mutableListOf<Any?>()
                if(feature.typeConstraint.isEmpty()){
                    ranges.add(XBool.X)
                    values.add(builder.variable(elementId.toString(), elementId.toString()))
                }
                if (feature is Invariant) {
                    if ((feature as Invariant).isNegated)
                        feature.typeConstraint = mutableListOf("False")
                    else
                        feature.typeConstraint = mutableListOf("True")
                }
                feature.typeConstraint.forEach {
                    when (it.trim()) {
                        "True", "true" -> {
                            ranges.add(XBool.True)
                            values.add(builder.True)
                        }

                        "False", "false" -> {
                            ranges.add(XBool.False)
                            values.add(builder.False)
                        }

                        "null", "X", "Unknown" -> {
                            ranges.add(XBool.X)
                            values.add(builder.variable(elementId.toString(), elementId.toString()))
                        }

                        else ->  // throw Error("Forbidden boolSpec value in ValueFeature $id: $valueSpec")
                        {
                            ranges.add(XBool.X)
                            values.add(builder.variable(elementId.toString(), elementId.toString()))
                        }
                    }
                }
                valueSpecs = ranges
                vectorQuantity = VectorQuantity(values)
            }
            BaseType.String -> {
                val values = mutableListOf<StrDD>()
                if(valueSpecs.isEmpty())
                    values.add(builder.Strings)
                valueSpecs.forEach{ values.add(builder.string(it as String)) }
                vectorQuantity = VectorQuantity(values)
            }
            else -> {
                feature.model!!.status.warn( Issue.Kind.WARN_UNRESOLVED_TYPE, "no type found for ${membership.memberName}; assuming Real", element = feature)
                vectorQuantity = Quantity(builder.Reals, unitSpec)
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
        when(str.trim().lowercase(Locale.US)) {
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

        var lb = -Double.POSITIVE_INFINITY
        var ub = Double.POSITIVE_INFINITY

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
     * Returns min value of position index of VectorQuantity
     */
    override fun <T: Number>  min(index: Int): T =
        when (vectorQuantity.values.getOrNull(index)) {
            is AADD -> (vectorQuantity.valuesIn(unitSpec)[index] as AADD).getRange().min
            is IDD -> (vectorQuantity.valuesIn(unitSpec)[index] as IDD).getRange().min
            else -> throw SemanticError(".min can only be applied on properties of type Integer or Real", element=feature)
        } as T

    /**
     * Returns max value of VectorQuantity's position index
     */
    override fun <T: Number> max(index: Int): T =
        when (vectorQuantity.values.getOrNull(index)) {
            is AADD -> (vectorQuantity.valuesIn(unitSpec)[index] as AADD).getRange().max
            is IDD -> (vectorQuantity.valuesIn(unitSpec)[index] as IDD).getRange().max
            else -> throw InternalError(".max can only be applied on properties of type Integer or Real")
        } as T


    /**
     * This method calls the parser with a given property, from which the dependency string is
     * parsed and the ast is created.
     */
    override fun compileExpression() {
        try {
            val parserSysMD = KerML(feature.model!!)
                .also { it.input = feature.expression?:"" }

            // The parsing itself, can throw exceptions that are caught optionally below.
            if (feature.model?.repo?.scalarType == null)
                feature.model?.status?.error("Could not resolve ScalarValues::ScalarValue -- add usage of ScalarValues", element = feature, kind = Issue.Kind.ERROR_UNRESOLVED_NAME)
            feature.type.filterIsInstance<UnresolvedFeature>().forEach {
                feature.model?.status?.error("Could not resolve Type '${it.relativeName}' of feature ${feature.qualifiedName}", element = feature, kind = Issue.Kind.ERROR_UNRESOLVED_NAME)
            }
            if (!feature.specializes(feature.model?.repo?.scalarType))
                feature.model?.status?.error("Expected subtype of ScalarValues::ScalarValue", element = feature)

            if (feature.expression?.isNotBlank() == true) {
                // set the scope to the element to which the property belongs.
                feature.owner
                    ?: throw SemanticError("No owner of ${feature.qualifiedName}; initialize identifications before using services.")
                parserSysMD.semantics.namespace = feature.owningNamespace!!
                parserSysMD.semantics.expression = feature

                ast = AstRoot(feature.model!!, feature, parserSysMD.Expression())
                // Initialize internal AST nodes, starting from leaves
                ast?.runDepthFirst { initialize() }
                when (baseType) {
                    BaseType.Bool if (ast!!.upQuantity.values[0] !is BDD) ->
                        throw SemanticError("Expecting dependency of type Boolean")
                    BaseType.Int if (ast!!.upQuantity.values[0] !is IDD) ->
                        throw SemanticError("Expecting dependency of type Integer")
                    BaseType.Real if (ast!!.upQuantity.values[0] !is AADD) ->
                        throw SemanticError("Expecting dependency of type Real")
                    BaseType.String if (ast!!.upQuantity.value !is StrDD) ->
                        throw SemanticError("Expecting dependency of type String")
                    else -> {}
                }
            }
        } catch (exception: Exception) {
            ast = null
            membership.model?.status?.error(
                 "In expression '${feature.expression}' of ${feature.qualifiedName}: ${exception.message}",
                element = membership,
                cause = exception)
        }
    }

    /**
     * Simple check whether there is a direct cyclic dependency in this variable.
     * Complex dependencies involving other variables are not found.
     * @throws SemanticError if there is a direct cyclic dependency.
     */
    override fun checkForCyclicDependency() {
        // Check if there is a cyclic dependency in a single expression ... should be better at
        // overall level -> todo.
        val leaveNames = mutableSetOf<String>()
        if (ast is AstRoot
            && (ast as AstRoot).dependency !is AstBySpecializations
            && (ast as AstRoot).dependency !is AstByParts
            && (ast as AstRoot).dependency !is AstByImplements
        ) {
            ast!!.getLeaves().forEach {
                if (it.qualifiedName != null)
                    leaveNames.add(it.qualifiedName!!)
            }
            if (membership.memberName in leaveNames || membership.memberShortName in leaveNames)
                throw SemanticError("Cyclic Dependency in '$name' ")
        }
    }

    override fun bool(): XBool {
        return bdd()
    }

    override fun <T : Comparable<T>> range(index: Int): NumberRange<T> {
        return when (baseType) {
            BaseType.Int -> idd().getRange()
            BaseType.Real -> aadd().getRange()
            else -> throw SysMDError("Conversion not possible")
        } as NumberRange<T>
    }
}
