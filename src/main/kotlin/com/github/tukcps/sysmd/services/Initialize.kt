package com.github.tukcps.sysmd.services

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.cspsolver.valuefeatures.RelatedExpression
import com.github.tukcps.sysmd.exceptions.InternalError
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDException
import com.github.tukcps.sysmd.exceptions.SysMDInconsistency
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.functions.AstByImplements
import com.github.tukcps.sysmd.model.expression.functions.AstByParts
import com.github.tukcps.sysmd.model.expression.functions.AstBySubclasses
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.compiler.parseDependency
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.model.sysml.implementation.AttributeUsageImplementation
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.ite
import com.github.tukcps.sysmd.services.check.checkIdentifications
import com.github.tukcps.sysmd.services.inheritance.addInherited
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.Session
import kotlin.math.abs


/**
 * Schedules and initialize the properties and the elements.
 * This shall be done before calling propagate().
 */
fun Session.initialize(level: Int = 100) {
    if (settings.initialize) {
        try {
            if (level > 0) resolveNames()        // Calls initialize of all elements --> at least ownership should be resolved.
            repo.numberType = global.resolve<DataType>("ScalarValues::Number")
            repo.scalarType = global.resolve<DataType>("ScalarValues::ScalarValue")
            repo.realType = global.resolve<DataType>("ScalarValues::Real")
            repo.integerType = global.resolve<DataType>("ScalarValues::Integer")
            repo.booleanType = global.resolve<DataType>("ScalarValues::Boolean")
            repo.stringType = global.resolve<DataType>("ScalarValues::String")
            if (level > 1) addInherited()     // Calls initialize of types that will add inherited properties.
            if (level > 2) resolveNames()        // Again, update name resolution considering types and inheritance
            if (level > 5) initRelationships()
            if (level > 4) addInherited()  //call add inherited again to add inherited relationships of resolved names
            if (level > 4) initVariables()

            // Now, we only do checking and reporting of issues to the Agenda.
            if (level > 6) checkIdentifications()
            if (level > 7) get().filterIsInstance<Type>().forEach { checkConsistencyOfInheritance(it) }
        } catch (error: Exception) {
            if (error is SysMDException)
                report(error)
            else
                report(SemanticError(message = "Initialization failed (${error}) ", cause = error))
            if (!settings.catchExceptions) throw error
        }
    }
}


private fun Session.initRelationships() {
    get().filterIsInstance<Relationship>().forEach {
        it.resolveNames()
    }
}


/**
 * Calls the 'initialize' function of all elements except Expressions in any order.
 * No guarantee that name references can be resolved.
 * Ensures that at least an initial number of elements is initialized.
 * No tests are made; they can only be done later.
 */
internal fun Session.resolveNames() {
    var stable: Boolean
    var iterations = 1000

    // First create ownership hierarchy
    do {
        stable = true
        iterations--

        // Check if an owner of element can be identified and
        // create them in the model.
        val elementsIdentified = mutableListOf<Element>()
        getUnownedElements().forEach {
            if (it.startOfPath is Namespace && it.path != null) {
                val resolvedOwner = (it.startOfPath as Namespace).resolve<Element>(it.path!!, searchInSuperClass = false, resolveReferences = false)
                if (resolvedOwner != null) {
                    val added = create(it.element, resolvedOwner)
                    elementsIdentified.add(it.element)
                    if (added != it.element)
                        updateUnownedElements(it.element, added)
                    stable = false
                }
            } else {
                if (it.startOfPath.model == this && it.path == null) {
                    val added = create(it.element, it.startOfPath)
                    elementsIdentified.add(it.element)
                    stable = false
                    if (added !== it.element)
                        updateUnownedElements(it.element, added)
                }
            }
        }
        elementsIdentified.forEach { dropUnownedElement(it) }
    } while (!stable && iterations > 0)
    if (!stable)
        reportInfo(global, "Not enough iterations in initialization; increase no. of iterations")

    get().forEach {
        if(it != global && it.owningNamespace == null) {
            report(it, "Element for which an owner could not be resolved: $it")
        }
    }

    // identify all names, but not yet Types w/ inheritance
    val elements = get().filter { it !is Type }
    for (element in elements) {
        element.resolveNames()
        if (element.updated)
            stable = false
        element.updated = false
    }

    // finally, inherited elements as well.
    val elements2 = get()
    for (element in elements2) {
        element.resolveNames()
        if (element.updated)
            stable = false
        element.updated = false
    }
}


/**
 * Initializes the properties:
 * - Schedules the properties such that they are ordered following their data dependencies,
 * i.e., a property that depends on the computation of another property is schedule behind it.
 * - Does a single evaluation upwards to propagate types and units; this ensures that the quantity property of each
 * Property is set correctly.
 */
private fun Session.initVariables() {
    repo.schedule.clear()
    // First, we initialize all properties in the symbol table, such that the values are equal to the specified ranges.
    // This guarantees that if we later infer types, the declared properties are already initialized.
    // FIXME: Temporary workaround, to preserve order before sorting

    // Create a variable for each feature for constraint propagation
    val variables = mutableListOf<Variable>()
    get().filterIsInstance<Feature>().forEach { feature ->
            when {
                feature.specializes(repo.realType) -> {
                    feature.variable = VariableImplementation(feature, BaseType.Real)
                    variables.add(feature.variable!!)
                }
                feature.specializes(repo.integerType) -> {
                    feature.variable = VariableImplementation(feature, BaseType.Int)
                    variables.add(feature.variable!!)
                }
                feature.specializes(repo.booleanType) -> {
                    feature.variable = VariableImplementation(feature, BaseType.Bool)
                    variables.add(feature.variable!!)
                }
                feature.specializes(repo.stringType) -> {
                    feature.variable = VariableImplementation(feature, BaseType.Str)
                    variables.add(feature.variable!!)
                }
            }
        }
    variables.sortBy { it.name.hashCode() }

    // Then, we set up a list of computed and not-yet-computed properties.
    val computed = mutableSetOf<Variable>()
    val notComputed = mutableListOf<Variable>()
    variables.forEach {
        notComputed += it
        it.initVectorQuantity()
        // reportError(it, "Feature '${it.qualifiedName}' has a type '${it.superclass.str}' that could not be resolved")
    }

    // Check if there is a cyclic dependency in a single expression ... should be better at
    // overall level -> todo.
    variables.forEach { variable ->
        val leaveNames = mutableSetOf<String>()
        if ((variable.ast != null) && ((variable.ast as AstRoot).dependency !is AstBySubclasses && (variable.ast as AstRoot).dependency !is AstByParts)
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
            report(InternalError(message = exception.message?:"Error during scheduling of constraints", cause = exception))
            computed += it
            notComputed -= it
            // schedule+=it --- we do not schedule an erroneous dependency.
        }
    }
    // add all remaining elements to schedule
    if (iterations >= 10000)
        while (notComputed.isNotEmpty()) {
            val it = notComputed.first()
            computed += it
            notComputed -= it
            repo.schedule += it
        }

    // Initialize internal AST nodes, starting from leaves upwards.
    repo.schedule.forEach {
        if (it.dependency.isNotBlank() && it !is RelatedExpression)
            it.parseDependency()
        it.ast?.runDepthFirst { initialize() }
    }

    repo.schedule.forEach {
        try {
            it.ast?.evalUpRec()
        } catch (exception: Exception) {
            report(exception)
        }
    }
    get().filterIsInstance<FeatureImplementation>().forEach{feature->
        if(feature.variable==null)
            if(feature.expression!=null && feature.expression!="") { //for nested attributes, there can be a feature of another type with an expression, which needs to be calculated
                var namespace = feature.owningNamespace
                while(namespace is FeatureImplementation)
                    namespace = namespace.owningNamespace
                val referencingVars = getVariables(feature.expression!!, namespace!!, feature)
                referencingVars.forEach { referencingVar ->
                    feature.owner.ref!!.ownedElement.find { it.ref == feature }
                    feature.ownedElement.filter { it.ref is FeatureImplementation }.map{it.ref}.forEach {owenedFeature ->
                        referencingVar!!.ownedElement.filter { it.ref is FeatureImplementation }.map{it.ref}.forEach { referencingFeature ->
                            if(owenedFeature!!.declaredName == referencingFeature!!.declaredName && owenedFeature.declaredName != "multiplicity") {
                                if(feature.qualifiedName in (owenedFeature as FeatureImplementation).variable!!.feature.qualifiedName ) { //test if the previous feature is already replaced
                                    owenedFeature.variable = (referencingFeature as FeatureImplementation).variable
                                }else {
                                    owenedFeature.variables.add((referencingFeature as FeatureImplementation).variable)
                                }
                            }
                        }
                    }
                }
            }

    }
    if (!dSolver.isInitialized())
        dSolver.initialize(this, 100)
    repo.schedule.forEach { dSolver.update(it) }
}
fun getVariables(expression: String, namespace: Namespace, feature: Feature):List<FeatureImplementation?>{
    var names = mutableListOf<String>()
    var expressionString = expression.replace(" ","") //remove spaces
    if(expressionString.elementAt(0)== '(' && expression.elementAt(expression.length-1)== ')') {
        expressionString = expressionString.removePrefix("(").removeSuffix(")")
        names = expressionString.split(",").toMutableList()
    } else{
        names.add(expression)
    }
    val variables = mutableListOf<FeatureImplementation?>()
    names.forEach { variables.add(namespace.resolve<FeatureImplementation>(it))}
    return variables
}


/**
 * Searches for a property in an element and, if not found in its subclasses.
 * The value of the property is computed using the ITE function.
 */
fun Session.estimateProperty(namespace: Namespace, qualifiedName: QualifiedName): VectorQuantity {
    // It is already known from this or a superclass.
    val p = namespace.resolveVar(qualifiedName)

    if (p != null) return p.vectorQuantity

    val subclasses = getSubclasses(namespace)
    var quantity: VectorQuantity? = null
    for (subclass in subclasses) {
        quantity = if (subclass == subclasses.first())
            estimateProperty(subclass, qualifiedName)
        else {
            val alternative = builder.variable("select_" + subclass.elementId, qualifiedName, true)
            alternative.ite(estimateProperty(subclass, qualifiedName), quantity!!)
        }
    }
    return quantity!!
}



/**
 * We look at an element and its superclass(es).
 * - The subclass properties must be a subset of the superclass properties with the same name.
 * - Maybe additional needs for other types; t.b.d.
 */
fun Session.checkConsistencyOfInheritance(element: Type) {
    element.allSupertypes().forEach { supertype ->
        element.ownedElement.forEach { ownedElement ->
            val owned = ownedElement.ref!!
            if (owned is Feature) {
                val superclassFeature = supertype.getOwnedElement(owned.declaredName, owned.declaredShortName)
                if (owned.isFeatureWithValue() && owned !is Multiplicity && superclassFeature is Feature) {
                    // Checks for supertype and subclass property
                    // Basic requirement for inheritance must hold in all cases otherwise something went wrong before ...
                    if (superclassFeature in owned.allSupertypes(true))
                        reportInconsistency(owned, "INCONSISTENCY: specialization ${owned.escapedName()} has feature that must be specialization of feature of its general class ${supertype.escapedName()}")
                    when {
                        owned.specializes(repo.realType) -> {
                            //Convert Ranges or owned and supertype to SI
                            owned.typeConstraint.indices.forEach {
                                var ownedRangeSpec = owned.typeConstraint.getOrNull(it)?:"*..*" // Default: all Reals
                                if (ownedRangeSpec.isBlank()) ownedRangeSpec = "*..*"
                                if (owned.type.first().ref!!.specializes(repo.realType)) {
                                    val ownedRange = Quantity(builder.range(Range(ownedRangeSpec)), owned.unitConstraint ?: "").getRange()

                                    val superClassRangeSpec = if (superclassFeature.typeConstraint.getOrNull(it).isNullOrBlank())
                                            Range.Reals
                                        else
                                            Range(superclassFeature.typeConstraint.getOrNull(it)!!)

                                    val extendedRangeSuperclass = builder.range(
                                        superClassRangeSpec.min - abs(superClassRangeSpec.min * 0.000001),
                                        superClassRangeSpec.max + abs(superClassRangeSpec.max * 0.000001)
                                    )
                                    val superClassRange = Quantity(extendedRangeSuperclass, superclassFeature.unitConstraint?:"").getRange()
                                    if (ownedRange !in superClassRange && ownedRange != Range.Reals)
                                        reportInconsistency(
                                            owned,
                                            "INCONSISTENCY: value ${owned.typeConstraint} of specialization must be refinement of general ${superclassFeature.escapedName()} with value ${superclassFeature.typeConstraint}"
                                        )
                                    if ((owned.unitConstraint ?: "") != (superclassFeature.unitConstraint ?: ""))
                                        reportInconsistency(
                                            owned,
                                            "INCONSISTENCY: unit of specialization ${owned.unitConstraint} of '${superclassFeature.escapedName()}' must be the same as '${superclassFeature.unitConstraint}'"
                                        )
                                }
                            }

                        }
                        owned.specializes(repo.integerType) ->
                            owned.typeConstraint.indices.forEach {
                                if (superclassFeature.indices?.contains(it) != false) {
                                    if (IntegerRange(owned.typeConstraint[it]) !in IntegerRange(superclassFeature.typeConstraint[it]) && IntegerRange(
                                            owned.typeConstraint[it]
                                        ) != IntegerRange.Integers
                                    )
                                        reportInconsistency(
                                            owned,
                                            "INCONSISTENCY: subclass value ${owned.typeConstraint} of ${owned.escapedName()} must be refinement of supertype value ${superclassFeature.typeConstraint}"
                                        )
                                }
                            }
                        owned.specializes(repo.booleanType) ->
                        { // if (owned.boolSpec !in superclassFeature.boolSpec) {
                            // TODO: Agree with Axel & Sebastian how to handle digital inconsistencies.
                            // reportError(get(it),
                            //    "INCONSISTENCY: subclass value ${owned.boolSpec} of ${owned.effectiveName} must be refinement of supertype value ${superclassProperty.boolSpec}"
                            //)
                        }
                    }
                }
                if (superclassFeature is Feature && owned.multiplicity !in superclassFeature.multiplicity)
                    report(superclassFeature, "INCONSISTENCY: multiplicity of subclass must be subset of supertype multiplicity.",
                        SysMDInconsistency("INCONSISTENCY: multiplicity of subclass must be subset of supertype multiplicity."))
            }
        }
    }
}
