package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType.Unknown
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.FeatureReferenceExpression
import com.github.tukcps.sysmd.model.expression.checkEvent
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.Runlevel
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
    private val schedule: MutableList<Variable> = mutableListOf()

    fun reset() {
        variables.clear()
        schedule.clear()
    }

    /**
     * Adds a Variable to the hash-map of all variables.
     * @param path The qualified name of path of the respective feature, used as key
     * @param variable instance of the variable
     */
    fun addVariable(path: String, variable: Variable) {
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


    fun getVariable(path: String, index: Int = 0): Variable? {
        variables[path]?.getOrNull(index)?.let { return it }
        val feature = model.global.resolve(path)?.memberElement as? Feature
        val ref = feature?.referencedFeature
        if (ref != null) {
            return getVariable(ref.path(), index)
        }
        return null
    }

    /**
     * Gets a variable by its id. The id is equal to the element id if there is an associated feature.
     */
    fun getVariable(id: UUID, index: Int = 0) = variables[(model[id] as? Membership)?.memberElement?.path()]?.get(index)
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
        val memberships = model.get().filterIsInstance<OwningMembership>()
            .filter { it.memberElement is Feature && (it.memberElement as Feature).specializes(model.repo.scalarType)}

        memberships.forEach { membership ->
            val feature = membership.memberElement as Feature
            val baseType = feature.toBaseType()
            val name = feature.path()
            val owner = feature.owner

            if (feature.referencedFeature === null &&
                !(feature is Expression && owner is Expression) && // skip non-basic expressions
                membership.owningNamespace !is FeatureReferenceExpression && // skip duplicating referenced features
                baseType != Unknown
            ) {
                if (feature.owner is Type && (feature.owner as Type).specializes(model.repo.inRangeType)) {
                    /* no Variable, handled by constraints of variables */
                } else
                    addVariable(name,
                        VariableImplementation(
                            membership,
                            solver = model.solver,
                            relatedElement = membership.memberElement.elementId,
                            path = feature.path(),
                            baseType = feature.toBaseType(),
                            satisfyAll = feature.isSufficient,
                            unitSpec = feature.unitConstraint?:"",
                            valueSpecs = feature.typeConstraint,
                            expression = feature.expression
                        )
                    )
            }
        }

        val sortedVars = variables.values.sortedBy { it.first().path }

        // Then, we set up a list of computed and not-yet-computed properties.
        val computed = mutableSetOf<Variable>()
        val notComputed = LinkedList<Variable>()
        sortedVars.forEach { v ->
            notComputed += v
            v.forEach { it.initVectorQuantity() }
        }

        // Compile all expressions first so that ASTs and their dependency strings are available
        // for the topological sort below. This is necessary so that aggregation functions like
        // sumOverParts can report their actual (fully-qualified) dependencies.
        sortedVars.forEach { v -> v.forEach {
            it.compileExpression() }
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
                    val notComputedElements = notComputed.map { it.path }
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

        // Re-initialize AST nodes in dependency order so that aggregation functions
        // (e.g. sumOverParts) see correctly computed values from their dependencies.
        schedule.forEach { variable ->
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
		    if(feature.expression != null && feature.expression!!.isNotEmpty())
		    { //for nested attributes, there can be a feature of another type with an expression, which needs to be calculated
			    getFeatures(feature.expression!!, feature.owningNamespace!!).filterNotNull().forEach { referencingVar ->
				    feature.owner!!.ownedElement.find { it == feature }
				    feature.ownedElement.filterIsInstance<Feature>().forEach { ownedFeature ->
					    referencingVar.ownedElement.filterIsInstance<Feature>().forEach { referencingFeature ->
						    if(ownedFeature.escapedName() == referencingFeature.escapedName())
						    {
							    ownedFeature.variable?.let { ownedVar ->
									referencingFeature.variable?.let { rv -> // can this ever be null?
										if(feature.path() in ownedVar.path) // ownedVar? not rv?
										{ //test if the previous feature is already replaced
											setVariable(ownedFeature.path(), rv)
										}
										else
										{
											addVariable(ownedFeature.path(), rv)
										}
									}
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
     * Enum for controlling the direction of the propagation.
     */
    enum class PropagateDirection { UP, DOWN, BOTH }
    /**
     * Simple constraint propagation.
     * Requires calling initialize if ast is not yet initialized, e.g., if it comes from database or REST.
     * Or as a benchmark to demonstrate the benefit of his method.
     */
    fun propagate(direction: PropagateDirection = PropagateDirection.BOTH) {
        try {
            if (schedule.isEmpty())
                model.initialize(Runlevel.VARIANCE_CHECKED)
            if (discreteSolver.isInitialized())
                discreteSolver.initialize(model)

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
                                if (direction == PropagateDirection.UP || direction == PropagateDirection.BOTH)
                                    variable.ast!!.evalUpRec()
                                if (direction == PropagateDirection.DOWN || direction == PropagateDirection.BOTH)
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
                        model.status.error( e.message ?: "(issue in constraint propagation, ${variable.path})", cause = e)
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
                model.status.warn(
                    Issue.Kind.WARN_ITERATIONS_EXCEEDED,
                    "Number of constraint propagation iterations exceeded; issue with: $instable. Increase it if needed.",
                )


            // Copy updated entries into the status map, check consistency.
            schedule.forEach {
                if (it.updated)
                    model.status.updatedValues[it.path] = it.valueStr
            }
        } catch (error: Exception) {
            model.status.error("During propagation: ${error.message}", cause = error)
        }
    }
}
