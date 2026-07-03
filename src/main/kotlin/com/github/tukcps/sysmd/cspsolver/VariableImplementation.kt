package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.legacy.Expression
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.exceptions.ExpressionError
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SolverError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.functions.AstByImplements
import com.github.tukcps.sysmd.model.expression.functions.AstByParts
import com.github.tukcps.sysmd.model.expression.functions.AstBySpecializations
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.StrDD
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.Range
import io.github.tukcps.aadd.values.XBool
import java.util.*
import java.util.Locale.getDefault


/**
 * A variable in the system of inequations / constraints
 * @param membership the membership that links the feature and its element
 * @param solver the solver that uses the variable
 * @param path the path in the model to the variable
 * @param baseType the base type, i.e., Bool, Real, Int, String.
 * @param satisfyAll whether the constraint system shall be satisfiable for each of the variables
 * @param unitSpec for Quantity, the expected unit
 * @param valueSpecs a vector with constraints
 * @param domain the domain of the unit
 * @param expression a dependency as arithmetic/boolean expression
 */
@Suppress("UNCHECKED_CAST")
open class VariableImplementation (
    private var membership: Membership,
    override val solver: Solver,
    override val path: String,
    override val baseType: BaseType,
    override val satisfyAll: Boolean = false,
    private val valueSpecs: List<String> = listOf(),
    override val unitSpec: String = "",
    override val domain: String? = null,
    override var expression: String? = null
): Variable {
    val builder get() = solver.model.builder
    override var updated: Boolean = true
    override var hasBeenChanged: Boolean = true

    /** access methods for the valueSpec field; returns different types */
    override var rangeSpecs: MutableList<Range> = mutableListOf()
    override var boolSpecs: MutableList<XBool> = mutableListOf()
    override var intSpecs: MutableList<IntegerRange> = mutableListOf()
    override val stringSpecs: MutableList<String> = mutableListOf()

    init {
        when (baseType) {
            BaseType.Real -> if (valueSpecs.isEmpty()) rangeSpecs = mutableListOf(Range.Reals) else
                valueSpecs.forEach { rangeSpecs.add(if (it.isNotBlank()) Range(it) else Range.Reals) }

            BaseType.Int  -> if(valueSpecs.isEmpty()) intSpecs = mutableListOf(IntegerRange.Integers) else
                valueSpecs.forEach { intSpecs.add(if (it.isNotBlank()) IntegerRange(it.trim('[', ']', ' ')) else IntegerRange.Integers) }

            BaseType.Bool -> if(valueSpecs.isEmpty()) boolSpecs = mutableListOf(XBool.X)
                else valueSpecs.forEach {
                    when (it.trim().lowercase(getDefault())) {
                        "true"  -> boolSpecs.add(XBool.True)
                        "false" -> boolSpecs.add(XBool.False)
                        else    -> boolSpecs.add(XBool.X)
                    }
                }
            BaseType.String -> {}
            BaseType.Unknown -> throw SysMDError("Unknown variable type: $baseType")
        }
    }

    private fun formatSingleValue(element: DD<*>): String {
        return when (element) {
            is IDD -> {
                val range = element.getRange()
                val minIsInf = range.min == Long.MIN_VALUE || range.min <= -2147483647L
                val maxIsInf = range.max == Long.MAX_VALUE || range.max >= 2147483647L
                when {
                    minIsInf && maxIsInf -> "*..*"
                    range.min == range.max -> range.min.toString()
                    range.min > range.max -> "∅"
                    else -> {
                        val min = if (minIsInf) "*" else range.min.toString()
                        val max = if (maxIsInf) "*" else range.max.toString()
                        "$min..$max"
                    }
                }
            }
            is AADD -> Representer().represent(element)
            else -> element.toString()
        }
    }

    /** A getter for a string representation of the value, with field for serialization. */
    override var valueStr: String = ""
        get() {
            val formatted = vectorQuantity.values.map { formatSingleValue(it) }
            field = if (formatted.all { it == "*..*" }) "*..*"
            else if (formatted.size > 1) formatted.toString()
            else formatted[0]
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
                val values = mutableListOf<AADD>()
                rangeSpecs.forEach{values.add(builder.real(it,path)) }
                val unitDomain = unitSpec // TODO!!!
                vectorQuantity = VectorQuantity(values, unitSpec, domain?:"")
            }
            BaseType.Int -> {
                val values = mutableListOf<IDD>()
                intSpecs.forEach { values.add(builder.integer(it)) }
                vectorQuantity = VectorQuantity(values)
            }
            BaseType.Bool -> {
                val values = mutableListOf<BDD>()
                boolSpecs.forEach {
                    values.add(
                        when (it) {
                            XBool.True -> builder.True
                            XBool.False -> builder.False
                            else -> builder.variable(path, path)
                        }
                    )
                }
                vectorQuantity = VectorQuantity(values)
            }
            BaseType.String -> {
                val values = mutableListOf<StrDD>()
                if(valueSpecs.isEmpty())
                    values.add(builder.Strings)
                valueSpecs.forEach{ values.add(builder.string(it)) }
                vectorQuantity = VectorQuantity(values)
            }
            else -> {
                throw SysMDError( "no type found for ${path}; assuming Real")
                // vectorQuantity = Quantity(builder.Reals, unitSpec)
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
            "true"          -> boolSpecs = mutableListOf(XBool.True)
            "false"         -> boolSpecs = mutableListOf(XBool.False)
            "x", "unknown"  -> boolSpecs = mutableListOf(XBool.X)
            "nab"           -> boolSpecs = mutableListOf(XBool.NaB)
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
        rangeSpecs = mutableListOf(Range(lb, ub))
        return this
    }

    /** Setter for a real ValueFeature */
    override fun rangeSpec(lb: Double?, ub: Double?): Variable {
        updated = true
        if (lb == null || ub == null) return this
        if (lb > ub)
            throw ExpressionError("in range subtype constraint, lower bound must be less or equal upper bound.")
        rangeSpecs = mutableListOf(Range(lb, ub))
        return this
    }

    /** Setter for a real ValueFeature */
    override fun rangeSpec(init : Range): Variable {
        updated = true
        if (init.min > init.max)
            throw ExpressionError("in range subtype constraint, lower bound must be less or equal upper bound.")
        rangeSpecs = mutableListOf(Range(init.min, init.max))
        return this
    }


    /** Setter for range specification that also initializes the value */
    override fun intSpec(init: IntegerRange) : Variable {
        updated = true
        if (init.min > init.max)
            throw ExpressionError("in integer range subtype constraint, lower bound must be less or equal upper bound.")
        intSpecs = mutableListOf(IntegerRange(init.min, init.max))
        return this
    }


    /**
     * Casts the quantity value to AADD and returns it.
     */
    override fun aadd(): AADD {
        if (vectorQuantity.values[0] is AADD) return vectorQuantity.valuesIn(unitSpec)[0] as AADD
        else throw SolverError("Expression value cannot be cast to AADD", path = path)
    }

    override fun bdd(): BDD {
        if (vectorQuantity.values[0] is BDD) return vectorQuantity.values[0] as BDD
        else
            throw SolverError("Expression value cannot be cast to BDD", path = path)
    }

    override fun idd(): IDD {
        if (vectorQuantity.values[0] is IDD) return vectorQuantity.values[0] as IDD
        else throw SolverError("Expression value cannot be cast to IDD", path = path)
    }


    /** Creates a compact string, skipping fields not relevant, incl. doc */
    override fun toString(): String =
        "Variable { path=${path}, type=$baseType, unitSpec=$unitSpec, valueSpecs=$valueSpecs, value = $vectorQuantity }"

    /**
     * Returns min value of position index of VectorQuantity
     */
    override fun <T: Number>  min(index: Int): T =
        when (vectorQuantity.values.getOrNull(index)) {
            is AADD -> (vectorQuantity.valuesIn(unitSpec)[index] as AADD).getRange().min
            is IDD -> (vectorQuantity.valuesIn(unitSpec)[index] as IDD).getRange().min
            else -> throw SolverError(".min can only be applied on properties of type Integer or Real", path=path)
        } as T

    /**
     * Returns max value of VectorQuantity's position index
     */
    override fun <T: Number> max(index: Int): T =
        when (vectorQuantity.values.getOrNull(index)) {
            is AADD -> (vectorQuantity.valuesIn(unitSpec)[index] as AADD).getRange().max
            is IDD -> (vectorQuantity.valuesIn(unitSpec)[index] as IDD).getRange().max
            else -> throw SolverError(".max can only be applied on properties of type Integer or Real", path=path)
        } as T


    /**
     * This method calls the parser with a given property, from which the dependency string is
     * parsed and the ast is created.
     */
    override fun compileExpression() {
        try {
            val parserSysMD = KerML(solver.model).also { it.input = expression?:"" }

            if (expression?.isNotBlank() == true) {
                // set the scope to the element to which the property belongs.
                parserSysMD.semantics.namespace = membership.owningNamespace!!

                ast = AstRoot(solver.model, this, parserSysMD.Expression())
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
            solver.model.status.error(
                message = "In variable '${expression}' of ${path}: ${exception.message}",
                path = path,
                cause = exception
            )
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
                throw SolverError("Cyclic Dependency in '$path' ", path = path)
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
