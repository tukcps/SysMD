package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType.Unknown
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.expression.checkEvent
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session
import java.util.*

/**
 * The solver consists of two solvers, one for booleans,
 * and one for numbers. C
 * @param model the related session and its model
 */
class Solver(
    val model: Session
) {
    private  var discreteSolver: DiscreteSolver = DiscreteSolver(this)

    /**
     * Relationship between the Feature's memberships and a related vector of variables.
     * The membership is used as key as it is unique even in case of inheritance.
     * A vector is needed as a feature can hold a multiplicity number of variables.
     */
    private val variables: HashMap<String, ArrayList<Variable>> = HashMap()

    /** List that stores the features that are part of constraint propagation */
    val schedule: MutableList<Variable> = mutableListOf()

    fun reset() {
        variables.clear()
        schedule.clear()
    }

    @Deprecated("Use memberQualifiedName() as key")
    fun addVariable(membership: Membership, variable: Variable) {
        if (variables[membership.memberElement.path()] != null)
            variables[membership.memberElement.path()]!!.add(variable)
        else
            variables[membership.memberElement.path()] = arrayListOf(variable)
    }

    /**
     * Adds a Variable to the hash-map of all variables.
     * @param path The qualified name of path of the respective feature, used as key
     * @param variable instance of the variable
     */
    fun addVariable(path: QualifiedName, variable: Variable) {
        if (variables[path] != null)
            variables[path]!!.add(variable)
        else
            variables[path] = arrayListOf(variable)
    }

    /**
     * Replaces a variable in the hash-map of all variables
     * @param path The qualified name or path of the respective feature, used as key
     * @param variable instance of the variable
     */
    fun setVariable(path: String, variable: Variable) {
        variables[path] = arrayListOf(variable)
    }


    fun getVariable(path: String, index: Int = 0): Variable? =
        variables[path]?.get(index)

    /**
     * Gets a variable by its id. The id is equal to the element id if there is an associated feature.
     */
    fun getVariable(id: UUID, index: Int = 0) = variables[(model[id] as? Membership)?.memberElement?.path()]?.get(index)

    @Deprecated("Use memberQualifiedName() as key")
    fun getVariable(membership: Membership, index: Int = 0) = variables[membership.memberElement.path()]?.get(index)


    fun getVariables(path: String) = variables[path]

    /**
     * Returns a list of all Feature's variables.
     * @return List with variables of all features
     */
    fun getVariables(): List<Variable> {
        val vars = mutableListOf<Variable>()
        variables.values.forEach { arr -> arr.forEach {  vars.add(it) } }
        return vars
    }


    /**
     * Initializes the properties:
     * - Schedules the properties such that they are ordered following their data dependencies,
     * i.e., a property that depends on the computation of another property is scheduled behind it.
     * - Does a single evaluation upwards to propagate types and units; this ensures that the quantity property of each
     * Property is set correctly.
     */
    fun initVariables() {
        reset()

        // Create a variable for each feature for constraint propagation
        val memberships = model.get().filterIsInstance<Membership>().asSequence().filter { it.memberElement is Feature }

        memberships.forEach { membership ->
            val feature = membership.memberElement as Feature
            val baseType = feature.toBaseType()
            val name = feature.path()

            if (feature.referencedFeature == null)
            if (baseType != Unknown) {
                if (feature.owner is Type && (feature.owner as Type).specializes(model.repo.inRangeType)) {
                    /* no Variable, handled by constraints of variables */
                } else
                    addVariable(name, VariableImplementation(membership, model.builder, baseType))
            }
        }

        val sortedVars = variables.values.sortedBy { it.first().membership.memberElement.path().hashCode() }

        // Then, we set up a list of computed and not-yet-computed properties.
        val computed = mutableSetOf<Variable>()
        val notComputed = LinkedList<Variable>()
        sortedVars.forEach { v ->
            notComputed += v
            v.forEach { it.initVectorQuantity() }
        }

        // Order the properties by their dependencies into the repo.schedule.
        // This schedule is used for initialization of the properties.
        var iterations = 0
        while (notComputed.isNotEmpty() && (iterations < 10000)) {
            iterations++
            val it = notComputed.first()
            try {
                if (it.ast != null) {   // For not constants, literals, ...
                    //test if there is a dependency in notComputed
                    val dependentProperties = it.ast?.getDependencyStrings()!!
                    val notComputedElements = notComputed.map { it.membership.path()+"::"+(it.membership.memberName?:it.membership.memberShortName) }
                    if (!dependentProperties.any { it in notComputedElements }) {
                        computed += it
                        notComputed -= it
                        schedule += it
                    } else {
                        notComputed.removeFirst()
                        notComputed.add(it)
                    }
                } else {  // For constants, literals, ...
                    notComputed -= it
                    computed += it
                    schedule += it
                }
            } catch (exception: Exception) {
                model.status.error(message = exception.message ?: "Error during scheduling of constraints", cause = exception)
                computed += it
                notComputed -= it
                // schedule+=it --- we do not schedule an erroneous dependency.
            }
        }
        // add all remaining elements to the schedule
        if (iterations >= 10000)
            while (notComputed.isNotEmpty()) {
                val it = notComputed.first()
                computed += it
                notComputed -= it
                schedule += it
            }

        // Initialize internal AST nodes, starting from leaves upwards.
        schedule.forEach { variable ->
            variable.compileExpression()
            variable.checkForCyclicDependency()
            variable.ast?.runDepthFirst { initialize() }
        }

        schedule.forEach {
            try {
                it.ast?.evalUpRec()
            } catch (exception: Exception) {
                model.status.error(exception.message ?: "Problem during initialization", cause = exception)
            }
        }

        // Fix for Vectors? Remove if possible
        model.get().filterIsInstance<Membership>().filter { it.memberElement is Feature }.forEach { membership ->
            val feature = membership.memberElement as Feature
            // if(feature.type[0].ref is AttributeDefinitionImplementation)
            if (feature.expression != null && feature.expression!!.isNotEmpty()) { //for nested attributes, there can be a feature of another type with an expression, which needs to be calculated

                val referencingVars = getFeatures(feature.expression!!, feature.owningNamespace!!)
                referencingVars.forEach { referencingVar ->
                    if (referencingVar != null) {
                        feature.owner!!.ownedElement.find { it == feature }
                        feature.ownedElement.filter { it is Feature }.forEach { ownedFeature ->
                            referencingVar.ownedElement.filter { it is Feature }
                                .forEach { referencingFeature ->
                                    if (ownedFeature.escapedName() == referencingFeature.escapedName()) {
                                        if ((ownedFeature as Feature).variable != null)
                                            if (feature.path() in ownedFeature.variable!!.name!!) { //test if the previous feature is already replaced
                                                setVariable(ownedFeature.path(), (referencingFeature as Feature).variable!! )
                                            } else {
                                                addVariable(ownedFeature.path(), (referencingFeature as Feature).variable !!)
                                            }
                                    }
                                }
                        }
                    }
                }
            }
            if (!discreteSolver.isInitialized())
                discreteSolver.initialize(model)
            discreteSolver.update(schedule)
        }
    }

    /**
     * Maps an expression string to a list of features.
     */
    fun getFeatures(expression: String, namespace: Namespace): List<Feature?> {
        var names = mutableListOf<String>()
        var expressionString = expression.replace(" ", "") //remove spaces
        if (expressionString.elementAt(0) == '(' && expression.elementAt(expression.length - 1) == ')') {
            expressionString = expressionString.removePrefix("(").removeSuffix(")")
            names = expressionString.split(",").toMutableList()
        } else {
            names.add(expression)
        }
        val features = mutableListOf<Feature?>()
        names.forEach { features.add(namespace.resolve(it)?.memberElement as? Feature) }
        if (features.size == 1 && features[0] == null)
            return emptyList()
        return features
    }


    /**
     * Most simple constraint propagation; just until Jack is finished.
     * Requires calling initialize if ast is not yet initialized, e.g., if it comes from database or REST.
     * Or as a benchmark to demonstrate the benefit of his method.
     */
    fun propagate() {
        try {
            if (schedule.isEmpty())
                model.initialize()
            if (discreteSolver.isInitialized())
                discreteSolver.initialize(model)
            // We use the inv { ... } syntax from standard SysMLv2 / KerML hence this is no longer needed:
            // else
            //    dSolver.processRequirements(get().filterIsInstance<Expression>().filter { it.type?.str?.contains("Requirement") == true })

            var modelIsStable: Boolean
            schedule.forEach {
                it.stable = false
                it.updated = false
            }
            model.status.numberOfPropagateIterations = 1
            var instable: Set<Variable>
            do {
                instable = mutableSetOf()
                modelIsStable = true
                schedule.forEach { variable ->
                    try {
                        assert(variable.baseType != Unknown)
                        if ( variable.baseType != BaseType.String ) {
                            modelIsStable = modelIsStable and variable.stable
                            if (variable.ast != null) {
                                variable.ast!!.evalUpRec()
                                variable.ast!!.evalDownRec()
                                variable.checkEvent()     // Sets property.stable to false,
                                // if changed in an iteration step, and property.updated iff changed in a 'propagate' call
                                if ( variable.baseType == BaseType.Bool && variable.updated ) {
                                    discreteSolver.update(variable)
                                }
                            } else
                                variable.stable = true
                            if (!variable.stable) instable.add(variable)
                        }
                    } catch (e: Exception) {
                        variable.stable = true
                        model.status.error( e.message ?: "(issue in constraint propagation)", element = variable.membership, cause = e)
                    }
                }
                discreteSolver.advanceState()
                discreteSolver.assertConstraints()

                // ----- For debugging ---
                // val inStables = repo.schedule.filter { !it.stable }
                // val stables = repo.schedule.filter { it.stable }
                model.status.numberOfPropagateIterations += 1
            } while (!modelIsStable && model.status.numberOfPropagateIterations < 100)
            if (!modelIsStable)
                model.status.warn( Issue.Kind.WARN_ITERATIONS_EXCEEDED,"Number of constraint propagation iterations exceeded; issue with: $instable. Increase it if needed.")

            // Copy updated entries into the status map, check consistency.
            schedule.forEach {
                if (it.updated)
                    model.status.updatedValues[it.elementId!!] = it.valueStr
            }
        } catch (error: Exception) {
            model.status.error("During propagation: ${error.message}", cause = error)
        }
    }
}


fun Type.toBaseType(): BaseType = when {
    this.specializes(this.model!!.repo.integerType) -> BaseType.Int
    this.specializes(this.model!!.repo.booleanType) -> BaseType.Bool
    this.specializes(this.model!!.repo.realType) -> BaseType.Real
    this.specializes(this.model!!.repo.stringType) -> BaseType.String
    else -> Unknown
}


data class VariableData(
    val name: String,
    val type: BaseType,
    val unit: String?=null,
    val value: String?=null
)

fun Element.getUnit(): String? {
    if ( (this is Feature) && this.specializes(this.model!!.repo.quantity)) {
        val unit = resolveLocal("unit")?.member<Feature>()?.expression?.trim('"', ' ')?:""
        return unit
    }
    return null
}

fun Element.getRange(): String? {
    if ( (this is Feature) && this.specializes(this.model!!.repo.range)) {
        val range = resolveLocal("range")?.member<Feature>()?.expression?.trim('"', ' ')?:""
        return range
    }
    return null
}


fun getVariableInfo(namespace: QualifiedName, membership: Membership): List<VariableData> {
    val element = membership.memberElement
    val elementName = (if (namespace.isNotEmpty()) "$namespace::" else "") + element.escapedName()
    val result = mutableListOf<VariableData>()

    if (element.owner is Type && (element.owner as Type).specializes(element.model!!.repo.inRangeType))
        return result

    if (element is Feature && element.specializes(element.model!!.repo.scalarType)) {
        result.add(VariableData(elementName, element.toBaseType(), element.getUnit(), element.getRange() ))
    }

    if (element is Namespace) {
        element.ownedMembership.forEach {
            val vars = getVariableInfo(elementName, it)
            result.addAll(vars)
        }
    }
    return result
}

fun getVariableInfo(model: Session): List<VariableData> {
    val result = mutableListOf<VariableData>()
    model.global.ownedMembership.forEach {
        result.addAll(getVariableInfo("", it))
    }
    return result
}