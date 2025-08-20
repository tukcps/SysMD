package com.github.tukcps.sysmd.services

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.cspsolver.valuefeatures.RelatedExpression
import com.github.tukcps.sysmd.exceptions.*
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.functions.AstByImplements
import com.github.tukcps.sysmd.model.expression.functions.AstByParts
import com.github.tukcps.sysmd.model.expression.functions.AstBySpecializations
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.ReferenceSubsettingImplementation
import com.github.tukcps.sysmd.services.check.checkConsistencyOfInheritance
import com.github.tukcps.sysmd.services.check.checkNameResolutionSuccessful
import com.github.tukcps.sysmd.services.inheritance.*
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveFeatureChain
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.getAllOfClass
import java.util.*

private fun Session.fillCache() {
    // Cache frequently used types for use in semantic checks
    repo.numberType = global.resolve<DataType>("ScalarValues::Number")
    repo.scalarType = global.resolve<DataType>("ScalarValues::ScalarValue")
    repo.realType = global.resolve<DataType>("ScalarValues::Real")
    repo.integerType = global.resolve<DataType>("ScalarValues::Integer")
    repo.naturalType = global.resolve<DataType>("ScalarValues::Natural")
    repo.booleanType = global.resolve<DataType>("ScalarValues::Boolean")
    repo.stringType = global.resolve<DataType>("ScalarValues::String")
    repo.inRangeType = global.resolve<DataType>("Ranges::InRange")
    repo.occurrence = global.resolve<Type>("Occurrences::Occurrence")
    repo.links      = global.resolve<Association>("Links::Link")
}

private fun Session.giveUUID5(){
    get().filter { it.isLibraryElement || it.isStandard }. forEach {
        val old = it.elementId
        it.generateUUID()
        if (old != it.elementId) {
            repo.elements.remove(old)
            repo.elements[it.elementId!!] = it
        }
    }
}


/**
 * For Connectors, the end features are references to the source and target.
 * This method gets all Connectors and adds to each end feature a reference to source resp. targets.
 */
private fun Session.addEndFeatureReferences() {
    get().filterIsInstance<Connector>().forEach { connector ->
        val ends = connector.ownedElement.filter { it is Feature && it.isEnd  }
        // TODO: consider multiplicity.
        if (connector.source.isNotEmpty() && connector.target.isNotEmpty() && ends.size >= 2) {
            val sourceEnd = ends[0] as Feature
            val targetEnd = ends[1] as Feature
            val ref = ReferenceSubsettingImplementation(sourceEnd, connector.source.first() as Feature)
            addOwnedRelationship(ref, sourceEnd)
            val refT = ReferenceSubsettingImplementation(targetEnd, connector.target.first() as Feature)
            addOwnedRelationship(refT, targetEnd)
        }
    }
}

/**
 * Schedules and initialize the properties and the elements.
 * This shall be done before calling propagate().
 */
fun Session.initialize(level: Int = 100) {
    if (settings.initialize) {
        try {
            if (level > 0) { // Ownership and Type definitions
                resolveAllNames()
                fillCache()
                get().asSequence().filterIsInstance<Type>().forEach { type -> type.checkForCycles() }
                get().asSequence().filterIsInstance<Specialization>().forEach { it.general.subtypes.add(it.specific) }
            }
            if (level > 1) {  // Inheritance and redefinition
                anything.addInheritedToSubtypes() // Calls 'initialize' of types that will add inherited properties.
                anything.addInheritedToSubtypes() // Dirty for redefinitions missed due to race condition
            }
            if (level > 2) { // Feature chains considering inherited features
                addEndFeatureReferences()
                resolveAllNames()
                resolveAllFeatureChains()
                giveUUID5()
            }

            if (level > 3)
                checkNameResolutionSuccessful()

            if (level > 4) {
                // We do static semantic checks ...
                getAllOfClass<Type>().asSequence().forEach { type ->
                    type.checkForCycles()
                }

                getAllOfClass<Feature>().asSequence().forEach { feature -> feature.checkIsNotTypedByOwner() }
            }
            if (level > 5) initVariables()

            // Now, we only do checking and reporting of issues to the Agenda.
            if (level > 7) get().asSequence().filterIsInstance<Type>().forEach {
                checkConsistencyOfInheritance(it)
            }
        } catch (error: Exception) {
            if (error is SysMDException)
                status.error(message = error.message, cause = error)
            else
                status.error(message = "Semantic analysis failed (${error}) ", cause = SysMDException("Initialization failed", cause = error))
        }
    }
}


/**
 * No guarantee that name references can be resolved.
 * Ensures that at least an initial number of elements is initialized.
 * No tests are made; they can only be done later.
 */
internal fun Session.resolveAllNames() {

    fun relationsWithUnresolvedReferences() = get()
        .asSequence()
        .filterIsInstance<Relationship>()
        .filter { rel -> rel !is Redefinition && (rel.source.any { it is Unresolved } || rel.target.any { it is Unresolved }) }
        .toSet()

    var relationshipsWithUnresolvedReferences = relationsWithUnresolvedReferences()
    var progress = true

    while (progress) {
        relationshipsWithUnresolvedReferences.forEach {
            for (index in it.source.indices) {
                if (it.source[index] is Unresolved) {
                    if ((it.source[index] as Unresolved).relativeName == null)
                        status.warn(Issue.Kind.ERROR_UNRESOLVED_NAME, "Unresolved element with no name", element = it)
                    else {
                        val unresolved = it.source[index] as Unresolved
                        // Try resolving all unresolved names except redefinitions that are done later
                        val resolved =
                            if (it is Redefinition) null else it.owningNamespace?.resolve<Element>(unresolved.relativeName!!)
                        if (resolved != null)
                            when (unresolved) {
                                is Feature if (resolved !is Feature)
                                    -> status.error("Expecting a kind of feature", kind = Issue.Kind.ERROR_TYPE_WRONG, element = it)

                                is Type if (resolved !is Type)
                                    -> status.error("Expecting a kind of type", kind = Issue.Kind.ERROR_TYPE_WRONG, element = it)

                                is Namespace if (resolved !is Namespace)
                                    -> status.error("Expecting a kind of namespace", kind = Issue.Kind.ERROR_TYPE_WRONG, element = it)

                                else -> it.source[index] = resolved
                            }
                    }
                }
            }

            for (index in it.target.indices) {
                if (it.target[index] is Unresolved) {
                    if ((it.target[index] as Unresolved).relativeName == null)
                        status.warn(Issue.Kind.ERROR_UNRESOLVED_NAME, "Unresolved element with no name", element = it)
                    else {
                        val unresolved = it.target[index] as Unresolved
                        val resolved =
                            if (it is Redefinition) null else it.owningNamespace?.resolve<Element>(unresolved.relativeName!!)
                        if (resolved != null)
                            when (unresolved) {
                                is Feature if (resolved !is Feature)
                                    -> status.error("Expecting a kind of feature", kind = Issue.Kind.ERROR_TYPE_WRONG, element = it)

                                is Type if (resolved !is Type)
                                    -> status.error("Expecting a kind of type", kind = Issue.Kind.ERROR_TYPE_WRONG, element = it)

                                is Namespace if (resolved !is Namespace)
                                    -> status.error("Expecting a kind of namespace", kind = Issue.Kind.ERROR_TYPE_WRONG, element = it)

                                else -> it.target[index] = resolved
                            }
                    }
                }
            }
        }
        val new = relationsWithUnresolvedReferences()
        progress = (relationshipsWithUnresolvedReferences.size - new.size) > 0
        relationshipsWithUnresolvedReferences = new
    }
}



/**
 * Calls the 'initialize' function of all elements except Expressions in any order.
 * No guarantee that name references can be resolved.
 * Ensures that at least an initial number of elements is initialized.
 * No tests are made; they can only be done later.
 */
internal fun Session.resolveAllFeatureChains() {

    initializeAllAssociations()

    get().asSequence().filterIsInstance<Relationship>().forEach {
        for (index in it.source.indices) {
            if (it.source[index] is UnresolvedFeatureChain) {
                if ( (it.source[index] as Unresolved).relativeName == null)
                    status.warn(Issue.Kind.ERROR_UNRESOLVED_NAME, "Unresolved feature chain with no name", element = it)
                else {
                    val unresolvedFeature = it.source[index] as Unresolved
                    val resolvedFeature = it.owningNamespace?.resolveFeatureChain(unresolvedFeature.relativeName!!)
                    if (resolvedFeature != null)
                        it.source[index] = resolvedFeature
                }
            }
        }
        for (index in it.target.indices) {
            if (it.target[index] is UnresolvedFeatureChain) {
                if ((it.target[index] as Unresolved).relativeName == null)
                    status.warn(Issue.Kind.ERROR_UNRESOLVED_NAME, "Unresolved element with no name", element = it)
                else {
                    val unresolvedFeature = it.target[index] as UnresolvedFeatureChain
                    val resolvedFeature = it.owningNamespace?.resolveFeatureChain(unresolvedFeature.relativeName!!)
                    if (resolvedFeature != null)
                        it.target[index] = resolvedFeature
                }
            }
        }
    }
}

/**
 * Sets the Association source and target types to the respecitive end features.
 */
internal fun Session.initializeAllAssociations() {
    get().asSequence().filterIsInstance<Association>().forEach { association ->
        // --> to initialize after inheritance!
        val endFeature = association.ownedElement
            .asSequence().filterIsInstance<Feature>().filter { feature -> feature.isEnd }.toList()

        if (endFeature.size >= 2) {
            association.sourceType = endFeature[0]
            association.targetType = mutableListOf(endFeature[1])
            for(i in 2..<endFeature.size) {
                association.targetType = mutableListOf(endFeature[i])
            }
        }
    }
}



/**
 * Initializes the properties:
 * - Schedules the properties such that they are ordered following their data dependencies,
 * i.e., a property that depends on the computation of another property is scheduled behind it.
 * - Does a single evaluation upwards to propagate types and units; this ensures that the quantity property of each
 * Property is set correctly.
 */
private fun Session.initVariables() {
    repo.schedule.clear()
    astNodes.clear()
    // First, we initialize all properties in the symbol table, such that the values are equal to the specified ranges.
    // This guarantees that if we later infer types, the declared properties are already initialized.
    // FIXME: Temporary workaround, to preserve order before sorting

    // Create a variable for each feature for constraint propagation
    val variables = mutableListOf<Variable>()
    val features = get().filterIsInstance<Feature>()
    features.forEach { feature ->
        when {
            feature.owner is Type && (feature.owner as Type).specializes(repo.inRangeType) -> {
                /* no Variable, handled by constraints of variables */
            }

            feature.specializes(repo.integerType) -> {
                feature.variable = VariableImplementation(feature, BaseType.Int)
                variables.add(feature.variable!!)
            }

            feature.specializes(repo.booleanType) -> {
                feature.variable = VariableImplementation(feature, BaseType.Bool)
                variables.add(feature.variable!!)
            }

            feature.specializes(repo.realType) -> {
                feature.variable = VariableImplementation(feature, BaseType.Real)
                variables.add(feature.variable!!)
            }

            feature.specializes(repo.stringType) -> {
                feature.variable = VariableImplementation(feature, BaseType.String)
                variables.add(feature.variable!!)
            }
        }
    }
    variables.sortBy { it.name.hashCode() }

    // Then, we set up a list of computed and not-yet-computed properties.
    val computed = mutableSetOf<Variable>()
    val notComputed = LinkedList<Variable>()
    variables.forEach {
        notComputed += it
        it.initVectorQuantity()
    }

    // Check if there is a cyclic dependency in a single expression ... should be better at
    // overall level -> todo.
    variables.forEach { variable ->
        val leaveNames = mutableSetOf<String>()
        if (variable.ast is AstRoot
            && (variable.ast as AstRoot).dependency !is AstBySpecializations
            && (variable.ast as AstRoot).dependency !is AstByParts
            && (variable.ast as AstRoot).dependency !is AstByImplements
        ) {
            variable.ast!!.getLeaves().forEach {
                if (it.qualifiedName != null)
                    leaveNames.add(it.qualifiedName!!)
            }
            if (variable.feature.declaredName in leaveNames || variable.feature.declaredShortName in leaveNames)
                throw SemanticError("Cyclic Dependency in ${variable.name}")
        }
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
                val notComputedElements = notComputed.map { it.name }
                if (!dependentProperties.any { it in notComputedElements }) {
                    computed += it
                    notComputed -= it
                    repo.schedule += it
                } else {
                    notComputed.removeFirst()
                    notComputed.add(it)
                }
            } else {  // For constants, literals, ...
                notComputed -= it
                computed += it
                repo.schedule += it
            }
        } catch (exception: Exception) {
            status.error(message = exception.message ?: "Error during scheduling of constraints", cause = exception)
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
            repo.schedule += it
        }

    // Initialize internal AST nodes, starting from leaves upwards.
    repo.schedule.forEach {
        if (it.feature.expression?.isNotBlank() == true && it !is RelatedExpression)
            it.compileExpression()
        it.ast?.runDepthFirst { initialize() }
    }

    repo.schedule.forEach {
        try {
            it.ast?.evalUpRec()
        } catch (exception: Exception) {
            status.error(exception.message ?: "Problem during initialization", cause = exception)
        }
    }

    get().filterIsInstance<Feature>().forEach { feature ->
        //if(feature.type[0].ref is AttributeDefinitionImplementation)
        if (feature.expression != null && feature.expression!!.isNotEmpty()) { //for nested attributes, there can be a feature of another type with an expression, which needs to be calculated
            val namespace = feature.owningNamespace

            val referencingVars = getVariables(feature.expression!!, namespace!!)
            referencingVars.forEach { referencingVar ->
                if (referencingVar != null) {
                    feature.owner!!.ownedElement.find { it == feature }
                    feature.ownedElement.filter { it is Feature }.forEach { ownedFeature ->
                        referencingVar.ownedElement.filter { it is Feature }
                            .forEach { referencingFeature ->
                                if (ownedFeature.declaredName == referencingFeature.declaredName) {
                                    if ((ownedFeature as Feature).variable != null)
                                    if (feature.path() in ownedFeature.variable!!.feature.path()) { //test if the previous feature is already replaced
                                        ownedFeature.variable = (referencingFeature as Feature).variable
                                    } else {
                                        ownedFeature.variables.add((referencingFeature as Feature).variable)
                                    }
                                }
                            }
                    }
                }
            }
        }
        if (!dSolver.isInitialized())
            dSolver.initialize(this)
        dSolver.update(repo.schedule)
    }
}

fun getVariables(expression: String, namespace: Namespace): List<Feature?> {
    var names = mutableListOf<String>()
    var expressionString = expression.replace(" ", "") //remove spaces
    if (expressionString.elementAt(0) == '(' && expression.elementAt(expression.length - 1) == ')') {
        expressionString = expressionString.removePrefix("(").removeSuffix(")")
        names = expressionString.split(",").toMutableList()
    } else {
        names.add(expression)
    }
    val variables = mutableListOf<Feature?>()
    names.forEach { variables.add(namespace.resolve<Feature>(it)) }
    if (variables.size == 1 && variables[0] == null)
        return emptyList()
    return variables
}
