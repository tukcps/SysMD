package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.legacy.Expression
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.exceptions.*
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.parseIntegerRange
import com.github.tukcps.sysmd.parseRealRange
import com.github.tukcps.sysmd.quantities.Representer
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.DDTypeCastError
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.NumberRange
import io.github.tukcps.aadd.values.bool.XBool
import io.github.tukcps.aadd.values.bounds.Bound
import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.ia.RealRange
import java.util.*
import java.util.Locale.getDefault
import kotlin.uuid.Uuid


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
    override val relatedElement: Uuid? = null,
    @Deprecated("Replace with relatedElement (getting element by path requires session, by uuid trivial)",
        replaceWith = ReplaceWith("relatedElement"))
    override val path: String,
    override val baseType: BaseType,
    override val satisfyAll: Boolean = false,
    private val  valueSpecs: List<String> = listOf(),
    override val unitSpec: String = "",
    override val domain: String? = null,
    override var expression: String? = null
): Variable {
    val builder get() = solver.model.builder
    override var updated: Boolean = true
    override var hasBeenChanged: Boolean = true

    /** access methods for the valueSpec field; returns different types */
    override var rangeSpecs: MutableList<RealRange> = mutableListOf()
    override var boolSpecs: MutableList<XBool> = mutableListOf()
    override var intSpecs: MutableList<IntegerRange> = mutableListOf()
    override val stringSpecs: MutableList<String> = mutableListOf()

    init {
        when (baseType) {
            BaseType.Real -> if (valueSpecs.isEmpty()) rangeSpecs = mutableListOf(RealRange.Reals) else
                valueSpecs.forEach {
                    rangeSpecs.add(parseRealRange(it)) }

            BaseType.Int  -> if(valueSpecs.isEmpty()) intSpecs = mutableListOf(IntegerRange.All) else
                valueSpecs.forEach {
                    intSpecs.add(parseIntegerRange(it)) }

            BaseType.Bool -> if(valueSpecs.isEmpty()) boolSpecs = mutableListOf(XBool.All)
                else valueSpecs.forEach {
                    when (it.trim().lowercase(getDefault())) {
                        "true"  -> boolSpecs.add(XBool.True)
                        "false" -> boolSpecs.add(XBool.False)
                        else    -> boolSpecs.add(XBool.All)
                    }
                }
            BaseType.String -> {}
            BaseType.Unknown -> throw SysMDError("Unknown variable type: $baseType")
        }
    }

    private fun formatSingleValue(element: DD<*>): String {
        return when (element) {
            // FIXME: When adopting -*..*, remove .replace("-*..", "*..") so negative infinity is preserved
            is IDD -> element.getRange().toString().let {
                it.replace("-*..", "*..")
            }
            is AADD -> Representer.represent(element)
            else -> element.toString()
        }
    }

    /** A getter for a string representation of the value, with field for serialization. */
    override var valueStr: String = ""
        get() {
            val formatted = vectorQuantity.values.map { formatSingleValue(it) }
            // FIXME: When adopting -*..*, change check and assigned value from "*..*" to "-*..*"
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
                            XBool.True -> builder.Bool.True
                            XBool.False -> builder.Bool.False
                            else -> builder.variable(path, path)
                        }
                    )
                }
                vectorQuantity = VectorQuantity(values)
            }
            BaseType.String -> {
                val values = mutableListOf<StrDD>()
                if(valueSpecs.isEmpty())
                    values.add(builder.Strings.All)
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
            "x", "unknown"  -> boolSpecs = mutableListOf(XBool.All)
            "nab"           -> boolSpecs = mutableListOf(XBool.Empty)
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
        rangeSpecs = mutableListOf(RealRange(lb, ub))
        return this
    }

    /** Setter for a real ValueFeature */
    override fun rangeSpec(lb: Double?, ub: Double?): Variable {
        updated = true
        if (lb == null || ub == null) return this
        if (lb > ub)
            throw ExpressionError("in range subtype constraint, lower bound must be less or equal upper bound.")
        rangeSpecs = mutableListOf(RealRange(lb, ub))
        return this
    }

    /** Setter for a real ValueFeature */
    override fun rangeSpec(init : RealRange): Variable {
        updated = true
        if (init.min > init.max)
            throw ExpressionError("in range subtype constraint, lower bound must be less or equal upper bound.")
        rangeSpecs = mutableListOf(RealRange(init.min, init.max))
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


    /** The values in the unit of [unitSpec], or in SI if there is none. */
    private fun valuesInUnitSpec(): List<DD<*>> =
        if (unitSpec.isEmpty()) vectorQuantity.valuesInSI() else vectorQuantity.valuesIn(unitSpec)

    /**
     * Casts the quantity value to AADD and returns it.
     */
    override fun aadd(): AADD {
        if (vectorQuantity.values[0] is AADD) return valuesInUnitSpec()[0] as AADD
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
    override fun <T: Bound>  min(index: Int): T =
        when (vectorQuantity.values.getOrNull(index)) {
            is AADD -> (valuesInUnitSpec()[index] as AADD).getRange().min
            is IDD -> (valuesInUnitSpec()[index] as IDD).getRange().min
            else -> throw SolverError(".min can only be applied on properties of type Integer or Real", path=path)
        } as T

    /**
     * Returns max value of VectorQuantity's position index
     */
    override fun <T: Bound> max(index: Int): T =
        when (vectorQuantity.values.getOrNull(index)) {
            is AADD -> (valuesInUnitSpec()[index] as AADD).getRange().max
            is IDD -> (valuesInUnitSpec()[index] as IDD).getRange().max
            else -> throw SolverError(".max can only be applied on properties of type Integer or Real", path=path)
        } as T

    override fun <T : NumberRange<*>> range(index: Int): T {
        return when (baseType) {
            BaseType.Int -> idd().getRange() as T
            BaseType.Real -> aadd().getRange() as T
            else -> throw SysMDError("Conversion not possible")
        }
    }

    /**
     * This method calls the parser with a given property, from which the dependency string is
     * parsed and the ast is created.
     */
    override fun compileExpression() {
        var elementForError: Element? = null
        try {
            val parserSysMD = KerML(solver.model).also { it.input = expression?:"" }

            if (expression?.isNotBlank() == true) {
                // set the scope to the element to which the property belongs.
                parserSysMD.semantics.namespace = membership.owningNamespace!!
                elementForError = parserSysMD.semantics.namespace
                ast = AstRoot(solver.model, this, parserSysMD.Expression())
                // Initialize internal AST nodes, starting from leaves
                ast?.runDepthFirst { initialize() }
                when (baseType) {
                    BaseType.Bool if (ast!!.upQuantity.values[0] !is BDD) ->
                        throw SolverError("Expecting expression of type Boolean")
                    BaseType.Int if (ast!!.upQuantity.values[0] !is IDD) ->
                        throw SolverError("Expecting expression of type Integer")
                    BaseType.Real if (ast!!.upQuantity.values[0] !is AADD) ->
                        throw SolverError("Expecting expression of type Real", elementId =  relatedElement)
                    BaseType.String if (ast!!.upQuantity.values[0] !is StrDD) ->
                        throw SolverError("Expecting expression of type String", elementId = relatedElement)
                    else -> {}
                }
            }
        } catch (exception: Exception) {
            ast = null
            if (exception is SysMDException) {
                solver.model.status.error(
                    message = "In variable '${expression}' of ${path}: ${exception.message}",
                    element = (exception.element?: solver.model[relatedElement?: solver.model.global.elementId])?.toElementData(),
                    cause = exception
                )
            } else if (exception is DDTypeCastError) {
                solver.model.status.error(
                    message = "In variable '${expression}' of ${path}: In SysMD, we expect typed literals (1.0 is Real. 1 is Integer, and do NOT CAST.",
                    element = elementForError?.toElementData(),
                    cause = exception,
                )
            }
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

}
