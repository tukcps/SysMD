package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType.Unknown
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.model.util.TypeConstraint
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.values.bool.XBool
import java.util.*
import kotlin.uuid.Uuid

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

    /** While a propagation iteration runs: collects variables that evalDown of a leaf has narrowed. */
    internal var changedByDownPropagation: MutableSet<Variable>? = null

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

        // Then, we set up a list of not-yet-computed properties.
        val notComputed = LinkedList<Variable>()
        sortedVars.forEach { v ->
            notComputed += v
            v.forEach {
                it.initVectorQuantity()
                // Explicit unit on feature (not inherited from datatype)
                val f = it.relatedElement?.let { id -> model[id] as? Feature }
                val rangeF = f?.getOwned<Feature>("range")
                val unitF = f?.getOwned<Feature>("unit")
                val isExplicit = (rangeF != null && !rangeF.isImpliedIncluded && TypeConstraint(rangeF.expression ?: "").unit.isNotEmpty()) ||
                                 (unitF != null && !unitF.isImpliedIncluded && !unitF.expression.isNullOrBlank())
                it.vectorQuantity = it.vectorQuantity.copy(userWantedUnitSpec = isExplicit)
            }
        }

        // Compile all expressions first so that ASTs and their dependency strings are available
        // for the topological sort below. This is necessary so that aggregation functions like
        // sumOverParts can report their actual (fully-qualified) dependencies.
        sortedVars.forEach { v -> v.forEach {
            it.compileExpression() }
        }

        // Order the properties by their dependencies into the repo.schedule.
        // This schedule is used for initialization of the properties.
        // The variables are taken in sorted order; a variable is scheduled as soon as none of the paths it depends on
        // belongs to a variable that is not yet scheduled. All bookkeeping is done with counters (linear effort).
        val pending = notComputed.toList()
        val dependencies = arrayOfNulls<Set<String>>(pending.size)
        val failure = arrayOfNulls<Exception>(pending.size)
        val notScheduledWithPath = HashMap<String, Int>()
        pending.forEach { notScheduledWithPath.merge(it.path, 1, Int::plus) }
        val dependents = HashMap<String, MutableList<Int>>()
        val unsatisfied = IntArray(pending.size)
        pending.forEachIndexed { i, variable ->
            try {
                dependencies[i] = variable.ast?.getDependencyStrings()
            } catch (exception: Exception) {
                failure[i] = exception
            }
            dependencies[i]?.forEach { dependency ->
                if (dependency in notScheduledWithPath) {
                    unsatisfied[i]++
                    dependents.getOrPut(dependency) { mutableListOf() }.add(i)
                }
            }
        }
        fun done(i: Int) {
            val path = pending[i].path
            if (notScheduledWithPath.merge(path, -1, Int::plus) == 0)
                dependents[path]?.forEach { unsatisfied[it]-- }
        }

        val queue = ArrayDeque<Int>(pending.indices.toList())
        var withoutProgress = 0
        while (queue.isNotEmpty() && withoutProgress <= queue.size) {
            val i = queue.removeFirst()
            val error = failure[i]
            if (error != null) {
                model.status.error(message = error.message ?: "Error during scheduling of constraints", cause = error)
                done(i) // we do not schedule an erroneous dependency.
                withoutProgress = 0
            } else if (unsatisfied[i] == 0) {
                schedule += pending[i]
                done(i)
                withoutProgress = 0
            } else {
                queue.addLast(i)
                withoutProgress++
            }
        }
        // add all remaining elements (cyclic dependencies) to the schedule
        queue.forEach { schedule += pending[it] }

        // Re-initialize AST nodes in dependency order so that aggregation functions
        // (e.g. sumOverParts) see correctly computed values from their dependencies.
        schedule.forEach { variable ->
            variable.checkForCyclicDependency()
            variable.ast?.runDepthFirst { initialize() }
        }

        // The nodes of each AST have been evaluated by their initialize() in dependency order;
        // only the root remains to apply the variable's range and unit constraints.
        schedule.forEach {
            try {
                it.ast?.evalUp()
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
        // A literal or an expression with operators (e.g. "4.5" or "a * b") is no name and cannot be resolved;
        // trying it would search all enclosing scopes, supertypes and imports in vain.
        names.forEach { features.add(if (isNameCandidate(it)) namespace.resolve(it)?.memberElement as? Feature else null) }
        if (features.size == 1 && features[0] == null)
            return emptyList()
        return features
    }


    /** Whether a text can be a (qualified) name or a feature chain at all. */
    private fun isNameCandidate(text: String): Boolean {
        if ('\'' in text) return true // unrestricted names may contain any character
        val first = text.firstOrNull() ?: return false
        if (!(first.isLetter() || first == '_' || first == '$')) return false
        return text.none { it.isWhitespace() || it in "+-*/^<>=!&|?[]{}\"%~(),;" }
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
            var instable: MutableSet<Variable> = mutableSetOf()

            // The first iteration visits every variable. After that, only variables are visited whose
            // neighbourhood changed in the previous iteration.
            var changed: Set<Variable>? = null // null: visit all
            val dependsOn = HashMap<Variable, Set<Variable>>()
            val alwaysVisit = HashSet<Variable>()
            schedule.forEach { variable ->
                val ast = variable.ast ?: return@forEach
                // Boolean variables are coupled by the discrete solver; aggregations, user-defined functions and
                // functions that resolve features of a namespace hold dependencies that are not visible as leaves
                // of this AST.
                var opaque = variable.baseType == BaseType.Bool
                ast.runDepthFirst {
                    if (this is AstAggregationFunction || this is AstUserDefinedFunction || this is AstSumI ||
                        this is AstByParts || this is AstBySpecializations ||
                        this is AstByImplements || this is AstHasA
                    ) opaque = true
                }
                if (opaque) alwaysVisit.add(variable)
                dependsOn[variable] = ast.getLeaves().mapNotNull { it.variable }.toSet()
            }
            fun needsVisit(variable: Variable): Boolean {
                val c = changed ?: return true
                return variable in c || variable in alwaysVisit || dependsOn[variable]?.any { it in c } == true
            }

            do {
                val changedNow = mutableSetOf<Variable>()
                changedByDownPropagation = changedNow
                instable = mutableSetOf()
                modelIsStable = true
                try {
                    val visited = schedule.filter { it.baseType != BaseType.String && needsVisit(it) }
                    visited.forEach { variable ->
                        try {
                            assert(variable.baseType != Unknown)
                            if (variable.ast != null) {
                                if (direction == PropagateDirection.UP || direction == PropagateDirection.BOTH)
                                    variable.ast!!.evalUpRec()
                                // Down-propagation of Real and Integer variables is done in the backward pass below.
                                if ((direction == PropagateDirection.DOWN || direction == PropagateDirection.BOTH) &&
                                    variable.baseType == BaseType.Bool)
                                    variable.ast!!.evalDownRec()
                                variable.checkEvent()     // Sets property.stable to false,
                                // if changed in an iteration step, and property.updated iff changed in a 'propagate' call
                                if (variable.baseType == BaseType.Bool && variable.updated) {
                                    discreteSolver.update(variable)
                                }
                            } else
                                variable.stable = true
                            if (!variable.stable)
                                instable.add(variable)

                            modelIsStable = modelIsStable and variable.stable
                        } catch (e: Exception) {
                            variable.stable = true
                            model.status.error(e.message ?: "(issue in constraint propagation, ${variable.path})", cause = e)
                        }
                    }
                    // Backward pass: evaluates Real and Integer variables downwards in reverse schedule order, so
                    // that a constraint at the end of a dependency chain reaches its inputs within one iteration.
                    // Boolean variables are evaluated in the forward pass together with the discrete solver.
                    if (direction != PropagateDirection.UP)
                        visited.asReversed().filter { it.ast != null && it.baseType != BaseType.Bool }.forEach { variable ->
                            try {
                                variable.ast!!.evalDownRec()
                                variable.checkEvent()
                                if (!variable.stable) {
                                    instable.add(variable)
                                    modelIsStable = false
                                }
                            } catch (e: Exception) {
                                variable.stable = true
                                model.status.error(e.message ?: "(issue in constraint propagation, ${variable.path})", cause = e)
                            }
                        }
                } finally {
                    changedByDownPropagation = null
                }
                discreteSolver.advanceState()
                discreteSolver.assertConstraints()

                // Variables to visit next: those not stable yet, and those narrowed by evalDown of others.
                changed = instable + changedNow
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

            checkConstraints()
        } catch (error: Exception) {
            model.status.error("During propagation: ${error.message}", cause = error)
        }
    }

    /**
     * Checks all constraints in the model and solver variables.
     * If any constraint is not fulfilled or cannot be satisfied,
     * reports an inconsistency issue to the session status (agenda).
     */
    fun checkConstraints() {
        val reportedElements = mutableSetOf<Uuid>()
        val reportedPaths = mutableSetOf<String>()

        model.status.issues.forEach { issue ->
            issue.element?.let { reportedElements.add(it) }
        }

        fun isInsideFunctionOrCalculation(element: Element?): Boolean {
            var current = element
            while (current != null) {
                if (current is com.github.tukcps.sysmd.model.sysml.CalculationDefinition || current is com.github.tukcps.sysmd.model.kerml.Function) return true
                current = current.owner
            }
            return false
        }

        fun reportInconsistency(element: Element?, variable: Variable, namePrefix: String? = null) {
            val elementData = element?.toElementData()
            val owner = element?.owner
            val ownerPath = if (owner != null && owner !== model.global && owner.path() != "Global") owner.path() else null
            val declaredName = element?.declaredName?.takeIf { !it.startsWith("assertConstraintUsage") && !it.startsWith("constraintUsage") }
            val name = element?.escapedName()?.takeIf { !it.startsWith("assertConstraintUsage") && !it.startsWith("constraintUsage") } ?: declaredName

            val desc = when {
                name != null && ownerPath != null -> "'$name' in '$ownerPath'"
                name != null -> "'$name'"
                ownerPath != null -> "in '$ownerPath'"
                else -> "'${element?.path() ?: variable.path}'"
            }

            val valDesc = if (variable.valueStr.isNotBlank() && variable.valueStr != "*..*") " (${variable.valueStr})" else ""
            val prefix = namePrefix ?: "Constraint $desc is not fulfilled: is not satisfiable"

            model.status.inconsistency(
                message = "$prefix$valDesc",
                element = elementData
            )
            element?.elementId?.let { reportedElements.add(it) }
            reportedPaths.add(variable.path)
        }

        fun isUnsatisfiable(variable: Variable): Boolean {
            val isContradiction = variable.valueStr == "Contradiction" || variable.valueStr == "Infeasible" || variable.valueStr == "∅"
            val hasInfeasibleValue = variable.isVectorQuantityInitialized && variable.vectorQuantity.values.any {
                it.isInfeasible() || (it is AADD && it.isEmpty()) || (it is IDD && it.isEmpty()) || it === model.builder.Bool.Infeasible || it === model.builder.Bool.Empty
            }
            return isContradiction || hasInfeasibleValue
        }

        // 1. Check all Invariants (including AssertConstraintUsages)
        val constraintElements = model.get().filterIsInstance<Invariant>()
        for (element in constraintElements) {
            if (element.elementId in reportedElements) continue
            if (isInsideFunctionOrCalculation(element)) continue
            val path = element.path()
            if (path in reportedPaths) continue

            val variable = getVariable(path) ?: (element as? Feature)?.variable ?: continue
            val boolValue = if (variable.baseType == BaseType.Bool && variable.isVectorQuantityInitialized) {
                when {
                    variable.vectorQuantity.values.any { it === model.builder.Bool.False } -> XBool.False
                    variable.vectorQuantity.values.any { it === model.builder.Bool.True } -> XBool.True
                    variable.valueStr.equals("false", ignoreCase = true) -> XBool.False
                    variable.valueStr.equals("true", ignoreCase = true) -> XBool.True
                    else -> null
                }
            } else null

            val isNegated = element.isNegated
            val isContradiction = variable.valueStr == "Contradiction"

            val violated = when {
                !isNegated && boolValue == XBool.False -> true
                isNegated && boolValue == XBool.True -> true
                isContradiction -> true
                else -> false
            }

            if (violated) {
                reportInconsistency(element, variable)
            }
        }

        // 2. Check Variables with explicit expressions or satisfyAll
        for (variable in getVariables()) {
            val relatedId = variable.relatedElement
            if (relatedId != null && relatedId in reportedElements) continue
            if (variable.path in reportedPaths) continue

            if (!variable.satisfyAll) continue

            val element = (if (relatedId != null) model[relatedId] else null) ?: model.global.resolve(variable.path)?.memberElement
            if (element != null && element.elementId in reportedElements) continue
            if (isInsideFunctionOrCalculation(element)) continue
            if (element is Invariant) continue
            if (element is com.github.tukcps.sysmd.model.sysml.ConstraintUsage) continue
            if (element is com.github.tukcps.sysmd.model.expression.BooleanExpression) continue

            if (isUnsatisfiable(variable)) {
                reportInconsistency(element, variable, namePrefix = "Constraint for '${variable.path}' is not satisfiable")
            }
        }

        // If a constraint is reported as not satisfiable, the reports about the unsatisfiable dependencies
        // that lead to it are redundant.
        if (model.status.issues.any { it.message.startsWith("Constraint ") && it.message.contains("not satisfiable") })
            model.status.issues.removeAll { it.message.startsWith("dependency of ") && it.message.endsWith("is not satisfiable") }
    }
}
